package eventStreaming.consumer

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}
import java.util.{Date, HashSet}

import com.mongodb.{BasicDBObject, DBCursor, MongoClient}
import eventStreaming.util.{MongoUtil, Util}
import eventStreaming.domain.{EventCursor, Event}
import org.bson.types.ObjectId

/**
 * Created by prayagupd
 * on 11/26/15.
 * The thread that is reading from the capped collection.
 */

class EventConsumer extends Runnable {
  var CONSUMER_SERVICE : String = ""
  var MONGO: MongoClient = null
  var RUNNING: AtomicBoolean = null
  var COUNTER: AtomicLong = null

  def this(consumerName: String,mongoClient: MongoClient, running: AtomicBoolean, counter: AtomicLong) {
    this()
    CONSUMER_SERVICE = consumerName
    MONGO = mongoClient
    RUNNING = running
    COUNTER = counter
  }

  def run {
    val readIds: HashSet[ObjectId] = new HashSet[ObjectId]
    var lastTimestamp: Long = 0
    while (RUNNING.get) {
      try {
        val startTime = new Date().getTime
        lastTimestamp = EventCursor.getLastIndex(MONGO, CONSUMER_SERVICE)
        Util.printLog(s"consumer#${CONSUMER_SERVICE} (first loop) looking for lastTimestamp in EventStream for last index => ${lastTimestamp}", true)
        val tailableCursor: DBCursor = MongoUtil.createTailableCursor(MONGO, lastTimestamp)
        val elapsedTime = (new Date().getTime - startTime)
        Util.printLog(s"consumer#${CONSUMER_SERVICE} | ${elapsedTime} ms", true)
        try {
          while (tailableCursor.hasNext && RUNNING.get) {
            //Util.printLog(s"consumer#${CONSUMER_SERVICE} (second loop) => listening for Event", true)
            val doc: BasicDBObject = tailableCursor.next.asInstanceOf[BasicDBObject]
            val docId: ObjectId = doc.getObjectId("_id")
            lastTimestamp = doc.getLong(Event.TIMESTAMP_FIELD)
            if (readIds.contains(docId)) {
              Util.printLog("------ duplicate id found: " + docId, true)
            }
            readIds.add(docId)
            EventCursor.setLastIndex(MONGO, CONSUMER_SERVICE, lastTimestamp)
            println(s"consumer#${CONSUMER_SERVICE} total event reads so far => ${readIds.size()}")
          }
        } catch {
          case e: Exception => {
            //e.printStackTrace()
            Util.printLog(s"error consumer#${CONSUMER_SERVICE} failed " + COUNTER.get + " for => " + e.getMessage, true)
          }
        } finally {
          try {
            if (tailableCursor != null) {
              tailableCursor.close
            }
          } catch {
            case t: Throwable => {
              Util.printLog(s"error consumer#${CONSUMER_SERVICE} failed for " + t.getMessage, true)
            }
          }
        }
      } catch {
        case t: Throwable => {
          Util.printLog(s"error consumer#${CONSUMER_SERVICE} faileddd => " + t.getMessage, true)
        }
      }
    }
  }
}
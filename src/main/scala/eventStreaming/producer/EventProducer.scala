package eventStreaming.producer

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

import com.mongodb.{BasicDBObject, MongoClient}
import eventStreaming.EventStreamApp
import eventStreaming.util.Util
import eventStreaming.domain.Event
import org.bson.types.ObjectId
/**
 * Created by prayagupd
 * on 11/26/15.
 */

/**
 * db.EventsStream.insert({TIMESTAMP_FIELD: NumberLong(1448430930244), message: NumberLong(229012), messageType: "download"})
 * The thread that is writing to the capped collection.
 */

class EventProducer extends Runnable {
  var NAME : String = ""
  var MONGO: MongoClient = null
  var RUNNING: AtomicBoolean = null
  var EVENTS_PRODUCED: AtomicLong = null

  def this(name : String, mongoClient: MongoClient, running: AtomicBoolean, counter: AtomicLong) {
    this()
    NAME = name
    MONGO = mongoClient
    RUNNING = running
    EVENTS_PRODUCED = counter
  }

  def run {
    Util.printLog(s"producer#${NAME} producing events", false)
    while (RUNNING.get) {
      val docId: ObjectId = ObjectId.get
      val document: BasicDBObject = new BasicDBObject("_id", docId)
      val d : Long = System.currentTimeMillis()
      document.put(Event.TIMESTAMP_FIELD, d.asInstanceOf[Object])
      document.put(Event.MESSAGE_TYPE, "download")
      document.put(Event.MESSAGE, "download")
      MONGO.getDB(EventStreamApp.EVENTS_DB).getCollection(Event.name).insert(document)
      EVENTS_PRODUCED.getAndIncrement()
    }
  }
}
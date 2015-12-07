package eventStreaming.consumer

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}
import java.util.{Date, HashSet}

import com.mongodb.{BasicDBObject, DBCursor, MongoClient}
import eventStreaming.util.{MongoEventStreamUtil, Util}
import eventStreaming.domain.{EventCursor, Event}
import org.bson.types.ObjectId

/**
 * Created by prayagupd
 * on 11/26/15.
 * The thread that is reading from the capped collection.
 */

class EventConsumer extends EventListener {

  def this(consumerName: String,mongoClient: MongoClient, running: AtomicBoolean, counter: AtomicLong) {
    this()
    CONSUMER_SERVICE = consumerName
    MONGO = mongoClient
    RUNNING = running
    COUNTER = counter
  }

  override def onMessage(doc: BasicDBObject) {
      println(s"message received ${doc.toJson}")
  }
}
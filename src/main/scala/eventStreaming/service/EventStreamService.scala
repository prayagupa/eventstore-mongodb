package eventStreaming.service

import com.mongodb.MongoClient
import com.mongodb.client.model.CreateCollectionOptions
import eventStreaming.domain.Event
import eventStreaming.util.MongoEventStreamUtil

/**
 * Created by prayagupd
 * on 12/7/15.
 */


object EventStreamService {
  val EVENTS_DB: String = "events-db"
  val SIZE_IN_BYTES: Long = 1 * 1024 * 1024 * 1024
  val MAX_DOCUMENTS: Int = 4 * 2 * 1000

  val NO_DOCUMENTS: Int = MAX_DOCUMENTS

  val NO_CONSUMERS: Int = 4
}

class EventStreamService {

  def initEventStream(mongo: MongoClient): Unit ={
    mongo.dropDatabase(EventStreamService.EVENTS_DB)
    MongoEventStreamUtil.eventDatabase(mongo).createCollection(Event.name,
      new CreateCollectionOptions().capped(true)
                                   .sizeInBytes(EventStreamService.SIZE_IN_BYTES)
                                   .maxDocuments(EventStreamService.MAX_DOCUMENTS))
  }
}

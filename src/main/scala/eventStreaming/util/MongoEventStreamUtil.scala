package eventStreaming.util

import java.util
import java.util.{ArrayList, List}

import com.mongodb._
import com.mongodb.client.{MongoCollection, MongoDatabase}
import eventStreaming.EventStreamApp
import eventStreaming.domain.Event
import eventStreaming.service.EventStreamService
import org.bson.Document

import scala.concurrent.forkjoin.ThreadLocalRandom

/**
  * Created by prayagupd
  * on 11/26/15.
  */

object MongoEventStreamUtil {

  val list :scala.collection.immutable.List[String] =
    scala.collection.immutable.List("PRODUCT_DOWNLOAD_EVENT", "PRODUCT_RELEASED", "PRODUCT_NOT_FOUND_EVENT")


  def mongoInstance : MongoClient = {
     new MongoClient(new MongoClientURI("mongodb://127.0.0.1:27017/events-db"))
   }
   def bulkInsertVersion2(mongo: Mongo): Boolean = {
     val collection: DBCollection = mongo.getDB(EventStreamService.EVENTS_DB).getCollection(Event.name)
     val bulk: BulkWriteOperation = collection.initializeOrderedBulkOperation

     for (i <- 0 to EventStreamService.MAX_DOCUMENTS) {
       val doc: DBObject = new BasicDBObject
       doc.put(Event.Created_At, System.currentTimeMillis)
       doc.put(Event.Event_Type, "download")
       doc.put(Event.Event, i + "")
       bulk.insert(doc)
     }
     val result: BulkWriteResult = bulk.execute

     mongo.close()
     result.isAcknowledged

   }

   def bulkInsertVersion3(mongo: MongoClient) {

     val documents: List[Document] = new ArrayList[Document]()
     val fromIndex = 0//EventStreamApp.MAX_DOCUMENTS
     val toIndex = fromIndex + EventStreamService.MAX_DOCUMENTS-10
     for (i <- fromIndex until toIndex) {
       val eventTypeIndex = ThreadLocalRandom.current().nextInt(list.size)

       val doc: Document = new Document
       doc.put(Event.OffsetIndex, i)
       doc.put(Event.Event, s"${list(eventTypeIndex)}-${i}")
       doc.put(Event.Created_At, System.currentTimeMillis)
       doc.put(Event.Event_Type, list(eventTypeIndex))
       doc.put(Event.Event, s"${list(eventTypeIndex)}-${i}")
       documents.add(doc)
     }
     val streamCappedCollection : MongoCollection[Document] = eventCollection(mongo)
     streamCappedCollection.insertMany(documents)
   }

   private def eventCollection(mongo: MongoClient): MongoCollection[Document] = {
     mongo.getDatabase(EventStreamService.EVENTS_DB).getCollection(Event.name)
   }

   def eventDatabase(mongo: MongoClient): MongoDatabase = {
     mongo.getDatabase(EventStreamService.EVENTS_DB)
   }

   def createTailableCursor(MONGO : MongoClient, lastId: Long, eventType: String): DBCursor = {
     val db : DB = MONGO.getDB(EventStreamService.EVENTS_DB)
     val collection: DBCollection = db.getCollection(Event.name)
     if (lastId == 0) {
       val q : DBObject = QueryBuilder.start(Event.Event_Type).is(MongoEventStreamUtil.list.head).get()
       return collection.find()
                        .sort(new BasicDBObject("$natural", 1))
                         .addOption(Bytes.QUERYOPTION_TAILABLE)
                         .addOption(Bytes.QUERYOPTION_AWAITDATA)
     }
     val query: BasicDBObject = new BasicDBObject(Event.Created_At, new BasicDBObject("$gt", lastId))
                                    .append(Event.Event_Type, eventType)
     println(s"querying : ${query}")
     collection.find(query)
                .sort(new BasicDBObject("$natural", 1))
                .addOption(Bytes.QUERYOPTION_TAILABLE)
                .addOption(Bytes.QUERYOPTION_AWAITDATA)
   }
 }

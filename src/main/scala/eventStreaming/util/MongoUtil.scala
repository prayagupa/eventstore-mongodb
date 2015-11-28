package eventStreaming.util

import java.util.{ArrayList, List}

import com.mongodb._
import com.mongodb.client.{MongoCollection, MongoDatabase}
import eventStreaming.EventStreamApp
import eventStreaming.domain.Event
import org.bson.Document

/**
  * Created by prayagupd
  * on 11/26/15.
  */

object MongoUtil {

   def mongoInstance : MongoClient = {
     new MongoClient(new MongoClientURI("mongodb://127.0.0.1:27017"))
   }
   def bulkInsertVersion2(): Boolean = {
     val mongo = mongoInstance
     val collection: DBCollection = mongo.getDB(EventStreamApp.EVENTS_DB).getCollection(Event.name)
     val bulk: BulkWriteOperation = collection.initializeOrderedBulkOperation

     for (i <- 0 to EventStreamApp.MAX_DOCUMENTS) {
       val doc: DBObject = new BasicDBObject
       doc.put(Event.TIMESTAMP_FIELD, System.currentTimeMillis)
       doc.put(Event.MESSAGE_TYPE, "download")
       doc.put(Event.MESSAGE, i + "")
       bulk.insert(doc)
     }
     val result: BulkWriteResult = bulk.execute

     mongo.close()
     result.isAcknowledged

   }

   def bulkInsertVersion3(mongo: MongoClient) {
     val documents: List[Document] = new ArrayList[Document]()

     for (i <- 0 to EventStreamApp.MAX_DOCUMENTS) {
       val doc: Document = new Document
       doc.put(Event.TIMESTAMP_FIELD, System.currentTimeMillis)
       doc.put(Event.MESSAGE_TYPE, "download")
       doc.put(Event.MESSAGE, "download-"+i)
       documents.add(doc)
     }
     val collection_ : MongoCollection[Document] = eventCollection(mongo)
     collection_.insertMany(documents)
   }

   private def eventCollection(mongo: MongoClient): MongoCollection[Document] = {
     mongo.getDatabase(EventStreamApp.EVENTS_DB).getCollection(Event.name)
   }

   def eventDatabase(mongo: MongoClient): MongoDatabase = {
     mongo.getDatabase(EventStreamApp.EVENTS_DB)
   }

   def createTailableCursor(MONGO : MongoClient, lastId: Long): DBCursor = {
     val db : DB = MONGO.getDB(EventStreamApp.EVENTS_DB)
     val collection: DBCollection = db.getCollection(Event.name)
     val q : DBObject = QueryBuilder.start(Event.MESSAGE_TYPE).is("download").get()
     if (lastId == 0) {
       return collection.find(q).sort(new BasicDBObject("$natural", 1))
         .addOption(Bytes.QUERYOPTION_TAILABLE).addOption(Bytes.QUERYOPTION_AWAITDATA)
     }
     val query: BasicDBObject = new BasicDBObject(Event.TIMESTAMP_FIELD, new BasicDBObject("$gt", lastId))
                                    .append(Event.MESSAGE_TYPE, "download")
     collection.find(query).sort(new BasicDBObject("$natural", 1))
       .addOption(Bytes.QUERYOPTION_TAILABLE).addOption(Bytes.QUERYOPTION_AWAITDATA)
   }
 }

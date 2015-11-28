package eventStreaming.domain

import java.util.Date

import com.mongodb.MongoClient
import com.mongodb.client.model.{UpdateOptions, Filters}
import eventStreaming.util.MongoUtil
import org.bson.Document
import org.bson.conversions.Bson

/**
 * Created by prayagupd
 * on 11/28/15.
 */

object EventCursor {
  val name : String = "EventCursor"
  val consumerNumber : String = "_id"
  val index : String = "lastIndex"
  val created : String = "created"

  def setLastIndex(mongo: MongoClient, consumerNumberV: String, lastIndexValue : Long): Unit = {
    val filter : Bson = Filters.eq(consumerNumber, consumerNumberV.toLong)
    val update : Bson =  new Document("$set",
      new Document()
        .append(index, lastIndexValue)
        .append(created, new Date()))
    val options : UpdateOptions = new UpdateOptions().upsert(true)
    MongoUtil.eventDatabase(mongo).getCollection(EventCursor.name).updateOne(filter, update, options)
  }

  def getLastIndex(mongo: MongoClient, consumerNumberV: String): Long = {
    val query = new Document(consumerNumber, consumerNumberV.toLong)
    val projection = new Document(index, true)
    val response =
      Option(MongoUtil.eventDatabase(mongo).getCollection(EventCursor.name).find(query).projection(projection).limit(1).first())
    if(response.isDefined) {
      return response.get.getLong("lastIndex")
    }
    0l
  }

  def main(args: Array[String]) {
    val mongo = MongoUtil.mongoInstance
    setLastIndex(mongo, "12", 2345)
    println(getLastIndex(mongo, "12"))
    mongo.close()
  }
}

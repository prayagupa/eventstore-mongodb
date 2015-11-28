package eventStreaming

/**
 * Created by prayagupd
 * on 11/24/15.
 */

import java.util.Date
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

import com.mongodb._
import com.mongodb.client.model.CreateCollectionOptions
import eventStreaming.consumer.EventConsumer
import eventStreaming.util.{Util, MongoUtil}
import eventStreaming.domain.Event

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object EventStreamApp {
  val EVENTS_DB: String = "events-db"
  val SIZE_IN_BYTES: Int = 1 * 1024 * 1024 * 1024
  val MAX_DOCUMENTS: Int = 2 * 1000

  val NO_DOCUMENTS: Int = MAX_DOCUMENTS

  val NO_CONSUMERS: Int = 4

  @throws(classOf[Exception])
  def main(pArgs: Array[String]) {
    val mongo: MongoClient = MongoUtil.mongoInstance

    mongo.dropDatabase(EVENTS_DB)
    MongoUtil.eventDatabase(mongo).createCollection(Event.name,
      new CreateCollectionOptions().capped(true).sizeInBytes(SIZE_IN_BYTES).maxDocuments(MAX_DOCUMENTS))

    //insert
    val s = new Date().getTime
    MongoUtil.bulkInsertVersion3(mongo)
    Util.printLog(s"Took ${(new Date().getTime - s) / 1000} secs to insert ${MAX_DOCUMENTS}", false)

    val consumerThreadStatus: AtomicBoolean = new AtomicBoolean(true)
    val consumerCounter: AtomicLong = new AtomicLong(0)
    val consumerThreads: mutable.ListBuffer[Thread] = new mutable.ListBuffer[Thread]

    for (i <- 0 until NO_CONSUMERS) {
      val consumerThread: Thread = new Thread(new EventConsumer(i + "", mongo, consumerThreadStatus, consumerCounter))
      consumerThread.start()
      consumerThreads += consumerThread
    }

//    val producerThreadStatus: AtomicBoolean = new AtomicBoolean(true)
//    val producerCounter: AtomicLong = new AtomicLong(0)
//    val producerThreads: mutable.ListBuffer[Thread] = new mutable.ListBuffer[Thread]

    //    val producerThread: Thread = new Thread(new EventProducer("1", mongo, producerThreadStatus, producerCounter))
//    producerThread.start()
//    producerThreads += producerThread
//
//    Thread.sleep(10 * 1000)
//    producerThreadStatus.set(false)
//    Thread.sleep(2 * 1000)
//
//    producerThreads.foreach((producerThread: Thread) =>
//      producerThread.interrupt()
//    )

    println("----- no of total events produced : " + MAX_DOCUMENTS)
    println("----- no of events consumed in total by all consumers = (events * no(consumers) ) = " + consumerCounter.get)
    println("======================================================")
  }
}

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
import eventStreaming.service.EventStreamService
import eventStreaming.util.{Util, MongoEventStreamUtil}
import eventStreaming.domain.Event

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object EventStreamApp {

  val eventStreamService = new EventStreamService

  @throws(classOf[Exception])
  def main(args: Array[String]) {
    val mongo: MongoClient = MongoEventStreamUtil.mongoInstance

    eventStreamService.initEventStream(mongo)

    //insert
    val s = new Date().getTime
    MongoEventStreamUtil.bulkInsertVersion3(mongo)
    Util.printLog(s"Took ${(new Date().getTime - s) / 1000} secs to insert ${EventStreamService.MAX_DOCUMENTS}", false)

    val consumerThreadStatus: AtomicBoolean = new AtomicBoolean(true)
    val consumerCounter: AtomicLong = new AtomicLong(0)
    val consumerThreads: mutable.ListBuffer[Thread] = new mutable.ListBuffer[Thread]

    MongoEventStreamUtil.consumerTypes.foreach(consumer => {
      val consumerThread: Thread = new Thread(new EventConsumer(consumer, mongo, consumerThreadStatus, consumerCounter))
      consumerThread.start()
      consumerThreads += consumerThread
    })

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

    println("----- no of total events produced : " + EventStreamService.MAX_DOCUMENTS)
    println("----- no of events consumed in total by all consumers = (events * no(consumers) ) = " + consumerCounter.get)
    println("======================================================")
  }
}

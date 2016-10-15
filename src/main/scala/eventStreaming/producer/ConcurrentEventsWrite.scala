package eventStreaming.producer

import java.util.Date
import java.util.concurrent.{ExecutorService, Executors}
import java.util.concurrent.atomic.AtomicLong

import eventStreaming.domain.Event
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, Collection, MongoDriver}
import reactivemongo.bson._

//import scala.actors.threadpool.{ThreadPoolExecutor}
import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by prayagupd
 * on 12/1/15.
 */


object ConcurrentEventsWrite {
  val Pool_Size = 5
  val Size = 50
  val executorService: ExecutorService = Executors.newFixedThreadPool(Pool_Size)
  val count : AtomicLong = new AtomicLong(Size)
  class EventStreamProducer extends Thread {

    val driver = new MongoDriver()
    val connection : MongoConnection = driver.connection(List("localhost"))
    val db : DefaultDB = connection("events-db")
    val collection : BSONCollection = db("EventStream2")

    def this(name : String) {
      this()
      setName(name)
    }

    override def run(): Unit = {
      val start = new Date().getTime


      val document = BSONDocument (
        Event.Created_At -> System.currentTimeMillis(),
        Event.from -> "ABC",
        Event.to -> "CBA",
        Event.Event -> "Message")

      val future = collection.insert(document)
      future.onComplete {
        case Failure(e) => {
          println(e.getMessage)
          //throw e
        }
        case Success(lastError) => {
          val time = new Date().getTime - start
          println(s"${this.getName} took $time ms => ${count.decrementAndGet()}")
        }
      }
    }
  }

  def main(args: Array[String]) {
        val start = new Date().getTime
        for(i <- 0 until Size) {
          executorService.submit(new EventStreamProducer(s"$i"))
        }
        while(count.get()!=0) {
          val time = new Date().getTime - start
          println(s"$time just to start 1000 inserts")
        }
        println("DONE")
    }
}

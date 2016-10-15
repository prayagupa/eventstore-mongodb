package eventStreaming.consumer

import scala.collection.mutable.ListBuffer

/**
 * Created by prayagupd
 * on 11/29/15.
 */

object NumberOfThreadsJavaCanHandle {
  val Threads = 1 * 1000
 def threads(): ListBuffer[Thread] = {
   val threads : ListBuffer[Thread] = new ListBuffer[Thread]()
   for(i <- 0 until Threads) {
     val thread = new Thread() {
       setName(s"consumer $i")
       override def run() {
         while(true) {
           Thread.sleep(20*1000)
           println(s"I'm consumer $i")
         }
       }
     }
     threads+=thread
   }

   threads
 }

  def main(args: Array[String]) {
      threads().foreach(thread => thread.start())
  }
}

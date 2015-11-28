package eventStreaming.util

import java.io
import java.io.FileWriter
import java.util.Date

/**
  * Created by prayagupd
  * on 11/26/15.
  */

object Util {

   def printLog(log: String, print: Boolean) = {
     if(print) { println(log) }
     writeToFile(s"event-stream.${new Date().getDate}.log", s"${log}")
   }

   def using[A <: {def close(): Unit}, B](param_writer: A)(func_write: A => B): B =
     try {
       func_write(param_writer)
     } finally {
       param_writer.close()
     }

   def writeToFile_(fileName:String, data:String) =
     using (new FileWriter(fileName)) { fileWriter =>
       fileWriter.write(data)
     }

   def writeToFile(fileName: String, data: String): Unit = {
     val fileWriter = new FileWriter(new io.File(fileName), true)
     try {
       //println(s"writing to ${fileName}")
       fileWriter.write(data)
       fileWriter.write("\n")
     } catch {
       case e: Exception =>
         println(s"error writing ${e.getMessage}")
       case _ : Throwable =>
         println(s"unknown error writing")
     } finally {
       fileWriter.close()
     }
   }

 }

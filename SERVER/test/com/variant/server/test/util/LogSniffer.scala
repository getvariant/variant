package com.variant.server.test.util

import play.api.Logger
import scala.io.Source
import scala.sys.process._
import java.nio.charset.Charset
import java.io.File
import scala.collection.mutable
import com.variant.core.UserError

/**
 * 
 */
object LogSniffer {
   
   /**
    * Get last n messages.
    * For simplicity, we ignore lines that don't parse, because they are
    * multi-line continuations of previous lines and so far we don't need that.
    * 
    */
   def last(n: Integer, fileName: String = "logs/application.log") = {
      
      val reader = new ReversedLinesFileReader(new File(fileName), Charset.defaultCharset())
      val result = mutable.Buffer[Entry]()
      var line = reader.readLine()
      var i = 0
      while (line != null && i < n) {
         try {
            result += new Entry(line)
            i += 1
         }
         catch {
            case e: Throwable => // Ignore
         }
         finally {
            line = reader.readLine()
         }
      }
      reader.close()
      result.toSeq
   }


   /**
    * Static Log Entry.
    */
   class Entry private[util] (line: String) {
      
      private[this] val tokens = line.split(" ", 9)
            
      val date = Entry.format.parse(tokens(0) + ' ' + tokens(1))
      
      val severity = {
         UserError.Severity.valueOf(tokens(2).substring(1, tokens(2).length - 1))
      }
      
      val message = tokens(8)
      
      override def toString = s"${Entry.format.format(date)} [${severity}] ${message}"
   }

   object Entry {
      val format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS")
   }
}

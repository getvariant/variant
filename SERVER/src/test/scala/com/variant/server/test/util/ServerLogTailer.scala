package com.variant.server.test.util

import scala.collection.JavaConverters._
import com.variant.core.util.LogTailer
import com.variant.core.util.LogTailer.Entry
import java.io.File
import java.nio.charset.Charset
import com.variant.core.util.apache.ReversedLinesFileReader
import com.typesafe.scalalogging.LazyLogging

/**
 * Repackaging of the core LogTailer class for the server side.
 */
object ServerLogTailer extends LazyLogging {

   object Level {
      val Trace = LogTailer.Level.TRACE
      val Debug = LogTailer.Level.DEBUG
      val Info = LogTailer.Level.INFO
      val Warn = LogTailer.Level.WARN
      val Error = LogTailer.Level.ERROR
   }

   def last(n: Integer, fileName: String = "log/variant.log"): Seq[Entry] = {

      logger.whenDebugEnabled {
         throw new RuntimeException("ServerLogTailer requires logging level of INFO")
      }
      
      LogTailer.last(n, fileName).asScala.toSeq
   }
}
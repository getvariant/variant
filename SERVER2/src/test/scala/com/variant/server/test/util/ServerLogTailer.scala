package com.variant.server.test.util

import scala.collection.JavaConverters._
import com.variant.core.util.LogTailer
import com.variant.core.util.LogTailer.Entry
import java.io.File
import java.nio.charset.Charset
import com.variant.core.util.apache.ReversedLinesFileReader

/**
 * Repackaging of the core LogTailer class for the server side.
 */
object ServerLogTailer {

   val Trace = LogTailer.Level.TRACE
   val Debug = LogTailer.Level.DEBUG
   val Info = LogTailer.Level.INFO
   val Warn = LogTailer.Level.WARN
   val Error = LogTailer.Level.ERROR

   def last(n: Integer, fileName: String = "log/variant.log"): Seq[Entry] = {

      LogTailer.last(n, fileName).asScala.toSeq
   }
}
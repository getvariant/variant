package com.variant.server.test.util

import scala.collection.JavaConverters._
import com.variant.core.util.LogTailer
import com.variant.core.util.LogTailer.Entry

/**
 * Repackaging of the core LogTailer class for the server side.
 */
object ServerLogTailer {
  
   def last(n: Integer, fileName: String = "log/variant.log"): Seq[Entry] = LogTailer.last(n, fileName).asScala.toSeq
}
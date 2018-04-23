package com.variant.server.test.util

import scala.collection.JavaConversions._;
import com.variant.core.util.LogTailer
import com.variant.core.util.LogTailer.Entry

/**
 * Repackaging of the core LogTailer class for the server side.
 */
object ServerLogTailer {
  
   def last(n: Integer, fileName: String = "logs/application.log"): Seq[Entry] = LogTailer.last(n, fileName).toSeq
}
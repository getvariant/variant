package com.variant.server.snippet

import net.liftweb._
import util.Helpers._
import java.lang.management.ManagementFactory
import scala.xml.NodeSeq
import scala.xml.Text
import org.apache.commons.lang3.time.DurationFormatUtils
import com.variant.server.core.VariantCore
/**
 * 
 */
object HealthCheck {
   
   /**
    * 
    */
   def render : NodeSeq = {
      Text(DurationFormatUtils.formatDuration((System.currentTimeMillis() - VariantCore.bootTime), "dd:HH:mm:ss.SSS"))
   }
   
}
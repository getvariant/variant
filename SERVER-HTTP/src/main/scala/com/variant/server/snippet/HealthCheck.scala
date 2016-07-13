package com.variant.server.snippet

import scala.xml.NodeSeq
import scala.xml.Text
import org.apache.commons.lang3.time.DurationFormatUtils
import net.liftweb.util.Helpers._
import com.variant.server.ServerBoot
/**
 * 
 */
object HealthCheck {
   
   /**
    * 
    */
   def render : NodeSeq = {
      Text(DurationFormatUtils.formatDuration((System.currentTimeMillis() - ServerBoot.bootTime), "dd:HH:mm:ss.SSS"))
   }
   
}
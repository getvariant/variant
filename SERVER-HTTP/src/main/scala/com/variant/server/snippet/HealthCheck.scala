package com.variant.server.snippet

import scala.xml.NodeSeq
import scala.xml.Text

import org.apache.commons.lang3.time.DurationFormatUtils

import com.variant.server.boot.ServerBoot

import net.liftweb.util.Helpers._
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
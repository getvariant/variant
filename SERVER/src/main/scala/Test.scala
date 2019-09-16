package com.variant.server.trace

object Test {

   def main(args: Array[String]) {

      import scala.concurrent.duration.FiniteDuration
      import java.util.concurrent.TimeUnit

      val duration = FiniteDuration(5, TimeUnit.SECONDS)
      println(duration.toString)
   }
}
package com.variant.server.test.spec

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

import scala.io.Source
import scala.sys.process._
import scala.sys.process.ProcessLogger
import scala.sys.process.stringSeqToProcess

import org.scalatest.BeforeAndAfterAll
import org.scalatest.MustMatchers

import com.typesafe.scalalogging.LazyLogging
import com.variant.server.test.util.ServerLogTailer
import com.variant.server.boot.ServerMessageLocal._
import com.variant.core.httpc.HttpRequest
import com.variant.server.test.util.ServerLogTailer.Level._

/**
 * All tests using a standalone server inherint from here.
 * Uses Postgres event fluser.
 */
object StandaloneServerSpec {
   private var sqlSchemaCreated = false
}

class StandaloneServerSpec extends BaseSpec with BeforeAndAfterAll {

   // Directory where the standalong server is built. Tests may overide this.
   protected lazy val serverDir = "/private/tmp/standaloneServer"

   // The flusher to use. Valid values: 'null', 'mysql', or 'postgres'. Tests may overide this.
   protected lazy val flusher = "none"

   // The standalone server
   protected val server = new StandaloneServer(serverDir, flusher)

   "Server must come up with exampleSchema schema" in {

      server.start()

      HttpRequest.get("http://localhost:5377/schema/exampleSchema").responseCode mustBe 200
   }

}

/**
 * Class represents a standalone server configured on the filesystem in serverDir directory.
 */
class StandaloneServer(serverDir: String, flusher: String) extends LazyLogging  {

   logger.info(s"Building server in ${serverDir}. Takes a few seconds...")

   // sbt, bless its soul, puts errors on standard out.
   val procLogger = ProcessLogger(
      l => if (l.matches(".*error.*")) logger.info("<OUT> " + l),
      l => logger.error("<ERR> " + l))

   // Build standalone server in serverDir.
   // Stdout is ignored. Stderr is sent to console.
   // Blocks until process terminates.
   // Remember that in test the current directory is test-base
   var cc = Seq("../standalone-server/build.sh", serverDir, flusher).!(procLogger)

   if (cc != 0)
      throw new RuntimeException(s"standaloneServer.sh crashed with cc [${cc}]")

   println(s"Built server in ${serverDir}")

   private[this] var svrProc: Option[Process] = None

   // Start server.
   val out = new LinkedBlockingQueue[String]()
   val err = new LinkedBlockingQueue[String]()

   /**
    * Start the server. Blocks until the server is confirmed to be up with both
    * status message in the log and the health page. If no server after given timeout
    * interval in seconds, throws a RuntimeException.
    *
    */
   def start(
      // Optional list of -Dkey=value params to be passed to variant in the form of a Map.
      //   e.g. Map("http.port" -> "1234") becomes -Dhttp.port=1234
      commandLineParams: Map[String, String] = Map(),
      // Optional timeout interval in seconds.
      timeout: Int = 5,
      // A callback to call in case of Timeout
      onTimeout: () => Unit = () => throw new RuntimeException(s"Timed out waiting for server to start")) {

      out.clear()
      err.clear()

      val svrLog = ProcessLogger(
         line => {
            println("<Server OUT> " + line)
            out.put(line)
         },
         line => {
            println("<Server ERR> " + line)
            err.put(line)
         })

      // Run server asynchronously
      var paramString = ""
      var port = "5377"

      commandLineParams.foreach { t =>
         paramString += " -D" + t._1 + "=" + t._2
         if (t._1 == "variant.http.port") port = t._2
      }
      svrProc = Some((serverDir + "/bin/variant start " + paramString).run(svrLog))

      // Block until we read the startup message.
      var started = false
      var failed = false
      while (!started && !failed) {
         val line = out.poll(timeout, TimeUnit.SECONDS)

         // If we timed out, do the onTimeout function
         // Otherwise, keep checking.
         if (line == null) {
            onTimeout()
            failed = true
         } else if (line.matches(".*\\[433\\].*")) started = true;
      }

      if (!failed) {
         // Confirm the server is running at this point.
         // Give the server time to bind to port -- happens last
         var timeoutMillis = 20000
         var waited = 0L
         var confirmed = false
         while (!confirmed && waited < timeoutMillis) {
            try {
               val health = Source.fromURL("http://localhost:" + port).mkString
               if (!health.startsWith("Variant AIM Server"))
                  throw new RuntimeException(s"Unexpected halth response from server [${health}]")
               confirmed = true;
            } catch {
               case t: Throwable =>
                  Thread.sleep(100)
                  waited += 100
            }
         }

         if (waited >= timeoutMillis)
            throw new RuntimeException(s"Timed out waiting for standalone server to come up after ${waited} millis")
      }
   }

   /**
    * Stop this server.
    * Stdout and stderr are sent to console.
    */
   def stop() {

      svrProc.foreach { proc =>

         (serverDir + "/bin/variant stop").!

         println(s"Waiting for server process to exit...")         
         val rc = proc.exitValue
         println(s"Server process completed with return code $rc")

         out.clear()
         err.clear()
         svrProc = None
      }
      // Let the server shutdown clean.
      // Thread.sleep(250)
   }
   
}

package com.variant.server.test.spec

import scala.sys.process.stringSeqToProcess
import scala.sys.process._
import scala.io.Source
import scala.collection.mutable
import org.scalatest.BeforeAndAfterAll
import com.variant.core.util.LogTailer.Entry
import scala.sys.process.ProcessLogger
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import com.variant.server.boot.VariantServer
import com.typesafe.scalalogging.LazyLogging
import com.variant.server.boot.ServerMessageLocal
import com.variant.core.httpc.HttpOperation

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
   protected lazy val flusher = "null"

   // The standalone server
   protected val server = new StandaloneServer(serverDir, flusher)

   "Server must come up with petclinic schema" in {

      server.start()

      HttpOperation.get("http://localhost:5377/connection/petclinic")
         .exec().responseCode mustBe 200
   }

   /**
    * Each test case runs in its own JVM. Each test runs in its
    * own instance of the test case. We want the jdbc schema
    * created only once per jvm, but the api be instance scoped.
    *
    * @throws Exception
    * Needed by the JUnit EventWriter test which is currently off.
    *
    * override def beforeAll() {
    * synchronized { // once per JVM
    * if (!sqlSchemaCreated) {
    * recreateDatabase()
    * sqlSchemaCreated = true
    * }
    * }
    * }
    */

   override def afterAll() {
      server.stop
   }

}

/**
 * Class represents a standalone server configured on the filesystem in serverDir directory.
 */
class StandaloneServer(serverDir: String, flusher: String) extends LazyLogging {

   logger.info(s"Building server in ${serverDir}. Takes a few seconds...")

   // sbt, bless its soul, puts errors on standard out.
   val procLogger = ProcessLogger(
      l => if (l.matches(".*error.*")) logger.info("<standaloneServer.sh OUT> " + l),
      l => logger.error("<standaloneServer.sh ERR> " + l))

   // Build standalone server in serverDir.
   // Stdout is ignored. Stderr is sent to console.
   // Blocks until process terminates.
   // Remember that in test the current directory is test-base
   var cc = Seq("../mbin/standaloneServer.sh", serverDir, flusher).!(procLogger)

   if (cc != 0)
      throw new RuntimeException(s"standaloneServer.sh crashed with cc [${cc}]")

   println(s"Built server in ${serverDir}")

   private[this] var svrProc: Process = _

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
      // Optional list of -Dkey=value params to be passed to variant.sh in the form of a Map.
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
      var port = "5377" // Default

      commandLineParams.foreach { t =>
         paramString += " -D" + t._1 + "=" + t._2
         if (t._1 == "http.port") port = t._2
      }
      svrProc = (serverDir + "/bin/variant.sh start " + paramString.toString).run(svrLog)

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

      (serverDir + "/bin/variant.sh stop").!

      // Wait until the server process exits.
      val svrExitStatus = svrProc.exitValue
      println(s"Server process completed with status ${svrExitStatus}")
      /* It's 143, because we kill in variant.sh
      if (svrExitStatus != null)
         throw new RuntimeException(s"Server process exited with error status ${svrExitStatus}")
         *
         */

      out.clear()
      err.clear()
      svrProc = null;

      // Let the server shutdown clean.
      // Thread.sleep(250)
   }
}

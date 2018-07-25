package com.variant.server.test

import scala.sys.process._
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import com.variant.server.boot.VariantServer
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.spec.StandaloneServerSpec
import scala.io.Source
import com.variant.server.util.httpc.HttpOperation
import com.variant.server.test.util.ServerLogTailer
import com.variant.core.impl.ServerError
import com.variant.core.UserError.Severity._
import com.variant.server.boot.ServerErrorLocal
/**
 * Test the server running in a separate process.
 */
class StandaloneServerTest extends StandaloneServerSpec {

   "Server" should {

      "send 404 on a bad request" in  {
         
         HttpOperation.get("http://localhost:5377/bad").exec()
            .getResponseCode mustBe 404

         HttpOperation.get("http://localhost:5377/variant/bad").exec()
            .getResponseCode mustBe 404

      }
    
      "send health on a root request" in  {
         val resp = HttpOperation.get("http://localhost:5377/variant").exec() 
         resp.getResponseCode mustBe 200
         resp.getStringContent must startWith (VariantServer.productName)
      }

      "fail to deploy petclinic without the postgres driver in ext/" in  {

         server.stop()
         s"rm ${serverDir}/ext/postgresql.postgresql-9.1-901-1.jdbc4.jar" ! ;
         server.start()
         val resp = HttpOperation.get("http://localhost:5377/variant/connection/petclinic_experiments").exec()
         resp.getResponseCode mustBe 400
         resp.getErrorContent mustBe ServerError.UnknownSchema.asMessage("petclinic_experiments")
         
         val lines = ServerLogTailer.last(7, serverDir + "/log/variant.log")
         println("***")
         println(lines)
         println("***")
         
         lines(0).severity mustBe ERROR
         lines(0).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.server.api.EventFlusherPostgres", "java.lang.reflect.InvocationTargetException")

         // This error goes in the log twice: the first instance has the call stack.
         lines(1).severity mustBe ERROR
         lines(1).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.server.api.EventFlusherPostgres", "java.lang.reflect.InvocationTargetException")

         lines(2).severity mustBe WARN
         lines(2).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("petclinic_experiments", s"${serverDir}/schemata/petclinic-experiments.json")

      }

      "Reploy petclinic after restoring postgres driver in ext/" in  {
         
         // Hot redeploy won't work. Not sure why, prob. class loader will refuse to look for the same class twice.

         server.stop()
         
         s"cp standalone-server/ext/postgresql.postgresql-9.1-901-1.jdbc4.jar ${serverDir}/ext/" ! ;

         server.start()
         
         HttpOperation.get("http://localhost:5377/variant/connection/petclinic_experiments")
            .exec().getResponseCode mustBe 200
         
      }

      
   }
   
}

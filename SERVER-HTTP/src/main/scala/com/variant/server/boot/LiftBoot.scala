package com.variant.server.boot

import net.liftweb.http.LiftRules
import net.liftweb.http.S
import com.variant.server.dispatch.Dispatcher
import net.liftweb.http.Bootable
import net.liftweb.http.provider.HTTPParam
import net.liftweb.http.Req
import com.typesafe.scalalogging.LazyLogging
import com.variant.server.ServerBoot
import org.apache.commons.lang3.time.DurationFormatUtils

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class LiftBoot extends Bootable with LazyLogging {
     
   /**
    * Lift calls this to boot us up.
    */
   def boot {   
      
      val now = System.currentTimeMillis();
      
      // Don't pipe /crossdomain.xml through Lift.
      LiftRules.liftRequest.append {
        case Req("crossdomain" :: Nil, "xml", _) => false
      }
      
      // where to search snippet
      LiftRules.addToPackages("com.variant.server")
    
      // API Dispatcher
      LiftRules.statelessDispatch.append(Dispatcher)
      
      LiftRules.supplementalHeaders.default.set(
         List(
            ("Access-Control-Allow-Origin", "*"),
            ("Access-Control-Allow-Credentials", "true")
         ))
      
      // Variant server
      ServerBoot.boot()
      val comptime = ServerBoot.getCore.getComptime
      logger.info(String.format(
				"%s Release %s © 2015-16 getvariant.com. Bootstrapped in %s. Listening on %s/", 
				comptime.getComponent(),
				comptime.getComponentVersion(),
				DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS"),
				LiftRules.context.path));
  }
}

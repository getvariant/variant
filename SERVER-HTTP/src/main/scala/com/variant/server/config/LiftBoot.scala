package com.variant.server.config

import net.liftweb.http.LiftRules
import com.variant.server.dispatch.Dispatcher
import net.liftweb.http.Bootable
import com.variant.core.Variant
import com.variant.server.core.VariantCore
import net.liftweb.http.provider.HTTPParam
import net.liftweb.http.Req

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class LiftBoot extends Bootable {
     
   /**
    * Lift calls this to boot us up.
    */
   def boot {   
      
      // Don't pipe /crossdomain.xml through Lift.
      LiftRules.liftRequest.append {
        case Req("crossdomain" :: Nil, "xml", _) => false
      }
      
      // API Dispatcher
      LiftRules.statelessDispatch.append(Dispatcher)
      
      LiftRules.supplementalHeaders.default.set(
         List(
            ("Access-Control-Allow-Origin", "*"),
            ("Access-Control-Allow-Credentials", "true")
         ))
       
      VariantCore.init()
  }
}

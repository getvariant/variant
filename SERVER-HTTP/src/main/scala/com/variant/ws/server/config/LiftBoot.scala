package com.variant.ws.server.config

import net.liftweb.http.LiftRules
import com.variant.ws.server.dispatch.Dispatcher
import net.liftweb.http.Bootable
import com.variant.core.Variant
import com.variant.ws.server.core.VariantCore
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
/*      
      LiftRules.supplementalHeaders.default.set(
         List(
            ("X-Lift-Version", LiftRules.liftVersion),
            ("Access-Control-Allow-Origin", "*"),
            ("Access-Control-Allow-Content-Type", "application/json"),
            ("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH"),
            ("Access-Control-Allow-Headers", "Cookie, Host, X-Forwarded-For, Accept-Charset, If-Modified-Since, Accept-Language, X-Forwarded-Port, Connection, X-Forwarded-Proto, User-Agent, Referer, Accept-Encoding, X-Requested-With, Authorization, Accept, Content-Type")
         ))
*/       
      VariantCore.init()
  }
}

package com.variant.ws.server.config

import net.liftweb.http.LiftRules
import com.variant.ws.server.dispatch.Dispatcher
import net.liftweb.http.Bootable
import com.variant.core.Variant

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class LiftBoot extends Bootable {
     
   /**
    * Lift calls this to boot us up.
    */
   def boot {   
      
      // API Dispatcher
      LiftRules.statelessDispatch.append(Dispatcher)
  }
}

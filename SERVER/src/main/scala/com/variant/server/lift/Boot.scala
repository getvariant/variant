package com.variant.server.lift

import net.liftweb.http.LiftRules
import com.variant.server.dispatch.Dispatcher
import net.liftweb.http.Bootable
import com.variant.core.Variant

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
object Boot {
   
   val variant = Variant.Factory.getInstance();
}

class Boot extends Bootable {
  
   
   def boot {
     
      // API Dispatcher
      LiftRules.statelessDispatch.append(Dispatcher)

      // Bootstrap Variant Core API.
      Boot.variant.bootstrap();

  }
}

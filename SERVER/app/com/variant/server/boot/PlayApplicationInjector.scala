package com.variant.server.boot

import javax.inject._
import play.api.Application

/**
 * Have Play inject currently running Application.
 *
trait ApplicationInjector {}

object PlayApplicationInjector {
   
   private var _playApp: Application = null
   
   def playApp = _playApp
}

//@Singleton
class PlayApplicationInjector @Inject() (application: Application) extends ApplicationInjector {
   
   println("*********** Injected Application")
   PlayApplicationInjector._playApp = application
}
*/
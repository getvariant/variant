package com.variant.server.play.action

import play.api.mvc._
import javax.inject.Inject
import scala.concurrent.ExecutionContext

/**
 * A disconnected action:
 * Starts and ends without a connection.
 */
class DisconnectedAction @Inject()
   (parser: BodyParsers.Default)
   (implicit ec: ExecutionContext) 
extends AbstractAction (parser) (ec) {}
            

package com.variant.server.test

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Application
import javax.inject.Inject
import com.variant.server.session.SessionStore
import org.scalatest.BeforeAndAfterAll
import com.variant.server.jdbc.JdbcService
import com.variant.server.event.EventWriter
import com.variant.server.event.EventWriter
import com.variant.server.boot.VariantServer
import com.variant.core.session.CoreSessionImpl
import com.variant.server.session.ServerSession
import com.variant.core.session.SessionScopedTargetingStabile
import com.variant.core.schema.Schema
import java.util.Random
import com.variant.core.event.impl.util.VariantStringUtils

/**
 * Sets schemaDir use the directory that contains good schema 
 */
abstract class BaseSpecWithSchema extends AbstractBaseSpec {
   
   override val schemasDir = "/test-schemas"

}

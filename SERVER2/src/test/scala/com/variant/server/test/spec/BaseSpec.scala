package com.variant.server.test.spec

import java.util.Random

import org.scalatest.MustMatchers
import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures

import com.variant.core.util.StringUtils
import com.variant.core.session.SessionScopedTargetingStabile
import com.variant.server.impl.SessionImpl
import com.variant.core.schema.Schema
import com.variant.server.api.Session

/**
 * No Server.
 * NOTE: All tests run with current directory set at `test-base`
 */
trait BaseSpec extends WordSpec with MustMatchers with ScalaFutures {

   /**
    * Generate a new random session ID.
    */
   protected def newSid() =
      StringUtils.random64BitString(new Random())

   /**
    * Create and add a targeting stabile to a session.
    */
   protected def setTargetingStabile(ssn: Session, experiences: String*) {
      val stabile = new SessionScopedTargetingStabile
      experiences.foreach { e => stabile.add(experience(e, ssn.getSchema)) }
      ssn.asInstanceOf[SessionImpl].coreSession.setTargetingStabile(stabile);
   }

   /**
    * Find experience object by its comma separated name.
    */
   protected def experience(name: String, schema: Schema) = {
      val tokens = name.split("\\.")
      assert(tokens.length == 2)
      schema.getVariation(tokens(0)).get.getExperience(tokens(1)).get
   }

}
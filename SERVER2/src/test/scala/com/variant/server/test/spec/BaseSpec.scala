package com.variant.server.test.spec

import java.util.Random

import org.scalatest.MustMatchers
import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures

import com.variant.core.util.StringUtils

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

}
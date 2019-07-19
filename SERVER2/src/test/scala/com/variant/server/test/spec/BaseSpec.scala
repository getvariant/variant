package com.variant.server.test.spec

import org.scalatest.MustMatchers
import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures

/**
 * No Server.
 * NOTE: All tests run with current directory set at `test-base`
 */
trait BaseSpec extends WordSpec with MustMatchers with ScalaFutures
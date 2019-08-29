package com.variant.server.test.spec

import org.scalatest.MustMatchers
import org.scalatest.OptionValues
import org.scalatest.WordSpec

/**
 * Specs that PlaySpec is based on, but without Play itself.
 */
trait ServerlessSpec extends WordSpec with MustMatchers with OptionValues
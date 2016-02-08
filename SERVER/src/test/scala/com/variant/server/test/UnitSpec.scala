package com.variant.server.test

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.OptionValues
import org.scalatest.Inside
import org.scalatest.Inspectors

abstract class UnitSpec extends FlatSpec 
   with Matchers with OptionValues with Inside with Inspectors
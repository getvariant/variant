package com.variant.server.test.spec

import org.scalatest.WordSpec
import org.scalatest.MustMatchers
import org.scalatest.concurrent.ScalaFutures
import akka.http.scaladsl.testkit.ScalatestRouteTest

trait BaseSpec extends WordSpec with MustMatchers with ScalaFutures with ScalatestRouteTest
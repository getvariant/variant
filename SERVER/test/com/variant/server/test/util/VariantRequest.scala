package com.variant.server.test.util

import play.api.test.Helpers._
import play.api.test.FakeRequest
import com.variant.core.util.Constants

object VariantRequest {
  
   def apply(method: String, uri: String, connId: String) = {
      FakeRequest(method, uri)
         .withHeaders("Content-Type" -> "text/plain", Constants.HTTP_HEADER_CONNID -> connId)
   }
}
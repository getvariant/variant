package com.variant.server.runtime

import scala.collection.JavaConversions._
import com.variant.core.schema.State
import com.variant.core.schema.Test.Experience
import com.variant.server.boot.VariantServer
import java.util.Collection
import com.variant.core.schema.impl.StateVariantImpl
import scala.collection.mutable.ArrayBuffer

/**
 * Test facade extends scope of non-public methods to public for testability.
 */
object RuntimeTestFacade {
    def apply(server: VariantServer) = new RuntimeTestFacade(server)  
}

class RuntimeTestFacade(server: VariantServer)  extends Runtime(server) {
   def resolveState(state: State, vector: Array[Experience]): (Boolean, StateVariantImpl) = {
      val result = super.resolveState(state, vector.toSeq)
      return (result._1(), result._2())
   }
}
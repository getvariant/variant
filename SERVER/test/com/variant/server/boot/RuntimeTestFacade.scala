package com.variant.server.boot

import java.util.ArrayList
import scala.collection.JavaConversions.seqAsJavaList
import com.variant.core.schema.State
import com.variant.core.schema.Test
import com.variant.core.schema.Test.Experience
import com.variant.core.schema.impl.StateVariantImpl
import com.variant.server.schema.SchemaGen

/**
 * Test facade extends scope of non-public methods to public for testability.
 */
object RuntimeTestFacade {
    def apply(schemaGen: SchemaGen) = new RuntimeTestFacade(schemaGen)  
}

class RuntimeTestFacade(schemaGen: SchemaGen)  extends Runtime(schemaGen) {

   /**
    * 
    */
   def resolveState(state: State, vector: Array[Experience]): (Boolean, StateVariantImpl) = {
      val result = super.resolveState(state, vector.toSeq)
      (result._1(), result._2())
   }
   
   /**
    * 
    */
   def isResolvable(vector: Array[Experience]): Boolean = {
      super.isResolvable(vector.toSeq)
   }
   
   /**
    * Second argument may change as a side effect, so must be mutable.
    * Take Scala types as params because of the consice way the caller can instantiate them,
    * but here we convert to a Java collection because Scala iterators don't support the remove()
    * mothod which is used in the underlying implementation.
    */
   def minUnresolvableSubvector(v: Array[Experience], w: Array[Experience]) = {
      super.minUnresolvableSubvector(new ArrayList(v.toSeq),new ArrayList(w.toSeq)).toArray()
   }
   
   /*
    * 
    */
   def isTargetable(test: Test, state: State,  alreadyTargetedExperiences: Array[Experience]): Boolean = {
      super.isTargetable(test, state, alreadyTargetedExperiences.toSeq)
   }
}
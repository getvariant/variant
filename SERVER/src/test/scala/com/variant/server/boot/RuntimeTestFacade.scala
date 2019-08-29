package com.variant.server.boot

import java.util.ArrayList

import scala.collection.JavaConverters._

import com.variant.core.schema.State
import com.variant.core.schema.StateVariant
import com.variant.core.schema.Variation
import com.variant.core.schema.Variation.Experience
import com.variant.server.schema.SchemaGen
import scala.collection.mutable.ArrayBuffer

/**
 * Test facade extends scope of non-public methods to public for testability.
 */
object RuntimeTestFacade {
   def apply(schemaGen: SchemaGen) = new RuntimeTestFacade(schemaGen)
}

class RuntimeTestFacade(schemaGen: SchemaGen) extends Runtime(schemaGen) {

   /**
    *
    */
   def resolveState(state: State, coordinates: Experience*): java.util.Optional[StateVariant] = {
      super.resolveState(state, coordinates.asJava)
   }

   /**
    *
    */
   def isResolvable(coordinates: Experience*): Boolean = {
      super.isResolvable(coordinates.asJava)
   }

   /**
    * Second argument may change as a side effect, so must be mutable.
    * Take Scala types as params because of the consice way the caller can instantiate them,
    * but here we convert to a Java collection because Scala iterators don't support the remove()
    * mothod which is used in the underlying implementation.
    */
   def minUnresolvableSubvector(v: Array[Experience], w: Array[Experience]) = {
      super.minUnresolvableSubvector(toJavaList(v), toJavaList(w)).toArray()
   }

   /*
    *
    */
   def isTargetable(test: Variation, state: State, alreadyTargetedExperiences: Array[Experience]): Boolean = {
      super.isTargetable(test, state, bufferAsJavaList(ArrayBuffer(alreadyTargetedExperiences: _*)))
   }

   /* I can't figure out how to convert immutable scala Array to a mutable java List
    * using the JavaConverters routines, so writing my own.
    */
   private[this] def toJavaList[V](arr: Array[V]): java.util.List[V] = {
      val result = new java.util.ArrayList[V]()
      arr.foreach { result.add(_) }
      result
   }
}
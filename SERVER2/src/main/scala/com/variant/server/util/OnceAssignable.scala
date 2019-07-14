package com.variant.server.util

/**
 * A value of type T, which can be reassigned at most once.
 * Optionally, can be created with an initial value, in which case
 * may still be reassigned once.
 */
class OnceAssignable[T](initValue: T) {

   def this() {
      this(null.asInstanceOf[T])
   }

   private[this] var assignmentCount = 0
   private[this] var _value: T = initValue

   // Call this at most once.
   def <=(newVal: T) {
      if (assignmentCount == 0) {
         assignmentCount == 1
         _value = newVal
      } else
         throw new RuntimeException(s"Value already set [${_value}]")
   }

   def get = _value
}

object OnceAssignable {

   def apply[T]() = new OnceAssignable[T]()

   def apply[T](newValue: T) = new OnceAssignable(newValue)

}
package com.variant.server.util

object JavaImplisits {
  
   implicit def оptional2Option[T](optional: java.util.Optional[T]): Option[T] = {
      optional.orElse(None)
   }
}
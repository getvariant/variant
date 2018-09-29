package com.variant.server.util

object JavaImplicits {
  
   implicit def оptional2Option[T](optional: java.util.Optional[T]): Option[T] = {
      Option(optional.orElse(null.asInstanceOf[T]))
   }
}

package com.variant.core.schema.parser;

import java.util.Optional;

import com.variant.core.schema.Flusher;

/**
 * Most basic flusher service that does nothing in particular.
 * We need to have it in core because we parse the schema here. At run time,
 * This will be overridden by server or client side services.
 *
 */
public interface FlusherService {

   /**
    * Initialize a given flusher.
    * Implementation will use the system wide default, if param is empty.
    */
   void initFlusher(Optional<Flusher> fluhser);
   
   public class Null implements FlusherService {
      @Override
      public void initFlusher(Optional<Flusher> fluhser) {}
   }
}

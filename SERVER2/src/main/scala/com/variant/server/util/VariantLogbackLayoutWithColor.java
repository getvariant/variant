package com.variant.server.util;

import ch.qos.logback.classic.Level;

/**
 * Custom logback layout which will always abbreviate "com.variant.server.some.thing.Else"
 * to "c.v.s.some.thing.Else"
 * 
 * @author Igor
 *
 */
public class VariantLogbackLayoutWithColor  extends VariantLogbackLayout {
	
   public static final String ANSI_RESET = "\u001B[0m";
   public static final String ANSI_BLACK = "\u001B[30m";
   public static final String ANSI_RED = "\u001B[31m";
   public static final String ANSI_GREEN = "\u001B[32m";
   public static final String ANSI_YELLOW = "\u001B[33m";
   public static final String ANSI_BLUE = "\u001B[34m";
   public static final String ANSI_PURPLE = "\u001B[35m";
   public static final String ANSI_CYAN = "\u001B[36m";
   public static final String ANSI_WHITE = "\u001B[37m";

   @Override
   protected String colorizeLevel(Level level) {
      return level == Level.WARN || level == Level.ERROR?  ANSI_RED + level.toString() + ANSI_RESET : level.toString();
   }

}
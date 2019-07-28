package com.variant.server.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.variant.core.Constants;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.LayoutBase;

/**
 * Custom logback layout which will always abbreviate "com.variant.server.some.thing.Else"
 * to "c.v.s.some.thing.Else"
 * 
 * @author Igor
 *
 */
public class VariantLogbackLayout extends LayoutBase<ILoggingEvent> implements Constants {

   private static final SimpleDateFormat df = new SimpleDateFormat(LOGGER_DATE_FORMAT);
	   
   public String doLayout(ILoggingEvent event) {
      
      StringBuffer sbuf = new StringBuffer(128);
      sbuf.append(df.format(new Date(event.getTimeStamp())));
      sbuf.append(" ");
      sbuf.append(colorizeLevel(event.getLevel()));
      sbuf.append(" - ");
    
      String[] tokens =  event.getLoggerName().split("\\.");
      for (int i = 0; i < tokens.length; i++) {
         if (i > 0) sbuf.append(".");
         sbuf.append(i < 3 ? tokens[i].charAt(0) : tokens[i]);
      }
      sbuf.append(" - ");
      sbuf.append(event.getFormattedMessage());
      sbuf.append(System.lineSeparator());
      
      // If this event has an error stack to display.
      IThrowableProxy throwbleProxy = event.getThrowableProxy();
      if (throwbleProxy != null) {
          String throwableStr = ThrowableProxyUtil.asString(throwbleProxy);
          sbuf.append(throwableStr);
          sbuf.append(System.lineSeparator());
      }

      return sbuf.toString();
   }
   
   /**
    * Do nothing for colorization here, but the colorizing subclass will override.
    * @param level
    * @return
    */
   protected String colorizeLevel(Level level) {
      return level.toString();
   }
}
package com.variant.server.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

/**
 * Custom logback layout which will always abbreviate "com.variant.server.some.thing.Else"
 * to "c.v.s.some.thing.Else"
 * 
 * @author Igor
 *
 */
public class VariantLogbackLayout  extends LayoutBase<ILoggingEvent> {

  public String doLayout(ILoggingEvent event) {
    StringBuffer sbuf = new StringBuffer(128);
    sbuf.append(event.getTimeStamp() - event.getTimeStamp());
    sbuf.append(" ");
    sbuf.append(event.getLevel());
    sbuf.append(" ");
    
    String[] tokens =  event.getLoggerName().split("\\.");
    for (int i = 0; i < tokens.length; i++) {
    	if (i > 0) sbuf.append(".");
    	sbuf.append(i < 3 ? tokens[i].charAt(0) : tokens[i]);
    }
    sbuf.append(" - ");
    sbuf.append(event.getFormattedMessage());
    sbuf.append(System.lineSeparator());
    return sbuf.toString();
  }
}
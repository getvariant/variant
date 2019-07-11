package com.variant.server.util;

import java.text.SimpleDateFormat;
import java.util.Date;

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

	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
  public String doLayout(ILoggingEvent event) {
    StringBuffer sbuf = new StringBuffer(128);
    sbuf.append(df.format(new Date(event.getTimeStamp())));
    sbuf.append(" ");
    sbuf.append(event.getLevel());
    sbuf.append(" - ");
    
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
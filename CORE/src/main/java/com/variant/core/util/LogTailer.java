package com.variant.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.variant.core.Constants;
import com.variant.core.util.apache.ReversedLinesFileReader;

public class LogTailer {
	   
   public static enum Level {
      TRACE, DEBUG, INFO, WARN, ERROR
   }
   
	/**
    * Get last n messages in chronological order.
    * For simplicity, we ignore lines that don't parse, because they are
    * multi-line continuations of previous lines and so far we don't need that.
    * @throws IOException 
    * 
    */
   public static List<Entry> last(int lines, String fileName) throws IOException {
      
      
	   ReversedLinesFileReader reader = new ReversedLinesFileReader(new File(fileName), Charset.defaultCharset());
	   List<Entry> result = new ArrayList<Entry>();
	   int i = 0;
	   String line;
      while ((line = reader.readLine()) != null && i < lines) {
         // Prepend, so that result is back in chronological order.
        	 try {
        	    result.add(0, new Entry(line)); 
             i++;
        	 } catch (ParseException pe) {}  // Simply skip lines that don't parse. 
      }
      reader.close();
      return result;
   }


   /**
    * Static Log Entry.
    */
   public static class Entry implements Constants {
      
	   private static DateFormat DATE_FORMAT = new SimpleDateFormat(LOGGER_DATE_FORMAT);
	   
	   public final Date date;
	   public final Level level;
	   public final String klass;
	   public final String message;

	   // Don't swallow the parse exception here because we want the caller to skip lines that don't parse.
	   private Entry (String line) throws ParseException  {
		   String[] tokens = line.split(" ", 7);
		   if (tokens.length != 7)
		      throw new ParseException("Too few tokens -- skip this line.", 0);
		   date = DATE_FORMAT.parse(tokens[0] + ' ' + tokens[1]);
		   level = Level.valueOf(tokens[2]);
		   klass = tokens[4];
		   message = tokens[6];
	   }
      
	   @Override
	   public String toString() {
		   return String.format("%s %s %s %s", DATE_FORMAT.format(date), level, klass, message);
	   }
   }

}

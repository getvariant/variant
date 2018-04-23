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

import com.variant.core.UserError;
import com.variant.core.util.apache.ReversedLinesFileReader;

public class LogTailer {
	   
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
         try {
        	// Prepend, so that result is back in chronological order.
        	 result.add(0, new Entry(line)); 
             i++;
         }
         catch (Throwable t) { /* Ignore */ }
      }
      reader.close();
      return result;
   }


   /**
    * Static Log Entry.
    */
   public static class Entry {

	   private static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
	   
	   public final Date date;
	   public final UserError.Severity severity;
	   public final String message;
	   
	   private Entry (String line) throws ParseException {
		   String[] tokens = line.split(" ", 9);
		   date = DATE_FORMAT.parse(tokens[0] + ' ' + tokens[1]);
		   severity = UserError.Severity.valueOf(tokens[2].substring(1, tokens[2].length() - 1));
		   message = tokens[8];
	   }
      
	   @Override
	   public String toString() {
		   return String.format("%s [%s] %s", DATE_FORMAT.format(date), severity, message);
	   }
   }

}

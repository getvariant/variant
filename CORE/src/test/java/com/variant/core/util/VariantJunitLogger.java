package com.variant.core.util;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.Marker;

public class VariantJunitLogger implements Logger {

	public enum Level {
	
		TRACE, DEBUG, INFO, WARN, ERROR;
		
		public boolean lessThan(Level other) {return ordinal() < other.ordinal();}
	}

	/**
	 * 
	 */
	public class Entry {
		
		private String msg;
		private Level level;
		private Throwable throwable;
				
		private Entry(Level level, String msg, Throwable t) {
			this.level = level;
			this.msg = msg;
			this.throwable = t;
		}
		
		public Level getLevel() {return level;}
		public String getMessage() {return msg;}
		public Throwable getThrowable() {return throwable;}
	}
	
	private LinkedList<Entry> entries = new LinkedList<Entry>();
	private PrintStream out;
	
	/**
	 * 
	 * @param level
	 * @param msg
	 * @param throwable
	 */
	private void addEntry(Level level, String msg, Throwable throwable) {
		entries.add(new Entry(level, msg, throwable));
		StringBuilder sb = new StringBuilder();
		sb.append(level).append(": ").append(msg);
		if (throwable != null) {
			sb.append("\n").append(throwable.toString());
			throwable.printStackTrace(out);
		}
		sb.append("\n");
		out.println(sb.toString());
	}
	/**
	 * 
	 * @param out
	 */
	public VariantJunitLogger(PrintStream out) {
		this.out = out;
	}
	
	/**
	 * 
	 * @param pos can be positive, negative or zero.
	 * @return for non-negative pos, 0 is the oldest, 1 — second oldest, etc.
	 *         for negative pos, -1 is the most recent, -2 — second most recent, etc.
	 *         null if no such element.
	 */
	public Entry get(int pos) {
		if (pos >= 0) return entries.get(pos);
		int index = 0;
		for (Iterator<Entry> iter = entries.descendingIterator(); iter.hasNext();) {
			Entry e = iter.next();
			if (--index == pos) return e;
		}
		return null;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public void debug(String arg0) {
		addEntry(Level.DEBUG, arg0, null);
	}

	@Override
	public void debug(String arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void debug(String arg0, Object... arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void debug(String arg0, Throwable arg1) {
		addEntry(Level.DEBUG, arg0, arg1);
	}

	@Override
	public void debug(Marker arg0, String arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void debug(String arg0, Object arg1, Object arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void debug(Marker arg0, String arg1, Object arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void debug(Marker arg0, String arg1, Object... arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void debug(Marker arg0, String arg1, Throwable arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void debug(Marker arg0, String arg1, Object arg2, Object arg3) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void error(String arg0) {
		addEntry(Level.ERROR, arg0, null);
	}

	@Override
	public void error(String arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void error(String arg0, Object... arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void error(String arg0, Throwable arg1) {
		addEntry(Level.ERROR, arg0, arg1);
	}

	@Override
	public void error(Marker arg0, String arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void error(String arg0, Object arg1, Object arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void error(Marker arg0, String arg1, Object arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void error(Marker arg0, String arg1, Object... arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void error(Marker arg0, String arg1, Throwable arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void error(Marker arg0, String arg1, Object arg2, Object arg3) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void info(String arg0) {
		addEntry(Level.INFO, arg0, null);
	}

	@Override
	public void info(String arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void info(String arg0, Object... arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void info(String arg0, Throwable arg1) {
		addEntry(Level.INFO, arg0, arg1);
	}

	@Override
	public void info(Marker arg0, String arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void info(String arg0, Object arg1, Object arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void info(Marker arg0, String arg1, Object arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void info(Marker arg0, String arg1, Object... arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void info(Marker arg0, String arg1, Throwable arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void info(Marker arg0, String arg1, Object arg2, Object arg3) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDebugEnabled() {
		return true;
	}

	@Override
	public boolean isDebugEnabled(Marker arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isErrorEnabled() {
		return true;
	}

	@Override
	public boolean isErrorEnabled(Marker arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInfoEnabled() {
		return true;
	}

	@Override
	public boolean isInfoEnabled(Marker arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isTraceEnabled() {
		return true;
	}

	@Override
	public boolean isTraceEnabled(Marker arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isWarnEnabled() {
		return true;
	}

	@Override
	public boolean isWarnEnabled(Marker arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void trace(String arg0) {
		addEntry(Level.TRACE, arg0, null);
	}

	@Override
	public void trace(String arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void trace(String arg0, Object... arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void trace(String arg0, Throwable arg1) {
		addEntry(Level.TRACE, arg0, arg1);
	}

	@Override
	public void trace(Marker arg0, String arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void trace(String arg0, Object arg1, Object arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void trace(Marker arg0, String arg1, Object arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void trace(Marker arg0, String arg1, Object... arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void trace(Marker arg0, String arg1, Throwable arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void trace(Marker arg0, String arg1, Object arg2, Object arg3) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void warn(String arg0) {
		addEntry(Level.WARN, arg0, null);
	}

	@Override
	public void warn(String arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void warn(String arg0, Object... arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void warn(String arg0, Throwable arg1) {
		addEntry(Level.WARN, arg0, arg1);
	}

	@Override
	public void warn(Marker arg0, String arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void warn(String arg0, Object arg1, Object arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void warn(Marker arg0, String arg1, Object arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void warn(Marker arg0, String arg1, Object... arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void warn(Marker arg0, String arg1, Throwable arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void warn(Marker arg0, String arg1, Object arg2, Object arg3) {
		throw new UnsupportedOperationException();
	}

}

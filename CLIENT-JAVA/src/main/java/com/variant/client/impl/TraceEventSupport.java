package com.variant.client.impl;

import java.util.HashMap;
import java.util.Map;

import com.variant.client.TraceEvent;
import com.variant.core.util.CollectionsUtils;

abstract public class TraceEventSupport implements TraceEvent {
	
	protected Map<String, String> attributes = new HashMap<String, String>();
	protected String name;
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
			
	/**
	 */
	public TraceEventSupport(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Map<String,String> getAttributes() {
		return attributes;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                  Static Factory Methods                                     //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Factory method returns a blank custom trace event with a given name. This event can be triggered by passing
	 * it to <code>Session.triggerTraceEvent()</code>.
	 * @param name Name of the trace event to be created
	 * @return an implementation of this interface.
	 * 
	 *  @since 0.9
	 */
	static public TraceEvent mkTraceEvent(String name) {
		return new TraceEventSupport(name) {};
	}

	/**
	 * Factory method returns a custom trace event with a given name and event attributes. This event can be triggered by passing
	 * it to <code>Session.triggerTraceEvent()</code>.
	 * @param name Name of the trace event to be created
	 * @return an implementation of this interface.
	 * 
	 *  @since 0.9
	 */
	static public TraceEvent mkTraceEvent(String name, Map<String,String> attributes) {
		TraceEvent result = mkTraceEvent(name);
		result.getAttributes().putAll(attributes);
		return result;
	}

   @Override
   public boolean equals(Object other) {
      if (! (other instanceof TraceEvent)) return false;
      TraceEvent o = (TraceEvent) other;
      return name.equals(o.getName()) && CollectionsUtils.equalAsSets(attributes, o.getAttributes());      
   }

}
package com.variant.core.event.impl;
/*
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.variant.core.event.VariantEvent;
import com.variant.core.event.VariantEventVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

/**
 * EVENT_VARIANTS DAO.
 * 
 * @author Igor.
 *
public class VariantEventVariantSupport implements VariantEventVariant {
	
	private long id;
	private VariantEvent event;
	private Test.Experience experience;
	protected Map<String, Object> params = new HashMap<String, Object>();

	protected VariantEventVariantSupport(VariantEvent event, Experience experience) {
		this.event = event;
		this.experience = experience;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public VariantEvent getEvent() {
		return event;
	}
	
	@Override
	public Experience getExperience() {
		return experience;
	}
	
	@Override
	public Object setParameter(String key, Object value) {
		return params.put(key, value);
	}

	@Override
	public Object getParameter(String key) {
		return params.get(key);
	}
	
	@Override
	public Set<String> getParameterKeys() {
		return params.keySet();
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param id
	 *
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * 
	 * @return
	 *
	@Override
	public String toString() {
		
		return new StringBuilder()
		.append('{')
		.append("eventId:'").append(getEvent().getId()).append("', ")
		.append("experience:'").append(getExperience()).append("', ")
		.append("isExperienceControl:").append(getExperience().isControl())
		.append("}").toString();

	}

}
*/
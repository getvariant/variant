package com.variant.core.impl;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.core.Variant;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.VariantTargetingTracker;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.impl.StateVisitedEvent;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.session.VariantSessionImpl;

/**
 * 
 * @author Igor
 *
 */
public class VariantStateRequestImpl implements VariantStateRequest, Serializable {

	/**
	 * Needs serializable because we keep it in session.
	 */
	private static final long serialVersionUID = 1L;
	
	private VariantSessionImpl session;	
	private State state;
	private Status status = Status.OK;
	private Map<String,String> resolvedParameterMap;
	private StateVisitedEvent event = null;
	private boolean committed = false;
	private VariantTargetingTracker targetingTracker = null;
	
	// This doesn't change over the life of a request, so we'll only compute this once.
	private Collection<Experience> targetedExperiencesCache; 
		
	/**
	 * 
	 * @param session
	 */
	VariantStateRequestImpl(VariantSessionImpl session, StateImpl state) {
		this.session = session;
		this.state = state;
		session.setStateRequest(this);
	}
	
	/**
	 * 
	 * @param targetingPersister
	 */
	void setTargetingPersister(VariantTargetingTracker targetingPersister) {
		this.targetingTracker = targetingPersister;
	}
	
	/**
	 *  Crate state visited event when appropriate.
	 */
	void createStateVisitedEvent() {
		event = new StateVisitedEvent(state);
	}
	
	
	/**
	 * Commit this state request and flush the state visited event to an implementation of EventPersister. 
	 */
	public void commit() {

		// We won't have an event if nothing is instrumented on this state
		if (event != null) {
			// The status of this request.
			event.getParameterMap().put("REQ_STATUS", status);
			// log all resolved state params as event params.
			for (Map.Entry<String,String> e: resolvedParameterMap.entrySet()) {
				event.getParameterMap().put(e.getKey(), e.getValue());				
			}
			// Flush state visited event
			session.triggerEvent(event);
			event = null;
		}
		
		committed = true;
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public VariantSession getSession() {
		return session;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public Map<String,String> getResolvedParameterMap() {
		return resolvedParameterMap;
	}


	@Override
	public VariantTargetingTracker getTargetingTracker() {
		return targetingTracker;
	}

	@Override
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public VariantEvent getStateVisitedEvent() {		
		return event;
	}

	@Override
	public Collection<Experience> getTargetedExperiences() {
		
		if (targetedExperiencesCache == null) {
			ArrayList<Experience> result = new ArrayList<Experience>();
			for (Test test: state.getInstrumentedTests()) {
				if (!test.isOn() || session.isDisqualified(test)) continue;
				Experience e = targetingTracker.get(test);
				if (e == null) throw new VariantInternalException("Experience for test [" + test.getName() + "] not found in targeting tracker.");
				result.add(e);
			}
			targetedExperiencesCache = result;
		}
		return targetedExperiencesCache;
	}

	@Override
	public Experience getTargetedExperience(Test test) {
		
		boolean found = false;
		
		for (Test t: state.getInstrumentedTests()) {

			if (!t.equals(test)) continue;
			found = true;
			if (!t.isOn() || session.isDisqualified(test)) continue;
			
			Experience e = targetingTracker.get(test);
			if (e == null) throw new VariantInternalException("Experience for test [" + test.getName() + "] not found in targeting tracker.");
			return e;
		}
		
		if (!found) throw new VariantRuntimeException(MessageTemplate.RUN_STATE_NOT_INSTRUMENTED_FOR_TEST, state.getName(), test.getName());

		return null;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param path
	 */
	public void setResolvedParameters(Map<String,String> parameterMap) {
		this.resolvedParameterMap = parameterMap;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isCommitted() {
		return committed;
	}
	
	/**
	 * 
	 * @return
	 */
	public Status getStatus() {
		return status;
	}

	private static final String FIELD_NAME_STATE = "state";
	private static final String FIELD_NAME_STATUS = "status";
	private static final String FILED_NAME_COMMITTED = "comm";
	private static final String FIELD_NAME_PARAMS = "params";
	private static final String FIELD_NAME_KEY = "key";
	private static final String FIELD_NAME_VALUE = "val";
	private static final String FIELD_NAME_EXPERIENCES = "exps";
	
	/**
	 * Serialize to JSON.
	 * @return
	 * @throws Exception
	 */
	public String toJson() throws Exception {
		StringWriter result = new StringWriter(2048);
		JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
		jsonGen.writeStartObject();
		jsonGen.writeStringField(FIELD_NAME_STATE, state.getName());
		jsonGen.writeStringField(FIELD_NAME_STATUS, status.toString());
		jsonGen.writeBooleanField(FILED_NAME_COMMITTED, committed);
		if (resolvedParameterMap.size() > 0) {
			jsonGen.writeArrayFieldStart(FIELD_NAME_PARAMS);
			for (Map.Entry<String,String> e: resolvedParameterMap.entrySet()) {
				jsonGen.writeStartObject();
				jsonGen.writeStringField(FIELD_NAME_KEY, e.getKey());
				jsonGen.writeStringField(FIELD_NAME_VALUE, e.getValue());
				jsonGen.writeEndObject();
			}
			jsonGen.writeEndArray();
		}
		
		Collection<Experience> targetedExperiences = getTargetedExperiences();
		if (targetedExperiences.size() > 0) {
			jsonGen.writeArrayFieldStart(FIELD_NAME_EXPERIENCES);
			for (Experience e: targetedExperiences) {
				jsonGen.writeString(e.toString());
			}
			jsonGen.writeEndArray();
		}

		jsonGen.writeEndObject();
		jsonGen.flush();
		return result.toString();
	}

	/**
	 * Deserialize from a raw map produced by parsing JSON.
	 * @param session
	 * @param fields
	 * @return
	 */
	public static VariantStateRequestImpl fromJson(Variant coreApi, VariantSessionImpl session, Map<String,?> fields) {
		
		Object stateName = fields.get(FIELD_NAME_STATE);
		if (stateName == null) 
			throw new VariantInternalException("Unable to deserialzie request: no state");
		if (!(stateName instanceof String)) 
			throw new VariantInternalException("Unable to deserialzie request: state not string");
		
		StateImpl state = (StateImpl) coreApi.getSchema().getState((String)stateName);
		
		if (state == null)
			throw new VariantInternalException("Unable to deserialzie request: state [" + stateName + "] not in schema");
		
		VariantStateRequestImpl result = new VariantStateRequestImpl(session, state);
		
		Object statusStr = fields.get(FIELD_NAME_STATUS);
		if (statusStr == null) 
			throw new VariantInternalException("Unable to deserialzie request: no status");
		if (!(statusStr instanceof String)) 
			throw new VariantInternalException("Unable to deserialzie request: status not string");

		result.status = Status.valueOf((String)statusStr);

		Object committed = fields.get(FILED_NAME_COMMITTED);
		if (committed == null) 
			throw new VariantInternalException("Unable to deserialzie request: no committed");
		if (!(committed instanceof Boolean)) 
			throw new VariantInternalException("Unable to deserialzie request: committed not boolean");

		result.committed = (Boolean) committed;

		Object paramListObj = fields.get(FIELD_NAME_PARAMS);
		if (paramListObj != null) {
			Map<String,String> paramMap = new HashMap<String, String>();
			try {
				List<?> paramListRaw = (List<?>) paramListObj; 
				for (Object obj: paramListRaw) {
					Map<?,?> objMap = (Map<?,?>) obj;
					String key = (String)objMap.get(FIELD_NAME_KEY);
					String val = (String) objMap.get(FIELD_NAME_VALUE);
					paramMap.put(key, val);
				}
			}
			catch (Exception e) {
				throw new VariantInternalException("Unable to deserialzie request: bad params spec", e);
			}
			result.resolvedParameterMap = paramMap;
		}

		Object experiencesListObj = fields.get(FIELD_NAME_EXPERIENCES);
		if (experiencesListObj != null) {
			ArrayList<Experience> experiencesList = new ArrayList<Experience>();
			try {
				List<?> experiencesListRaw = (List<?>) experiencesListObj; 
				for (Object obj: experiencesListRaw) {
					String expQualifiedName = (String) obj;
					// qualified name = testName.expName - need to parse.
					Schema schema = coreApi.getSchema();
					String[] tokens = expQualifiedName.split("\\.");
					experiencesList.add(schema.getTest(tokens[0]).getExperience(tokens[1]));
				}
			}
			catch (Exception e) {
				throw new VariantInternalException("Unable to deserialzie request: bad params spec", e);
			}
			result.targetedExperiencesCache = experiencesList;
		}

		return result;
	}
}

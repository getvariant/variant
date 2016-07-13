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
import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
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
import com.variant.core.session.SessionScopedTargetingStabile;
import com.variant.core.srvstub.TestExperienceServerStub;

/**
 * 
 * @author Igor
 *
 */
public class CoreStateRequestImpl implements VariantCoreStateRequest, Serializable {

	/**
	 * Needs serializable because we keep it in session.
	 */
	private static final long serialVersionUID = 1L;
	
	private CoreSessionImpl session;	
	private State state;
	private Status status = Status.OK;
	private Map<String,String> resolvedParameterMap;
	private StateVisitedEvent event = null;
	private boolean committed = false;
	
	// For transitional server side use only.
	private String stateName = null;
	
	// This doesn't change over the life of a request, so we'll only compute this once.
	private Collection<Experience> activeExperiences; 
		
	/**
	 * Regular constructor
	 * @param session
	 */
	CoreStateRequestImpl(CoreSessionImpl session, StateImpl state) {
		this.session = session;
		this.state = state;
		session.setStateRequest(this);
	}

	/**
	 * Transitional server side constructor that has state name instead
	 * of the fully instantiated State object, which we cannot instantiate
	 * without a schema, which we don't yet have on server.
	 * 
	 * @param session
	 */
	CoreStateRequestImpl(CoreSessionImpl session, String stateName) {
		this.session = session;
		this.stateName = stateName;
		session.setStateRequest(this);
	}
	
	/**
	 *  Crate state visited event when appropriate.
	 */
	void createStateVisitedEvent() {
		event = new StateVisitedEvent(state);
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Commit this state request and trigger the state visited event. 
	 */
	@Override
	public void commit() {
		
		if (isCommitted()) throw new IllegalStateException("Request already committed");
		
		// We won't have an event if nothing is instrumented on this state
		if (event != null) {
			// The status of this request.
			event.getParameterMap().put("REQ_STATUS", status);
			// log all resolved state params as event params.
			for (Map.Entry<String,String> e: resolvedParameterMap.entrySet()) {
				event.getParameterMap().put(e.getKey(), e.getValue());				
			}
			// Trigger state visited event
			session.triggerEvent(event);
			event = null;
		}
		
		committed = true;

	}

	@Override
	public VariantCoreSession getSession() {
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


	/* move to session
	@Override
	public VariantTargetingTracker getTargetingTracker() {
		return targetingTracker;
	}
	*/
	
	@Override
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public VariantEvent getStateVisitedEvent() {		
		return event;
	}

	@Override
	public Collection<Experience> getActiveExperiences() {
		
		if (activeExperiences == null) {

			SessionScopedTargetingStabile stabile = session.getTargetingStabile();
			ArrayList<Experience> result = new ArrayList<Experience>();

			for (Test test: state.getInstrumentedTests()) {
				if (!test.isOn() || session.getDisqualifiedTests().contains(test)) continue;
				SessionScopedTargetingStabile.Entry entry = stabile.get(test.getName());
				if (entry == null) throw new VariantInternalException("Targeted experience for test [" + test.getName() + "] expected but not found in sessioin.");
				result.add(test.getExperience(entry.getExperienceName()));
			}
			activeExperiences = result;
		}
		return activeExperiences;
	}

	@Override
	public Experience getActiveExperience(Test test) {
		
		boolean found = false;
		
		for (Test t: state.getInstrumentedTests()) {

			if (!t.equals(test)) continue;
			found = true;

			if (!t.isOn() || session.getDisqualifiedTests().contains(test)) return null;
			
			SessionScopedTargetingStabile.Entry entry = session.getTargetingStabile().get(test.getName());
			if (entry == null) throw new VariantInternalException("Targeted experience for test [" + test.getName() + "] expected but not found in sessioin.");
			return test.getExperience(entry.getExperienceName());
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

	/**
	 * A transitional hack to get access to state name on the server where we
	 * don't yet have the schema and cannot properly instantiate state by state
	 * name.  Which is fine because we'llonly need the name to log events.
	 * @return
	 */
	public String getStateName() {
		
		if (session.getCoreApi().getComptime().getComponent() == VariantComptime.Component.SERVER)
			throw new VariantInternalException("Method is supported only on Server");
		
		return stateName;
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
		
		Collection<Experience> targetedExperiences = getActiveExperiences();
		if (targetedExperiences.size() > 0) {
			jsonGen.writeArrayFieldStart(FIELD_NAME_EXPERIENCES);
			for (Experience e: targetedExperiences) {
				jsonGen.writeString(e.toString() + "." + e.isControl());
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
	public static CoreStateRequestImpl fromJson(VariantCore coreApi, CoreSessionImpl session, Map<String,?> fields) {
		
		Object stateName = fields.get(FIELD_NAME_STATE);
		if (stateName == null) 
			throw new VariantInternalException("Unable to deserialzie request: no state");
		if (!(stateName instanceof String)) 
			throw new VariantInternalException("Unable to deserialzie request: state not string");
		
		// If we're on the server, we don't have the schema => we can't instantiate a State object,
		// but we need the state name to log events.
		
		CoreStateRequestImpl result =  ((VariantCore)coreApi).getComptime().getComponent() == VariantComptime.Component.SERVER ?
				new CoreStateRequestImpl(session, (String)stateName) :
				new CoreStateRequestImpl(session, (StateImpl) coreApi.getSchema().getState((String)stateName));
		
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
					// qualified name = testName.expName.bool - need to parse.
					String[] tokens = expQualifiedName.split("\\.");
					if (((VariantCore)coreApi).getComptime().getComponent() == VariantComptime.Component.SERVER) {
						experiencesList.add(new TestExperienceServerStub(tokens[0], tokens[1], new Boolean(tokens[2])));
					}
					else {
						Schema schema = coreApi.getSchema();
						experiencesList.add(schema.getTest(tokens[0]).getExperience(tokens[1]));
					}
				}
			}
			catch (Exception e) {
				throw new VariantInternalException("Unable to deserialzie request: bad params spec", e);
			}
			result.activeExperiences = experiencesList;
		}

		return result;
	}
	
}
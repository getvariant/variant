package com.variant.core.session;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.set.UnmodifiableSet;

import static com.variant.core.exception.RuntimeError.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.impl.StateVisitedEvent;
import com.variant.core.event.impl.util.VariantCollectionsUtils;
import com.variant.core.exception.Error;
import com.variant.core.exception.RuntimeErrorException;
import com.variant.core.exception.RuntimeInternalException;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.schema.impl.StateVariantImpl;
import com.variant.core.util.CaseInsensitiveMap;

/**
 * 
 * @author Igor
 *
 */
public class CoreStateRequest implements Serializable {

	/**
	 * Needs serializable because we keep it in session.
	 */
	private static final long serialVersionUID = 1L;
	
	private CoreSession session;	
	private StateImpl state;
	private Status status = Status.OK;
	private StateVariant resolvedStateVariant;
	private Map<String,String> resolvedParameterMap;
	private StateVisitedEvent event = null;
	
	// For transitional server side use only.
	// private String stateName = null;
	
	// This doesn't change over the life of a request, so we'll only compute this once.
	private Collection<Experience> activeExperiences; 
	
	// Client subclass will manipulate this. We just need it for serialization.
	protected boolean isCommitted = false;
	
	//---------------------------------------------------------------------------------------------//
	//                                          PACKAGE                                            //
	//---------------------------------------------------------------------------------------------//
	/**
	 * Regular constructor
	 * @param session
	 */
	CoreStateRequest(CoreSession session, StateImpl state) {
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
	 *
	CoreStateRequestImpl(CoreSessionImpl session, String stateName) {
		this.session = session;
		this.stateName = stateName;
		session.setStateRequest(this);
	}
*/	
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
	 * We don't actually need userData in this core implementation, but we want the method signature
	 * ready for the client.
	 * ON CLIENT NOW.
	public void commit() {

		session.checkState(); Used to check for enclosing session's non-expiration.
		
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
			// session.triggerEvent(event); -- on the client and server now.
			event = null;
		}
		
		committed = true;
		
		session.save();
	}

	/**
	 *
	public boolean isCommitted() {
		return committed;
	}
*/
	public CoreSession getSession() {
		return session;
	}

	public State getState() {
		return state;
	}

	public StateVariant getResolvedStateVariant() {
		return resolvedStateVariant;
	}
	
	public  String getResolvedParameter(String name) {
		return resolvedParameterMap.get(name);
	}

	public  Set<String> getResolvedParameterNames() {
		return UnmodifiableSet.unmodifiableSet(resolvedParameterMap.keySet());
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public VariantEvent getStateVisitedEvent() {		
		return event;
	}

	public Collection<Experience> getLiveExperiences() {
		
		if (activeExperiences == null) {

			SessionScopedTargetingStabile stabile = session.getTargetingStabile();
			ArrayList<Experience> result = new ArrayList<Experience>();

			for (Test test: state.getInstrumentedTests()) {
				if (!test.isOn() || session.getDisqualifiedTests().contains(test)) continue;
				SessionScopedTargetingStabile.Entry entry = stabile.get(test.getName());
				if (entry == null) throw new RuntimeInternalException("Targeted experience for test [" + test.getName() + "] expected but not found in sessioin.");
				result.add(test.getExperience(entry.getExperienceName()));
			}
			activeExperiences = result;
		}
		return activeExperiences;
	}

	public Experience getLiveExperience(Test test) {
		
		boolean found = false;
		
		for (Test t: state.getInstrumentedTests()) {

			if (!t.equals(test)) continue;
			found = true;

			if (!t.isOn() || session.getDisqualifiedTests().contains(test)) return null;
			
			SessionScopedTargetingStabile.Entry entry = session.getTargetingStabile().get(test.getName());
			if (entry == null) throw new RuntimeInternalException("Targeted experience for test [" + test.getName() + "] expected but not found in sessioin.");
			return test.getExperience(entry.getExperienceName());
		}
		
		if (!found) throw new RuntimeErrorException(STATE_NOT_INSTRUMENTED_FOR_TEST, state.getName(), test.getName());

		return null;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * @param path
	 */
	public void setResolvedStateVariant(StateVariantImpl variant) {
		this.resolvedStateVariant = variant;
		if (variant == null) {
			resolvedParameterMap = state.getParameterMap();
		}
		else {
			resolvedParameterMap = new CaseInsensitiveMap<String>();
			VariantCollectionsUtils.mapMerge(resolvedParameterMap, state.getParameterMap(), variant.getParameters());
		}
	}
		
	/**
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
	 *
	public String getStateName() {
		
		if (session.getCoreApi().getComptime().getComponent() == VariantComptime.Component.SERVER)
			throw new VariantInternalException("Method is supported only on Server");
		
		return stateName;
	}
	*/
	//---------------------------------------------------------------------------------------------//
	//                                       SERIALIZATION                                         //
	//---------------------------------------------------------------------------------------------//

	private static final String FIELD_NAME_STATE = "state";
	private static final String FIELD_NAME_STATUS = "status";
	private static final String FILED_NAME_COMMITTED = "committed";
	private static final String FIELD_NAME_PARAMS = "params";
	private static final String FIELD_NAME_KEY = "name";
	private static final String FIELD_NAME_VALUE = "value";
	private static final String FIELD_NAME_EXPERIENCES = "exps";
	
	/**
	 * Serialize to JSON.
	 * @return
	 * @throws Exception
	 */
	public String toJson(Schema schema) throws Exception {
		StringWriter result = new StringWriter(2048);
		JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
		jsonGen.writeStartObject();
		jsonGen.writeStringField(FIELD_NAME_STATE, state.getName());
		jsonGen.writeStringField(FIELD_NAME_STATUS, status.toString());
		jsonGen.writeBooleanField(FILED_NAME_COMMITTED, isCommitted);
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
		
		Collection<Experience> liveExperiences = getLiveExperiences();
		if (liveExperiences.size() > 0) {
			jsonGen.writeArrayFieldStart(FIELD_NAME_EXPERIENCES);
			for (Experience e: liveExperiences) {
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
	public static CoreStateRequest fromJson(Schema schema, CoreSession session, Map<String,?> fields) {
		
		Object stateName = fields.get(FIELD_NAME_STATE);
		if (stateName == null) 
			throw new RuntimeInternalException("Unable to deserialzie request: no state");
		if (!(stateName instanceof String)) 
			throw new RuntimeInternalException("Unable to deserialzie request: state not string");
		
		// If we're on the server, we don't have the schema => we can't instantiate a State object,
		// but we need the state name to log events.
		
		CoreStateRequest result = new CoreStateRequest(session, (StateImpl) schema.getState((String)stateName));
		
		Object statusStr = fields.get(FIELD_NAME_STATUS);
		if (statusStr == null) 
			throw new RuntimeInternalException("Unable to deserialzie request: no status");
		if (!(statusStr instanceof String)) 
			throw new RuntimeInternalException("Unable to deserialzie request: status not string");

		result.status = Status.valueOf((String)statusStr);

		Object committed = fields.get(FILED_NAME_COMMITTED);
		if (committed == null) 
			throw new RuntimeInternalException("Unable to deserialzie request: no committed");
		if (!(committed instanceof Boolean)) 
			throw new RuntimeInternalException("Unable to deserialzie request: committed not boolean");

		result.isCommitted = (Boolean) committed;

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
				throw new RuntimeInternalException("Unable to deserialzie request: bad params spec", e);
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
					experiencesList.add(schema.getTest(tokens[0]).getExperience(tokens[1]));
				}
			}
			catch (Exception e) {
				throw new RuntimeInternalException("Unable to deserialzie request: bad experiences spec", e);
			}
			result.activeExperiences = experiencesList;
		}

		return result;
	}
	
	/**
	 * Status of a {@link com.variant.core.VariantCoreStateRequest}.
	 */
	static enum Status {
		OK, FAIL
	}

}

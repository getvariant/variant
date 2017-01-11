package com.variant.core.session;

import static com.variant.core.exception.CommonError.STATE_NOT_INSTRUMENTED_BY_TEST;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.UnmodifiableMap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.core.StateRequestStatus;
import com.variant.core.VariantEvent;
import com.variant.core.exception.CoreException;
import com.variant.core.impl.StateVisitedEvent;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.schema.impl.StateVariantImpl;
import com.variant.core.util.CaseInsensitiveMap;
import com.variant.core.util.CaseInsensitiveImmutableMap;
import com.variant.core.util.VariantCollectionsUtils;

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
	private StateVariant resolvedStateVariant;
	private Map<String,String> resolvedParameterMap;
	private StateVisitedEvent event = null;
	
	// For transitional server side use only.
	// private String stateName = null;
	
	// Active
	private Set<Experience> liveExperiences; 
	
	private StateRequestStatus status = StateRequestStatus.OK;
	private boolean committed = false;
	
	//---------------------------------------------------------------------------------------------//
	//                                          PACKAGE                                            //
	//---------------------------------------------------------------------------------------------//
	/**
	 * Regular constructor
	 * @param session
	 */
	public CoreStateRequest(CoreSession session, StateImpl state) {
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
	public void createStateVisitedEvent() {
		event = new StateVisitedEvent(state);
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	public CoreSession getSession() {
		return session;
	}

	public State getState() {
		return state;
	}

	public void commit() {
		committed = true;
	}

	public boolean isCommitted() {
		return committed;
	}

	public StateRequestStatus getStatus() {
		return status;
	}
	
	public void setStatus(StateRequestStatus status) {
		this.status = status;
	}

	public StateVariant getResolvedStateVariant() {
		return resolvedStateVariant;
	}
	
	/**
	 * Immutable parameter map, if need all.
	 * @return
	 */
	public  Map<String,String> getResolvedParameters() {
		return new CaseInsensitiveImmutableMap<String>(resolvedParameterMap);
	}
	
	public VariantEvent getStateVisitedEvent() {		
		return event;
	}

	/**
	 * Live experiences don't change during the session, ok to cache as this is a session scoped object.
	 * @return
	 */
	public synchronized Set<Experience> getLiveExperiences() {
		
		if (liveExperiences == null) {

			SessionScopedTargetingStabile stabile = session.getTargetingStabile();
			HashSet<Experience> result = new HashSet<Experience>();

			for (Test test: state.getInstrumentedTests()) {
				if (!test.isOn() || session.getDisqualifiedTests().contains(test)) continue;
				SessionScopedTargetingStabile.Entry entry = stabile.get(test.getName());
				if (entry == null) throw new CoreException.Internal("Targeted experience for test [" + test.getName() + "] expected but not found in sessioin.");
				result.add(test.getExperience(entry.getExperienceName()));
			}
			liveExperiences = result;
		}
		return liveExperiences;
	}

	public Experience getLiveExperience(Test test) {
		
		boolean found = false;
		
		for (Test t: state.getInstrumentedTests()) {

			if (!t.equals(test)) continue;
			found = true;

			if (!t.isOn() || session.getDisqualifiedTests().contains(test)) return null;
			
			SessionScopedTargetingStabile.Entry entry = session.getTargetingStabile().get(test.getName());
			if (entry == null) throw new CoreException.Internal("Targeted experience for test [" + test.getName() + "] expected but not found in sessioin.");
			return test.getExperience(entry.getExperienceName());
		}
		
		if (!found) throw new CoreException.User(STATE_NOT_INSTRUMENTED_BY_TEST, state.getName(), test.getName());

		return null;
	}
	
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
			throw new CoreException.Internal("Unable to deserialzie request: no state");
		if (!(stateName instanceof String)) 
			throw new CoreException.Internal("Unable to deserialzie request: state not string");
		
		// If we're on the server, we don't have the schema => we can't instantiate a State object,
		// but we need the state name to log events.
		
		CoreStateRequest result = new CoreStateRequest(session, (StateImpl) schema.getState((String)stateName));
		
		String statusStr = (String) fields.get(FIELD_NAME_STATUS);
		if (statusStr == null) 
			throw new CoreException.Internal("Unable to deserialzie request: no status");
		if (!(statusStr instanceof String)) 
			throw new CoreException.Internal("Unable to deserialzie request: status not string");

		result.status = StateRequestStatus.valueOf(statusStr);
		
		Object committed = fields.get(FILED_NAME_COMMITTED);
		if (committed == null) 
			throw new CoreException.Internal("Unable to deserialzie request: no committed");
		if (!(committed instanceof Boolean)) 
			throw new CoreException.Internal("Unable to deserialzie request: committed not boolean");

		result.committed = (Boolean) committed;

		Object paramListObj = fields.get(FIELD_NAME_PARAMS);
		if (paramListObj != null) {
			Map<String,String> paramMap = new CaseInsensitiveMap<String>();
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
				throw new CoreException.Internal("Unable to deserialzie request: bad params spec", e);
			}
			result.resolvedParameterMap = paramMap;
		}

		Object experiencesListObj = fields.get(FIELD_NAME_EXPERIENCES);
		if (experiencesListObj != null) {
			result.liveExperiences = new HashSet<Experience>();
			try {
				List<?> experiencesListRaw = (List<?>) experiencesListObj; 
				for (Object obj: experiencesListRaw) {
					String expQualifiedName = (String) obj;
					// qualified name = testName.expName.bool - need to parse.
					String[] tokens = expQualifiedName.split("\\.");
					result.liveExperiences.add(schema.getTest(tokens[0]).getExperience(tokens[1]));
				}
			}
			catch (Exception e) {
				throw new CoreException.Internal("Unable to deserialzie request: bad experiences spec", e);
			}
		}

		return result;
	}
	
}

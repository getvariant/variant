package com.variant.core.session;

import static com.variant.core.impl.ServerError.STATE_NOT_INSTRUMENTED_BY_TEST;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.core.StateRequestStatus;
import com.variant.core.TraceEvent;
import com.variant.core.impl.CoreException;
import com.variant.core.impl.StateVisitedEvent;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.schema.impl.StateVariantImpl;
import com.variant.core.util.CaseInsensitiveMap;
import com.variant.core.util.CollectionsUtils;
import com.variant.core.util.immutable.CaseInsensitiveImmutableMap;

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
	
	private final CoreSession session;
	private final StateImpl state;
	private StateVariant resolvedStateVariant;
	private Map<String,String> resolvedParameterMap;
	private StateVisitedEvent event  = null;
	
	// For transitional server side use only.
	// private String stateName = null;
	
	// Active
	private Set<Experience> liveExperiences; 
	
	private StateRequestStatus status = StateRequestStatus.OK;
	private boolean committed = false;
	
	/**
	 * Regular constructor
	 * @param session
	 */
	public CoreStateRequest(CoreSession session, State state) {
		this.session = session;
		this.state = (StateImpl) state;
		session.setStateRequest(this);
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	public void createStateVisitedEvent() {
		event = new StateVisitedEvent(session, state);
	}

	public CoreSession getSession() {
		return session;
	}

	public State getState() {
		return state;
	}

	public void commit() {
		event = null;
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
	
	public TraceEvent getStateVisitedEvent() {		
		return event;
	}

	/**
	 * Live experiences don't change during the session, ok to cache as this is a session scoped object.
	 * @return
	 */
	public synchronized Set<Experience> getLiveExperiences() {
		
		if (liveExperiences == null) {

			SessionScopedTargetingStabile stabile = session.getTargetingStabile();
			HashSet<Experience> result = new LinkedHashSet<Experience>();

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
			resolvedParameterMap = state.getParameters();
		}
		else {
			resolvedParameterMap = new CaseInsensitiveMap<String>();
			CollectionsUtils.mapMerge(resolvedParameterMap, state.getParameters(), variant.getParameters());
		}
	}
		
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
	private static final String FIELD_NAME_VARIANT = "variant";
	private static final String FIELD_NAME_TEST = "test";
	private static final String FIELD_NAME_OFFSET = "offset";
	private static final String FIELD_NAME_EVENT = "event";

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
		
		if (!resolvedParameterMap.isEmpty()) {
			jsonGen.writeArrayFieldStart(FIELD_NAME_PARAMS);
			for (Map.Entry<String,String> e: resolvedParameterMap.entrySet()) {
				jsonGen.writeStartObject();
				jsonGen.writeStringField(FIELD_NAME_KEY, e.getKey());
				jsonGen.writeStringField(FIELD_NAME_VALUE, e.getValue());
				jsonGen.writeEndObject();
			}
			jsonGen.writeEndArray();
		}

		if (resolvedStateVariant != null) {
			jsonGen.writeObjectFieldStart(FIELD_NAME_VARIANT);
			jsonGen.writeStringField(FIELD_NAME_TEST, resolvedStateVariant.getTest().getName());
			int offset = 0;
			for (StateVariant var: resolvedStateVariant.getOnState().getVariants()) {
				if (var == resolvedStateVariant) break;
				offset++;
			}
			jsonGen.writeNumberField(FIELD_NAME_OFFSET, offset);
			jsonGen.writeEndObject();			
		}

		if (event != null) {
			jsonGen.writeFieldName(FIELD_NAME_EVENT);
			jsonGen.writeRawValue(event.toJson());
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
	@SuppressWarnings("unchecked")
	public static CoreStateRequest fromJson(Schema schema, CoreSession session, Map<String,?> fields) {
		
		String stateName = (String) fields.get(FIELD_NAME_STATE);
		if (stateName == null) 
			throw new CoreException.Internal("Unable to deserialzie request: no state");
		
		if (!(stateName instanceof String)) 
			throw new CoreException.Internal("Unable to deserialzie request: state not string");
		
		State state = schema.getState(stateName);
		
		if (state == null) 
			throw new CoreException.Internal(String.format("State [%s] not in schema [%s]", stateName, schema.getMeta().getName()));

		CoreStateRequest result = new CoreStateRequest(session, state);
		
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

		Map<String,?> eventMap = (Map<String,?>) fields.get(FIELD_NAME_EVENT);
		if (eventMap != null) {
			result.event = StateVisitedEvent.fromJson(session, eventMap);
		}
		
		Object experiencesListObj = fields.get(FIELD_NAME_EXPERIENCES);
		if (experiencesListObj != null) {
			result.liveExperiences = new LinkedHashSet<Experience>();
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

		Map<?,?> variantObj = (Map<?,?>) fields.get(FIELD_NAME_VARIANT);
		if (variantObj != null) {
			String testName = (String) variantObj.get(FIELD_NAME_TEST);
			int offset = (Integer) variantObj.get(FIELD_NAME_OFFSET);
			result.resolvedStateVariant = schema.getTest(testName).getOnState(schema.getState(stateName)).getVariants().get(offset);
		}

		return result;
	}
	
}

package com.variant.core.session;

import static com.variant.core.schema.impl.MessageTemplate.BOOT_TARGETING_TRACKER_NO_INTERFACE;
import static com.variant.core.schema.impl.MessageTemplate.RUN_ACTIVE_REQUEST;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.VariantTargetingTracker;
import com.variant.core.VariantProperties.Key;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.impl.EventWriter;
import com.variant.core.event.impl.VariantEventDecoratorImpl;
import com.variant.core.exception.VariantBootstrapException;
import com.variant.core.exception.VariantException;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.VariantComptime;
import com.variant.core.impl.VariantCoreImpl;
import com.variant.core.impl.VariantStateRequestImpl;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.util.Tuples.Pair;
import com.variant.core.util.VariantCollectionsUtils;

/**
 * 
 * @author Igor
 *
 */
public class VariantSessionImpl implements VariantSession, Serializable {
	
	///
	private static final long serialVersionUID = 1L;

	private String id;
	private long timestamp = System.currentTimeMillis();
	private VariantStateRequestImpl currentRequest = null;
	private HashMap<State, Integer> traversedStates = new HashMap<State, Integer>();
	private HashMap<Test, Boolean> traversedTests = new HashMap<Test, Boolean>();
	private VariantCoreImpl coreApi;
	private String schemaId;
	
	/**
	 * 
	 * @param id
	 */
	public VariantSessionImpl(VariantCoreImpl coreApi, String id) {
		this.coreApi = coreApi;
		// No schema ID on server yet. 
		if (coreApi.getComptime().getComponent() != VariantComptime.Component.SERVER) 
			this.schemaId = coreApi.getSchema().getId();
		this.id = id;
	}
		
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * 
	 */
	@Override
	public String getSchemaId() {
		return schemaId;
	}

	@Override
	public long creationTimestamp() {
		return timestamp;
	}
	
	@Override
	public VariantStateRequest getStateRequest() {
		return currentRequest;
	}

	@Override
	public Collection<Pair<State, Integer>> getTraversedStates() {
		return VariantCollectionsUtils.mapToPairs(traversedStates);
	}

	@Override
	public Collection<Pair<Test, Boolean>> getTraversedTests() {
		return VariantCollectionsUtils.mapToPairs(traversedTests);
	}

	@Override
	public void triggerEvent(VariantEvent event) {

		if (event == null) throw new IllegalArgumentException("Event cannot be null");		
		EventWriter ew = ((VariantCoreImpl) coreApi).getEventWriter();
		ew.write(new VariantEventDecoratorImpl(event, this));
	}

	/**
	 * 
	 */
	@Override
	public VariantStateRequest targetForState(State state, Object...targetingPersisterUserData) {
		
		// Can't have two requests at one time
		if (currentRequest != null && !currentRequest.isCommitted()) {
			throw new VariantRuntimeException (RUN_ACTIVE_REQUEST);
		}
		
		// init Targeting Persister with the same user data.
		VariantTargetingTracker tp = null;
		String className = coreApi.getProperties().get(Key.TARGETING_TRACKER_CLASS_NAME, String.class);
		
		try {
			Object object = Class.forName(className).newInstance();
			if (object instanceof VariantTargetingTracker) {
				tp = (VariantTargetingTracker) object;
			}
			else {
				throw new VariantBootstrapException(BOOT_TARGETING_TRACKER_NO_INTERFACE, className, VariantTargetingTracker.class.getName());
			}
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to instantiate targeting persister class [" + className +"]", e);
		}
			
		tp.initialized(coreApi, this, targetingPersisterUserData);

		addTraversedState(state);
		
		return coreApi.getRuntime().targetSessionForState(this, state, tp);
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @return
	 */
	public VariantCoreImpl getCoreApi() {
		return coreApi;
	}
	
	/**
	 * 
	 * @param req
	 */
	public void setStateRequest(VariantStateRequestImpl req) {
		currentRequest = req;
	}

	/**
	 * Traversed and qualified?
	 * @param test
	 * @return
	 */
	public boolean isQualified(Test test) {
		Boolean result = traversedTests.get(test);
		return result != null && result;
	}
	
	/**
	 * Traversed and disqualified?
	 * @param test
	 * @return
	 */
	public boolean isDisqualified(Test test) {
		Boolean result = traversedTests.get(test);
		return result != null && !result;
	}

	/**
	 * 
	 * @param test
	 */
	public void addTraversedTest(Test test, boolean qualified) {

		if (traversedTests.get(test) != null) 
			throw new VariantInternalException(
					String.format("Test [%s] already exists in the traversed list", test.getName()));
					
		traversedTests.put(test, qualified);
	}

	/**
	 * 
	 * @param state
	 */
	public void addTraversedState(State state) {

		Integer count = traversedStates.get(state);
		if (count == null) count = 1;
		else count++;
		traversedStates.put(state, count);
	}

	private static final String FIELD_NAME_ID = "sid";
	private static final String FIELD_NAME_TIMESTAMP = "ts";
	private static final String FIELD_NAME_SCHEMA_ID = "schid";
	private static final String FIELD_NAME_CURRENT_REQUEST = "req";
	private static final String FIELD_NAME_TRAVERSED_STATES = "states";
	private static final String FIELD_NAME_TRAVERSED_TESTS = "tests";
	private static final String FIELD_NAME_STATE = "state";
	private static final String FIELD_NAME_COUNT = "count";
	private static final String FIELD_NAME_TEST = "test";
	private static final String FIELD_NAME_QUALIFIED = "qual";
	
	/**
	 * Serialize as JSON.
	 * @return
	 * @throws JsonProcessingException 
	 */
	public String toJson() throws VariantException {
		try {
			StringWriter result = new StringWriter(2048);
			JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
			jsonGen.writeStartObject();
			jsonGen.writeStringField(FIELD_NAME_ID, id);
			jsonGen.writeNumberField(FIELD_NAME_TIMESTAMP, timestamp);
			jsonGen.writeStringField(FIELD_NAME_SCHEMA_ID, schemaId);
			if (currentRequest != null) {
				jsonGen.writeFieldName(FIELD_NAME_CURRENT_REQUEST);
				jsonGen.writeRawValue(currentRequest.toJson());
			}
			if (traversedStates.size() > 0) {
				jsonGen.writeArrayFieldStart(FIELD_NAME_TRAVERSED_STATES);
				for (Map.Entry<State, Integer> e: traversedStates.entrySet()) {
					jsonGen.writeStartObject();
					jsonGen.writeStringField(FIELD_NAME_STATE, e.getKey().getName());
					jsonGen.writeNumberField(FIELD_NAME_COUNT, e.getValue());
					jsonGen.writeEndObject();
				}
				jsonGen.writeEndArray();
			}
			if (traversedTests.size() > 0) {
				jsonGen.writeArrayFieldStart(FIELD_NAME_TRAVERSED_TESTS);
				for (Map.Entry<Test, Boolean> e: traversedTests.entrySet()) {
					jsonGen.writeStartObject();
					jsonGen.writeStringField(FIELD_NAME_TEST, e.getKey().getName());
					jsonGen.writeBooleanField(FIELD_NAME_QUALIFIED, e.getValue());
					jsonGen.writeEndObject();
				}
				jsonGen.writeEndArray();
			}		
			jsonGen.writeEndObject();
			jsonGen.flush();
			return result.toString();
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to serialize session", e);
		}
	}

	/**
	 * Deserialize from JSON
	 * @param json
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static VariantSessionImpl fromJson(VariantCoreImpl coreApi, String json) {

		ObjectMapper mapper = new ObjectMapper();		
		Map<String,?> fields = null;
		
		try {
			fields = mapper.readValue(json, Map.class);
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to deserialzie session: [" + json + "]");
		}
		
		Object idObj = fields.get(FIELD_NAME_ID);
		if (idObj == null) 
			throw new VariantInternalException("Unable to deserialzie session: no id: [" + json + "]");
		if (!(idObj instanceof String)) 
			throw new VariantInternalException("Unable to deserialzie session: id not string: [" + json + "]");

		Object schidObj = fields.get(FIELD_NAME_SCHEMA_ID);
		if (schidObj == null ) 
			throw new VariantInternalException("Unable to deserialzie session: no schema id: [" + json + "]");
		if (!(schidObj instanceof String)) 
			throw new VariantInternalException("Unable to deserialzie session: schema id not string: [" + json + "]");

		// If schema has changed, return null. But remember that we don't yet have a schema on server.
		if (coreApi.getComptime().getComponent() != VariantComptime.Component.SERVER && !coreApi.getSchema().getId().equals(schidObj)) {
			return null;
		}
		
		VariantSessionImpl result = new VariantSessionImpl(coreApi, (String)idObj);

		Object tsObj = fields.get(FIELD_NAME_TIMESTAMP);
		if (tsObj == null) 
			throw new VariantInternalException("Unable to deserialzie session: no timestamp: [" + json + "]");
		if (!(tsObj instanceof Number)) 
			throw new VariantInternalException("Unable to deserialzie session: id not number: [" + json + "]");

		result.timestamp = ((Number)tsObj).longValue();
		
		Object currentRequestObj = fields.get(FIELD_NAME_CURRENT_REQUEST);
		if (currentRequestObj != null) {
			if (!(currentRequestObj instanceof Map<?,?>)) 
			throw new VariantInternalException("Unable to deserialzie session: currentRequest not map: [" + json + "]");
			result.currentRequest = VariantStateRequestImpl.fromJson(coreApi, result, (Map<String,?>)currentRequestObj);
		}
		
		// If server, don't deserialize traversed tests and states because we don't have the schema.
		if (coreApi.getComptime().getComponent() != VariantComptime.Component.SERVER) {
			Object statesObj = fields.get(FIELD_NAME_TRAVERSED_STATES);
			if (statesObj != null) {
				HashMap<State,Integer> statesMap = new HashMap<State, Integer>();
				try {
					List<?> statesListRaw = (List<?>) statesObj; 
					for (Object obj: statesListRaw) {
						Map<?,?> objMap = (Map<?,?>) obj;
						String stateName = (String) objMap.get(FIELD_NAME_STATE);
						State state = coreApi.getSchema().getState(stateName);
						Integer count =  (Integer) objMap.get(FIELD_NAME_COUNT);
						statesMap.put(state, count);
					}
				}
				catch (Exception e) {
					throw new VariantInternalException("Unable to deserialzie session: bad states spec", e);
				}
				result.traversedStates = statesMap;
			}
		
			Object testsObj = fields.get(FIELD_NAME_TRAVERSED_TESTS);
			if (testsObj != null) {
				HashMap<Test, Boolean> testsMap = new HashMap<Test, Boolean>();
				try {
					List<?> testsListRaw = (List<?>) testsObj; 
					for (Object obj: testsListRaw) {
						Map<?,?> objMap = (Map<?,?>) obj;
						String testName = (String) objMap.get(FIELD_NAME_TEST);
						Test test = coreApi.getSchema().getTest(testName);
						Boolean qualified =  (Boolean) objMap.get(FIELD_NAME_QUALIFIED);
						testsMap.put(test, qualified);
					}
				}
				catch (Exception e) {
					throw new VariantInternalException("Unable to deserialzie session: bad tests spec", e);
				}
				result.traversedTests = testsMap;
			}
		}
		
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		try {
			VariantSessionImpl other = (VariantSessionImpl) o;
			return id.equals(other.id);
		}
		catch(ClassCastException e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}

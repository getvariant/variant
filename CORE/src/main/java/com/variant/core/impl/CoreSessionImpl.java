package com.variant.core.impl;

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
import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.impl.EventWriter;
import com.variant.core.event.impl.VariantEventDecoratorImpl;
import com.variant.core.exception.VariantException;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.util.Tuples.Pair;
import com.variant.core.util.VariantCollectionsUtils;

/**
 * 
 * @author Igor
 *
 */
public class CoreSessionImpl implements VariantCoreSession, Serializable {
	
	///
	private static final long serialVersionUID = 1L;

	private String id;
	private long timestamp = System.currentTimeMillis();
	private VariantCoreStateRequestImpl currentRequest = null;
	private HashMap<State, Integer> traversedStates = new HashMap<State, Integer>();
	private HashMap<Test, Boolean> traversedTests = new HashMap<Test, Boolean>();
	private VariantCore coreApi;
	private SessionScopedTargetingStabile targetingStabile;
	private String schemaId;
	
	/**
	 * 
	 * @param id
	 */
	protected CoreSessionImpl(String id, VariantCore coreApi, SessionScopedTargetingStabile targetingStabile) {
		
		this.coreApi = coreApi;
		this.targetingStabile = targetingStabile;
		
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
	public VariantCoreStateRequest getStateRequest() {
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
		EventWriter ew = ((VariantCore) coreApi).getEventWriter();
		ew.write(new VariantEventDecoratorImpl(event, this));
	}

	/**
	 * 
	 */
	@Override
	public VariantCoreStateRequest targetForState(State state) {
		
		// Can't have two requests at one time
		if (currentRequest != null && !currentRequest.isCommitted()) {
			throw new VariantRuntimeException (RUN_ACTIVE_REQUEST);
		}
		
		addTraversedState(state);
		
		return coreApi.getRuntime().targetSessionForState(this, (StateImpl) state);
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @return
	 */
	public VariantCore getCoreApi() {
		return coreApi;
	}
	
	/**
	 * 
	 * @param req
	 */
	public void setStateRequest(VariantCoreStateRequestImpl req) {
		currentRequest = req;
	}

	/**
	 * Traversed and qualified?
	 * @param test
	 * @return
	 */
	public boolean isQualifiedFor(Test test) {
		Boolean result = traversedTests.get(test);
		return result != null && result;
	}
	
	/**
	 * The {@link SessionScopedTargetingStabile} object associated with this session.
	 * @return
	 */
	SessionScopedTargetingStabile getTargetingStabile() {
		return targetingStabile;
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
	private static final String FIELD_NAME_TARGETING_STABIL = "stabil";
	
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
			
			if (targetingStabile.size() > 0) {
				jsonGen.writeArrayFieldStart(FIELD_NAME_TARGETING_STABIL);
				for (SessionScopedTargetingStabile.Entry entry: targetingStabile.getAll()) {
					jsonGen.writeString(entry.toString());
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
	public static CoreSessionImpl fromJson(VariantCore coreApi, String json) {

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
		
		SessionScopedTargetingStabile targetingStabile = new SessionScopedTargetingStabile();
		Object stabileObj = fields.get(FIELD_NAME_TARGETING_STABIL);
		if (stabileObj != null) {
			try {
				List<?> listRaw = (List<?>) stabileObj; 
				for (Object obj: listRaw) {
					String entryString = (String) obj;
					String[] tokens = entryString.split("\\.");
					targetingStabile.add(tokens[0], tokens[1], Long.parseLong(tokens[2]));
				}
			}
			catch (Exception e) {
				throw new VariantInternalException("Unable to deserialzie session: bad states spec", e);
			}
		}
			
		CoreSessionImpl result = new CoreSessionImpl((String)idObj, coreApi, targetingStabile);

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
			result.currentRequest = VariantCoreStateRequestImpl.fromJson(coreApi, result, (Map<String,?>)currentRequestObj);
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
			CoreSessionImpl other = (CoreSessionImpl) o;
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

package com.variant.core.impl;

import static com.variant.core.schema.impl.MessageTemplate.RUN_ACTIVE_REQUEST;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

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
import com.variant.core.session.SessionScopedTargetingStabile;
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
	private LinkedHashSet<Test> traversedTests = new LinkedHashSet<Test>();
	private LinkedHashSet<Test> disqualTests = new LinkedHashSet<Test>();
	private VariantCore coreApi;
	private SessionScopedTargetingStabile targetingStabile = new SessionScopedTargetingStabile();
	private String schemaId;
	
	/**
	 * 
	 * @param id
	 */
	public CoreSessionImpl(String id, VariantCore coreApi) {
		
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
	public VariantCoreStateRequest getStateRequest() {
		return currentRequest;
	}

	@Override
	public Collection<Pair<State, Integer>> getTraversedStates() {
		return VariantCollectionsUtils.mapToPairs(traversedStates);
	}

	@Override
	public Collection<Test> getTraversedTests() {
		return CollectionUtils.unmodifiableCollection(traversedTests);
	}

	@Override
	public Collection<Test> getDisqualifiedTests() {
		return CollectionUtils.unmodifiableCollection(disqualTests);
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
	 * 
	 * @param test
	 */
	public void addTraversedTest(Test test) {

		if (traversedTests.contains(test)) 
			throw new VariantInternalException(
					String.format("Test [%s] already contained in the traversed list", test.getName()));
		
		traversedTests.add(test);
	}

	/**
	 * 
	 * @param test
	 */
	public void addDisqualifiedTest(Test test) {

		if (disqualTests.contains(test)) {
				throw new VariantInternalException(
						String.format("Test [%s] already contained in the disqual list", test.getName()));
		}	
		disqualTests.add(test);
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
	private static final String FIELD_NAME_TIMESTAMP = "time";
	private static final String FIELD_NAME_SCHEMA_ID = "schid";
	private static final String FIELD_NAME_CURRENT_REQUEST = "req";
	private static final String FIELD_NAME_TRAVERSED_STATES = "ts";
	private static final String FIELD_NAME_TRAVERSED_TESTS = "tts";
	private static final String FIELD_NAME_DISQUAL_TESTS = "dts";
	private static final String FIELD_NAME_STATE = "state";
	private static final String FIELD_NAME_COUNT = "count";
	private static final String FIELD_NAME_TEST = "test";
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
				for (Test t: traversedTests) {
					jsonGen.writeString(t.getName());
				}
				jsonGen.writeEndArray();
			}
			
			if (disqualTests.size() > 0) {
				jsonGen.writeArrayFieldStart(FIELD_NAME_DISQUAL_TESTS);
				for (Test t: traversedTests) {
					jsonGen.writeString(t.getName());
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
					targetingStabile.add(
							tokens[0], 
							tokens[1], 
							Long.parseLong(tokens[2]));
				}
			}
			catch (Exception e) {
				throw new VariantInternalException("Unable to deserialzie session: bad states spec", e);
			}
		}
			
		CoreSessionImpl result = new CoreSessionImpl((String)idObj, coreApi);
		result.setTargetingStabile(targetingStabile);
		
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
				LinkedHashSet<Test> tests = new LinkedHashSet<Test>();
				try {
					List<String> testList = (List<String>) testsObj; 
					for (String testName: testList) {
						tests.add(coreApi.getSchema().getTest(testName));
					}
				}
				catch (Exception e) {
					throw new VariantInternalException("Unable to deserialzie session: bad tests spec", e);
				}
				result.traversedTests = tests;
			}
			
			testsObj = fields.get(FIELD_NAME_DISQUAL_TESTS);
			if (testsObj != null) {
				
				LinkedHashSet<Test> tests = new LinkedHashSet<Test>();
			
				try {
					List<String> testList = (List<String>) testsObj; 
					for (String testName: testList) {
						tests.add(coreApi.getSchema().getTest(testName));
					
					}
				}
				catch (Exception e) {
					throw new VariantInternalException("Unable to deserialzie session: bad disqual tests spec", e);
				}
				result.disqualTests = tests;
			}
		}
		
		return result;
	}
	
	/**
	 * @param targetintStabile
	 */
	public void setTargetingStabile(SessionScopedTargetingStabile targetingStabile) {
		this.targetingStabile = targetingStabile;
	}
	
	/**
	 * The {@link SessionScopedTargetingStabile} object associated with this session.
	 * @return
	 */
	public SessionScopedTargetingStabile getTargetingStabile() {
		return targetingStabile;
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

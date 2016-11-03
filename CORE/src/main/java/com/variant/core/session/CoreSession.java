package com.variant.core.session;

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
import com.variant.core.event.impl.util.VariantCollectionsUtils;
import com.variant.core.exception.RuntimeInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.SessionId;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.util.Tuples.Pair;

/**
 * 
 * @author Igor
 *
 */
public class CoreSession implements Serializable {
	
	///
	private static final long serialVersionUID = 1L;

	private SessionId sid;
	private long timestamp = System.currentTimeMillis();
	private CoreStateRequest currentRequest = null;
	private HashMap<State, Integer> traversedStates = new HashMap<State, Integer>();
	private LinkedHashSet<Test> traversedTests = new LinkedHashSet<Test>();
	private LinkedHashSet<Test> disqualTests = new LinkedHashSet<Test>();
	private Schema schema;
	private SessionScopedTargetingStabile targetingStabile = new SessionScopedTargetingStabile();
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Brand new session
	 * @param id
	 */
	public CoreSession(SessionId id, Schema schema) {
		this.sid = id;		
		this.schema = schema;
	}
	
	/**
	 * Deserialized session from raw JSON.
	 * @param json
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public CoreSession (String json, Schema schema) {
		this(SessionId.NULL, schema);
		try {
			ObjectMapper mapper = new ObjectMapper();		
			Map<String,?> mappedJson = mapper.readValue(json, Map.class);
			fromJson(mappedJson, schema);
		}
		catch (VariantRuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeInternalException("Unable to deserialzie session: [" + json + "]", e);
		}

	}

	/**
	 * Deserialized session from mapped JSON.
	 * @param json
	 * @return
	 */
	public CoreSession (Map<String,?> mappedJson, Schema schema) {
		this(SessionId.NULL, schema);
		fromJson(mappedJson, schema);
	}

	/**
	 * 
	 */
	public String getId() {
		return sid.id;
	}

	/**
	 * 
	 */
	public Schema getSchema() {
		return schema;
	}

	public long creationTimestamp() {
		return timestamp;
	}
	
	public CoreStateRequest getStateRequest() {
		return currentRequest;
	}

	public Collection<Pair<State, Integer>> getTraversedStates() {
		return VariantCollectionsUtils.mapToPairs(traversedStates);
	}

	public Collection<Test> getTraversedTests() {
		return CollectionUtils.unmodifiableCollection(traversedTests);
	}

	public Collection<Test> getDisqualifiedTests() {
		return CollectionUtils.unmodifiableCollection(disqualTests);
	}


	/**  
	 * No notion of expiration in core.
	 *
	@Override
	public boolean isExpired() {
		throw new IllegalArgumentException();
	}

	//---------------------------------------------------------------------------------------------//
	//                                    PUBLIC UNSUPPORTED                                       //
	//---------------------------------------------------------------------------------------------//

	@Override
	public Object setAttribute(String name, Object value) {
		throw new VariantExpectedRuntimeException(RUN_METHOD_UNSUPPORTED);
	}

	@Override
	public Object getAttribute(String name) {
		throw new VariantExpectedRuntimeException(RUN_METHOD_UNSUPPORTED);
	}
*/
	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//
	/**
	 * 
	 * @return
	 *
	public VariantCore getCoreApi() {
		return core;
	}
	
	/**
	 * 
	 * @param req
	 */
	public void setStateRequest(CoreStateRequest req) {
		currentRequest = req;
	}
		
	/**
	 * 
	 * @param test
	 */
	public void addTraversedTest(Test test) {

		if (traversedTests.contains(test)) 
			throw new RuntimeInternalException(
					String.format("Test [%s] already contained in the traversed list", test.getName()));
		
		traversedTests.add(test);
	}

	/**
	 * 
	 * @param test
	 */
	public void addDisqualifiedTest(Test test) {

		if (disqualTests.contains(test)) {
				throw new RuntimeInternalException(
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

	/**
	 * core session doesn't know how to save itself.
	 *
	public void save() {
		core.getSessionService().saveSession(this);
	}
	*
	public void checkState() {
		if (!schemaId.equals(core.getSchema().getId())) {
			throw new VariantSchemaModifiedException(core.getSchema().getId(), schemaId);		
		}
	}
	*/
	//---------------------------------------------------------------------------------------------//
	//                                       Serialization                                          //
	//---------------------------------------------------------------------------------------------//

	private static final String FIELD_NAME_ID = "sid";
	private static final String FIELD_NAME_TIMESTAMP = "ts";
	private static final String FIELD_NAME_SCHEMA_ID = "schid";
	private static final String FIELD_NAME_CURRENT_REQUEST = "request";
	private static final String FIELD_NAME_TRAVERSED_STATES = "states";
	private static final String FIELD_NAME_TRAVERSED_TESTS = "tests";
	private static final String FIELD_NAME_DISQUAL_TESTS = "dis_tests";
	private static final String FIELD_NAME_STATE = "state";
	private static final String FIELD_NAME_COUNT = "count";
	private static final String FIELD_NAME_TARGETING_STABIL = "stabil";
	
	/**
	 * Serialize as JSON.
	 * @return
	 * @throws JsonProcessingException 
	 */
	public String toJson(Schema schema) throws VariantRuntimeException {
		try {
			StringWriter result = new StringWriter(2048);
			JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
			jsonGen.writeStartObject();
			jsonGen.writeStringField(FIELD_NAME_ID, sid.id);
			jsonGen.writeNumberField(FIELD_NAME_TIMESTAMP, timestamp);
			jsonGen.writeStringField(FIELD_NAME_SCHEMA_ID, schema.getId());
			
			if (currentRequest != null) {
				jsonGen.writeFieldName(FIELD_NAME_CURRENT_REQUEST);
				jsonGen.writeRawValue(currentRequest.toJson(schema));
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
				for (Test t: disqualTests) {
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
			throw new RuntimeInternalException("Unable to serialize session", e);
		}
	}
	
	/**
	 * Deserialize from parsed JSON
	 * @param json
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void fromJson(Map<String,?> parsedJson, Schema schema) {
		
		Object idObj = parsedJson.get(FIELD_NAME_ID);
		if (idObj == null) 
			throw new RuntimeInternalException("No id");
		if (!(idObj instanceof String)) 
			throw new RuntimeInternalException("id not string");
		sid = new SessionId((String)idObj);
		
		Object schidObj = parsedJson.get(FIELD_NAME_SCHEMA_ID);
		if (schidObj == null ) 
			throw new RuntimeInternalException("No schema id");
		if (!(schidObj instanceof String)) 
			throw new RuntimeInternalException("Schema id not string");

/* If schema has changed, we may not be able to deserialize it. Remember that we don't yet have a schema on server.
		if (core.getComptime().getComponent() != VariantComptime.Component.SERVER && !core.getSchema().getId().equals(schidObj)) {
			throw new VariantSchemaModifiedException(core.getSchema().getId(), (String)schidObj);
		}
CLEANUP */									
		Object tsObj = parsedJson.get(FIELD_NAME_TIMESTAMP);
		if (tsObj == null) 
			throw new RuntimeInternalException("No timestamp");
		if (!(tsObj instanceof Number)) 
			throw new RuntimeInternalException("Timestamp is not number");

		timestamp = ((Number)tsObj).longValue();
		
		Object currentRequestObj = parsedJson.get(FIELD_NAME_CURRENT_REQUEST);
		if (currentRequestObj != null) {
			if (!(currentRequestObj instanceof Map<?,?>)) 
			throw new RuntimeInternalException("currentRequest not map");
			currentRequest = CoreStateRequest.fromJson(schema, this, (Map<String,?>)currentRequestObj);
		}
		
		SessionScopedTargetingStabile targetingStabile = new SessionScopedTargetingStabile();
		Object stabileObj = parsedJson.get(FIELD_NAME_TARGETING_STABIL);
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
				throw new RuntimeInternalException("Unable to deserialzie session: bad stabil spec", e);
			}
		}
		setTargetingStabile(targetingStabile);

		// If server, don't deserialize traversed tests and states because we don't have the schema.
		/* Have schema now on both sides.
		if (core.getComptime().getComponent() != VariantComptime.Component.SERVER) {
		*/
		Object statesObj = parsedJson.get(FIELD_NAME_TRAVERSED_STATES);
		if (statesObj != null) {
			HashMap<State,Integer> statesMap = new HashMap<State, Integer>();
			try {
				List<?> statesListRaw = (List<?>) statesObj; 
				for (Object obj: statesListRaw) {
					Map<?,?> objMap = (Map<?,?>) obj;
					String stateName = (String) objMap.get(FIELD_NAME_STATE);
					State state = schema.getState(stateName);
					Integer count =  (Integer) objMap.get(FIELD_NAME_COUNT);
					statesMap.put(state, count);
				}
			}
			catch (Exception e) {
				throw new RuntimeInternalException("Unable to deserialzie session: bad states spec", e);
			}
			traversedStates = statesMap;
		}
	
		Object testsObj = parsedJson.get(FIELD_NAME_TRAVERSED_TESTS);
		if (testsObj != null) {
			LinkedHashSet<Test> tests = new LinkedHashSet<Test>();
			try {
				List<String> testList = (List<String>) testsObj; 
				for (String testName: testList) {
					tests.add(schema.getTest(testName));
				}
			}
			catch (Exception e) {
				throw new RuntimeInternalException("Unable to deserialzie session: bad tests spec", e);
			}
			traversedTests = tests;
		}
		
		testsObj = parsedJson.get(FIELD_NAME_DISQUAL_TESTS);
		if (testsObj != null) {
			
			LinkedHashSet<Test> tests = new LinkedHashSet<Test>();
		
			try {
				List<String> testList = (List<String>) testsObj; 
				for (String testName: testList) {
					tests.add(schema.getTest(testName));
				
				}
			}
			catch (Exception e) {
				throw new RuntimeInternalException("Unable to deserialzie session: bad disqual tests spec", e);
			}
			disqualTests = tests;
		}
/*		}		CLEANUP */
	}

	@Override
	public boolean equals(Object o) {
		try {
			CoreSession other = (CoreSession) o;
			return sid.equals(other.sid);
		}
		catch(ClassCastException e) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return sid.hashCode();
	}
	
}

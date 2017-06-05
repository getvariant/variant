package com.variant.core.session;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.CoreException;
import com.variant.core.VariantException;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

/**
 * 
 * @author Igor
 *
 */
public class CoreSession implements Serializable {
	
	///
	private static final long serialVersionUID = 1L;

	private String sid;
	private Date createDate = new Date();
	private CoreStateRequest currentRequest = null;
	private LinkedHashMap<State, Integer> traversedStates = new LinkedHashMap<State, Integer>();
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
	public CoreSession(String id, Schema schema) {
		this.sid = id;		
		this.schema = schema;
	}
	
	/**
	 * Deserialized session from annotated JSON.
	 * @param json
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static CoreSession fromJson(String annotatedJson, Schema schema) {
		try {
			ObjectMapper mapper = new ObjectMapper();		
			Map<String,?> mappedJson = mapper.readValue(annotatedJson, Map.class);
			return fromJson(mappedJson, schema);
		}
		catch (VariantException e) {
			throw e;
		}
		catch (Exception e) {
			throw new CoreException.Internal("Unable to deserialzie session: [" + annotatedJson + "]", e);
		}

	}

	/**
	 * Deserialize from parsed JSON
	 * @param json
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static CoreSession fromJson(Map<String,?> parsedJson, Schema schema) {
		
		CoreSession result = new CoreSession(null, schema);
		
		Object idObj = parsedJson.get(FIELD_NAME_ID);
		if (idObj == null) 
			throw new CoreException.Internal("No id");
		if (!(idObj instanceof String)) 
			throw new CoreException.Internal("id not string");
		result.sid = (String)idObj;
		
		Object schidObj = parsedJson.get(FIELD_NAME_SCHEMA_ID);
		if (schidObj == null ) 
			throw new CoreException.Internal("No schema id");
		if (!(schidObj instanceof String)) 
			throw new CoreException.Internal("Schema id not string");

		Object tsObj = parsedJson.get(FIELD_NAME_TIMESTAMP);
		if (tsObj == null) 
			throw new CoreException.Internal("No timestamp");
		if (!(tsObj instanceof Number)) 
			throw new CoreException.Internal("Timestamp is not number");

		result.createDate = new Date(((Long)tsObj).longValue());
		
		Object currentRequestObj = parsedJson.get(FIELD_NAME_CURRENT_REQUEST);
		if (currentRequestObj != null) {
			if (!(currentRequestObj instanceof Map<?,?>)) 
			throw new CoreException.Internal("currentRequest not map");
			result.currentRequest = CoreStateRequest.fromJson(schema, result, (Map<String,?>)currentRequestObj);
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
				throw new CoreException.Internal("Unable to deserialzie session: bad stabil spec", e);
			}
		}
		result.setTargetingStabile(targetingStabile);

		Object statesObj = parsedJson.get(FIELD_NAME_TRAVERSED_STATES);
		if (statesObj != null) {
			LinkedHashMap<State,Integer> statesMap = new LinkedHashMap<State, Integer>();
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
				throw new CoreException.Internal("Unable to deserialzie session: bad states spec", e);
			}
			result.traversedStates = statesMap;
		}
	
		Object testsObj = parsedJson.get(FIELD_NAME_TRAVERSED_TESTS);
		if (testsObj != null) {
			LinkedHashSet<Test> tests = new LinkedHashSet<Test>();
			try {
				List<String> testList = (List<String>) testsObj; 
				for (String test: testList) {
					tests.add(schema.getTest(test));
				}
			}
			catch (Exception e) {
				throw new CoreException.Internal("Unable to deserialzie session: bad tests spec", e);
			}
			result.traversedTests = tests;
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
				throw new CoreException.Internal("Unable to deserialzie session: bad disqual tests spec", e);
			}
			result.disqualTests = tests;
		}
		return result;
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//	
	/**
	 * 
	 */
	public String getId() {
		return sid;
	}

	/**
	 * 
	 */
	public Schema getSchema() {
		return schema;
	}

	public Date createDate() {
		return createDate;
	}
	
	public CoreStateRequest getStateRequest() {
		return currentRequest;
	}

	public Map<State, Integer> getTraversedStates() {
		return Collections.unmodifiableMap(traversedStates);
	}

	public Set<Test> getTraversedTests() {
		return Collections.unmodifiableSet(traversedTests);
	}

	public Set<Test> getDisqualifiedTests() {
		return Collections.unmodifiableSet(disqualTests);
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//	
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
			throw new CoreException.Internal(
					String.format("Test [%s] already contained in the traversed list", test.getName()));
		
		traversedTests.add(test);
	}

	/**
	 * 
	 * @param test
	 */
	public void addDisqualifiedTest(Test test) {

		if (disqualTests.contains(test)) {
				throw new CoreException.Internal(
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
	private static final String FIELD_NAME_DISQUAL_TESTS = "disqualTests";
	private static final String FIELD_NAME_STATE = "state";
	private static final String FIELD_NAME_COUNT = "count";
	private static final String FIELD_NAME_TARGETING_STABIL = "stabil";
	
	/**
	 * Serialize as JSON.
	 * @return
	 * @throws JsonProcessingException 
	 */
	public String toJson() {
		try {
			StringWriter result = new StringWriter(2048);
			JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
			jsonGen.writeStartObject();
			jsonGen.writeStringField(FIELD_NAME_ID, sid);
			jsonGen.writeNumberField(FIELD_NAME_TIMESTAMP, createDate.getTime());
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
			throw new CoreException.Internal("Unable to serialize session", e);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		try {
			CoreSession other = (CoreSession) o;
			return other != null && sid.equals(other.sid);
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

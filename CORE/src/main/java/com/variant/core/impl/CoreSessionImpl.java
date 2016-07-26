package com.variant.core.impl;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import static com.variant.core.schema.impl.MessageTemplate.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.impl.EventWriter;
import com.variant.core.event.impl.PersistableEventImpl;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.exception.VariantSchemaModifiedException;
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
public class CoreSessionImpl implements VariantSession, Serializable {
	
	///
	private static final long serialVersionUID = 1L;

	private String id;
	private long timestamp = System.currentTimeMillis();
	private CoreStateRequestImpl currentRequest = null;
	private HashMap<State, Integer> traversedStates = new HashMap<State, Integer>();
	private LinkedHashSet<Test> traversedTests = new LinkedHashSet<Test>();
	private LinkedHashSet<Test> disqualTests = new LinkedHashSet<Test>();
	private VariantCore core;
	private SessionScopedTargetingStabile targetingStabile = new SessionScopedTargetingStabile();
	private String schemaId;
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param id
	 */
	public CoreSessionImpl(String id, VariantCore core) {
		
		this.core = core;

		if (core.getComptime().getComponent() == VariantComptime.Component.SERVER)
			throw new VariantInternalException("Cannot create schema on Server");

		if (core.getSchema() == null) 
			throw new VariantRuntimeUserErrorException(RUN_SCHEMA_UNDEFINED);
		
		// No schema ID on server yet. 
		if (core.getComptime().getComponent() != VariantComptime.Component.SERVER) 
			this.schemaId = core.getSchema().getId();
		this.id = id;
		
	}
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
		EventWriter ew = ((VariantCore) core).getEventWriter();
		ew.write(new PersistableEventImpl(event, this));
	}

	/**
	 * 
	 */
	@Override
	public VariantStateRequest targetForState(State state) {
				
		checkState();
		
		// Can't have two requests at one time
		if (currentRequest != null && !currentRequest.isCommitted()) {
			throw new VariantRuntimeUserErrorException(RUN_ACTIVE_REQUEST);
		}
				
		return core.getRuntime().targetSessionForState(this, (StateImpl) state);
	}

	/**
	 * No notion of expiration in core.
	 */
	@Override
	public boolean isExpired() {
		throw new IllegalArgumentException();
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @return
	 */
	public VariantCore getCoreApi() {
		return core;
	}
	
	/**
	 * 
	 * @param req
	 */
	public void setStateRequest(CoreStateRequestImpl req) {
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
	 * 
	 */
	public void save() {
		core.getSessionService().saveSession(this);
	}
	
	public void checkState() {
		if (!schemaId.equals(core.getSchema().getId())) {
			throw new VariantSchemaModifiedException(core.getSchema().getId(), schemaId);		
		}
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                       Serialization                                          //
	//---------------------------------------------------------------------------------------------//

	private static final String FIELD_NAME_ID = "sid";
	private static final String FIELD_NAME_TIMESTAMP = "time";
	private static final String FIELD_NAME_SCHEMA_ID = "schid";
	private static final String FIELD_NAME_CURRENT_REQUEST = "req";
	private static final String FIELD_NAME_TRAVERSED_STATES = "ts";
	private static final String FIELD_NAME_TRAVERSED_TESTS = "tts";
	private static final String FIELD_NAME_DISQUAL_TESTS = "dts";
	private static final String FIELD_NAME_STATE = "state";
	private static final String FIELD_NAME_COUNT = "count";
	private static final String FIELD_NAME_TARGETING_STABIL = "stabil";
	
	/**
	 * Serialize as JSON.
	 * @return
	 * @throws JsonProcessingException 
	 */
	public String toJson() throws VariantRuntimeException {
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
			throw new VariantInternalException("Unable to serialize session", e);
		}
	}

	/**
	 * Deserialize from JSON
	 * @param json
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static CoreSessionImpl fromJson(VariantCore core, String rawJson) {
		
		try {
			ObjectMapper mapper = new ObjectMapper();		
			Map<String,?> fields = mapper.readValue(rawJson, Map.class);
			return fromJson(core, fields);
		}
		catch (VariantRuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to deserialzie session: [" + rawJson + "]", e);
		}

	}
	
	/**
	 * Deserialize from parsed JSON
	 * @param json
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static CoreSessionImpl fromJson(VariantCore core, Map<String,?> parsedJson) {
		
		Object idObj = parsedJson.get(FIELD_NAME_ID);
		if (idObj == null) 
			throw new VariantInternalException("No id");
		if (!(idObj instanceof String)) 
			throw new VariantInternalException("id not string");

		Object schidObj = parsedJson.get(FIELD_NAME_SCHEMA_ID);
		if (schidObj == null ) 
			throw new VariantInternalException("No schema id");
		if (!(schidObj instanceof String)) 
			throw new VariantInternalException("Schema id not string");

		// If schema has changed, we may not be able to deserialize it. Remember that we don't yet have a schema on server.
		if (core.getComptime().getComponent() != VariantComptime.Component.SERVER && !core.getSchema().getId().equals(schidObj)) {
			throw new VariantSchemaModifiedException(core.getSchema().getId(), (String)schidObj);
		}
					
		CoreSessionImpl result = new CoreSessionImpl((String)idObj, core);
				
		Object tsObj = parsedJson.get(FIELD_NAME_TIMESTAMP);
		if (tsObj == null) 
			throw new VariantInternalException("No timestamp");
		if (!(tsObj instanceof Number)) 
			throw new VariantInternalException("Timestamp is not number");

		result.timestamp = ((Number)tsObj).longValue();
		
		Object currentRequestObj = parsedJson.get(FIELD_NAME_CURRENT_REQUEST);
		if (currentRequestObj != null) {
			if (!(currentRequestObj instanceof Map<?,?>)) 
			throw new VariantInternalException("currentRequest not map");
			result.currentRequest = CoreStateRequestImpl.fromJson(core, result, (Map<String,?>)currentRequestObj);
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
				throw new VariantInternalException("Unable to deserialzie session: bad statil spec", e);
			}
		}
		result.setTargetingStabile(targetingStabile);

		// If server, don't deserialize traversed tests and states because we don't have the schema.
		if (core.getComptime().getComponent() != VariantComptime.Component.SERVER) {
			Object statesObj = parsedJson.get(FIELD_NAME_TRAVERSED_STATES);
			if (statesObj != null) {
				HashMap<State,Integer> statesMap = new HashMap<State, Integer>();
				try {
					List<?> statesListRaw = (List<?>) statesObj; 
					for (Object obj: statesListRaw) {
						Map<?,?> objMap = (Map<?,?>) obj;
						String stateName = (String) objMap.get(FIELD_NAME_STATE);
						State state = core.getSchema().getState(stateName);
						Integer count =  (Integer) objMap.get(FIELD_NAME_COUNT);
						statesMap.put(state, count);
					}
				}
				catch (Exception e) {
					throw new VariantInternalException("Unable to deserialzie session: bad states spec", e);
				}
				result.traversedStates = statesMap;
			}
		
			Object testsObj = parsedJson.get(FIELD_NAME_TRAVERSED_TESTS);
			if (testsObj != null) {
				LinkedHashSet<Test> tests = new LinkedHashSet<Test>();
				try {
					List<String> testList = (List<String>) testsObj; 
					for (String testName: testList) {
						tests.add(core.getSchema().getTest(testName));
					}
				}
				catch (Exception e) {
					throw new VariantInternalException("Unable to deserialzie session: bad tests spec", e);
				}
				result.traversedTests = tests;
			}
			
			testsObj = parsedJson.get(FIELD_NAME_DISQUAL_TESTS);
			if (testsObj != null) {
				
				LinkedHashSet<Test> tests = new LinkedHashSet<Test>();
			
				try {
					List<String> testList = (List<String>) testsObj; 
					for (String testName: testList) {
						tests.add(core.getSchema().getTest(testName));
					
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

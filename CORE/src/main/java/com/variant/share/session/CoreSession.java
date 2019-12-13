package com.variant.share.session;

import java.io.Serializable;
import java.io.StringWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.share.error.CoreException;
import com.variant.share.error.VariantException;
import com.variant.share.schema.Schema;
import com.variant.share.schema.State;
import com.variant.share.schema.Variation;
import com.variant.share.schema.Variation.Experience;

/**
 * 
 * @author Igor
 *
 */
@SuppressWarnings("serial")
public class CoreSession implements Serializable {
	
	private String sid;
	private Instant timestamp = Instant.now();
	private LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>();
	private CoreStateRequest currentRequest = null;
	private LinkedHashMap<State, Integer> traversedStates = new LinkedHashMap<State, Integer>();
	private LinkedHashSet<Variation> traversedVariations = new LinkedHashSet<Variation>();
	private LinkedHashSet<Variation> disqualVariations = new LinkedHashSet<Variation>();
	private SessionScopedTargetingStabile targetingStabile = new SessionScopedTargetingStabile();
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Brand new session
	 * @param id
	 */
	public CoreSession(String id) {
		this.sid = id;		
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

	public Instant getTimestamp() {
		return timestamp;
	}
	
	public Optional<CoreStateRequest> getStateRequest() {
		return Optional.ofNullable(currentRequest);
	}

	public Map<State, Integer> getTraversedStates() {
		return Collections.unmodifiableMap(traversedStates);
	}

	public Set<Variation> getTraversedVariations() {
		return Collections.unmodifiableSet(traversedVariations);
	}

	public Set<Variation> getDisqualifiedVariations() {
		return Collections.unmodifiableSet(disqualVariations);
	}

	public Set<Experience> getLiveExperiences(Schema schema) {
		return targetingStabile.getAllAsExperiences(schema);
	}
	
	public Map<String,String> getAttributes() {
		return attributes;
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
	public void addTraversedVariation(Variation var) {

		if (traversedVariations.contains(var)) 
			throw new CoreException.Internal(
					String.format("Variation [%s] already contained in the traversed list", var.getName()));
		
		traversedVariations.add(var);
	}

	/**
	 * 
	 * @param test
	 */
	public void addDisqualifiedTest(Variation var) {

		if (disqualVariations.contains(var)) {
				throw new CoreException.Internal(
						String.format("Variation [%s] already contained in the disqual list", var.getName()));
		}	
		disqualVariations.add(var);
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

	//---------------------------------------------------------------------------------------------//
	//                                       Serialization                                          //
	//---------------------------------------------------------------------------------------------//

	private static final String FIELD_NAME_ATTRIBUTES = "attrs";
	private static final String FIELD_NAME_COUNT = "count";
	private static final String FIELD_NAME_DISQUAL_TESTS = "disqualTests";
	private static final String FIELD_NAME_CURRENT_REQUEST = "request";
	private static final String FIELD_NAME_ID = "sid";
	private static final String FIELD_NAME_TARGETING_STABILE = "stab";
	private static final String FIELD_NAME_STATE = "state";
	private static final String FIELD_NAME_TRAVERSED_STATES = "states";
	private static final String FIELD_NAME_TRAVERSED_TESTS = "tests";
	private static final String FIELD_NAME_TIMESTAMP = "ts";
	
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
			jsonGen.writeStringField(FIELD_NAME_TIMESTAMP, DateTimeFormatter.ISO_INSTANT.format(timestamp));
			
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
			
			if (attributes.size() > 0) {
				jsonGen.writeObjectFieldStart(FIELD_NAME_ATTRIBUTES);
				for (Map.Entry<String, String> e: attributes.entrySet()) {
					jsonGen.writeStringField(e.getKey(), e.getValue());
				}
				jsonGen.writeEndObject();
			}

			if (traversedVariations.size() > 0) {
				jsonGen.writeArrayFieldStart(FIELD_NAME_TRAVERSED_TESTS);
				for (Variation t: traversedVariations) {
					jsonGen.writeString(t.getName());
				}
				jsonGen.writeEndArray();
			}
			
			if (disqualVariations.size() > 0) {
				jsonGen.writeArrayFieldStart(FIELD_NAME_DISQUAL_TESTS);
				for (Variation t: disqualVariations) {
					jsonGen.writeString(t.getName());
				}
				jsonGen.writeEndArray();
			}

			if (targetingStabile.size() > 0) {
				jsonGen.writeArrayFieldStart(FIELD_NAME_TARGETING_STABILE);
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
		
		CoreSession result = new CoreSession(null);
		
		Object idObj = parsedJson.get(FIELD_NAME_ID);
		if (idObj == null) 
			throw new CoreException.Internal("No id");
		if (!(idObj instanceof String)) 
			throw new CoreException.Internal("id not string");
		result.sid = (String)idObj;
		
		Object tsObj = parsedJson.get(FIELD_NAME_TIMESTAMP);
		if (tsObj == null) 
			throw new CoreException.Internal("No timestamp");
		if (!(tsObj instanceof String)) 
			throw new CoreException.Internal("Timestamp is not string");

		result.timestamp = Instant.parse((String)tsObj);
		
		Object currentRequestObj = parsedJson.get(FIELD_NAME_CURRENT_REQUEST);
		if (currentRequestObj != null) {
			if (!(currentRequestObj instanceof Map<?,?>)) 
			throw new CoreException.Internal("currentRequest not map");
			result.currentRequest = CoreStateRequest.fromJson(schema, result, (Map<String,?>)currentRequestObj);
		}
		
		SessionScopedTargetingStabile targetingStabile = new SessionScopedTargetingStabile();
		Object stabileObj = parsedJson.get(FIELD_NAME_TARGETING_STABILE);
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
					State state = schema.getState(stateName).get();
					Integer count =  (Integer) objMap.get(FIELD_NAME_COUNT);
					statesMap.put(state, count);
				}
			}
			catch (Exception e) {
				throw new CoreException.Internal("Unable to deserialzie session: bad states spec", e);
			}
			result.traversedStates = statesMap;
		}
	
		
		Object attributesObj = parsedJson.get(FIELD_NAME_ATTRIBUTES);
		if (attributesObj != null) {
			try {
				Map<String,String> attributesMap = (Map<String,String>) attributesObj; 
				for (Map.Entry<String, String> attribute: attributesMap.entrySet()) {
					result.attributes.put(attribute.getKey(), attribute.getValue());
				}
			}
			catch (Exception e) {
				throw new CoreException.Internal("Unable to deserialzie session: bad attributes spec", e);
			}
		}

		
		
		Object testsObj = parsedJson.get(FIELD_NAME_TRAVERSED_TESTS);
		if (testsObj != null) {
			LinkedHashSet<Variation> vars = new LinkedHashSet<Variation>();
			try {
				List<String> testList = (List<String>) testsObj; 
				for (String test: testList) {
					vars.add(schema.getVariation(test).get());
				}
			}
			catch (Exception e) {
				throw new CoreException.Internal("Unable to deserialzie session: bad tests spec", e);
			}
			result.traversedVariations = vars;
		}
		
		testsObj = parsedJson.get(FIELD_NAME_DISQUAL_TESTS);
		if (testsObj != null) {
			LinkedHashSet<Variation> vars = new LinkedHashSet<Variation>();
			try {
				List<String> testList = (List<String>) testsObj; 
				for (String testName: testList) {
					vars.add(schema.getVariation(testName).get());
				
				}
			}
			catch (Exception e) {
				throw new CoreException.Internal("Unable to deserialzie session: bad disqual tests spec", e);
			}
			result.disqualVariations = vars;
		}
		return result;
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
	
	public static void main(String[] args) {
		System.out.println(Instant.now().toString());
		System.out.println(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
	}
}

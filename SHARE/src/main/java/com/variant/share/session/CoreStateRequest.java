package com.variant.share.session;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.share.error.CoreException;
import com.variant.share.error.ServerError;
import com.variant.share.schema.Schema;
import com.variant.share.schema.State;
import com.variant.share.schema.StateVariant;
import com.variant.share.schema.Variation;
import com.variant.share.schema.Variation.Experience;
import com.variant.share.schema.impl.StateImpl;
import com.variant.share.util.CaseInsensitiveLinkedMap;
import com.variant.share.util.CollectionsUtils;
import com.variant.share.util.immutable.CaseInsensitiveImmutableMap;

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
	private Optional<StateVariant> resolvedVariant;
	//private Map<String,String> resolvedParameterMap;
	
	// Active
	private Set<Experience> liveExperiences; 
	
	private Status status = Status.InProgress;
	
	/**
	 * Regular constructor
	 * @param session
	 */
	public CoreStateRequest(CoreSession session, State state) {
		this.session = session;
		this.state = (StateImpl) state;
		session.setStateRequest(this);
		resolvedVariant = Optional.empty();
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * State of a state request.  We replicate this on both client and server,
	 * so as not to have any public core classes other than schema. 
	 * 
	 * @since 0.9
	 */

	public enum Status {

		InProgress, Committed, Failed;

		/**
		 * Is a value one of the given values?
		 * 
		 * @param statuses
		 * @return ture if this value is one of the given values, false otherwise.
		 */
		public boolean isIn(Status... statuses) {
			for (Status s: statuses) if (this == s) return true;
			return false;
		}
	}

	/**
	 */
	public CoreSession getSession() {
		return session;
	}

	/**
	 */
	public State getState() {
		return state;
	}

	/**
	 */
	public void setStatus(Status targetStatus) {
		
		if (status == Status.Committed && targetStatus == Status.Failed)
			throw new CoreException.User(ServerError.CANNOT_FAIL);
		else if (status == Status.Failed && targetStatus == Status.Committed)
			throw new CoreException.User(ServerError.CANNOT_COMMIT);
		else
			status = targetStatus;
	}

	/**
	 * 
	 * @return
	 */
	public Status getStatus() {
		return status;
	}

	public Optional<StateVariant> getResolvedStateVariant() {
		return resolvedVariant;
	}
	
	/**
	 * Immutable parameter map, if need all.
	 * @return
	 */
	public  Map<String,String> getResolvedParameters() {
		
		Map<String,String> result = new CaseInsensitiveLinkedMap<String>();
		
		if (resolvedVariant.isPresent() && resolvedVariant.get().getParameters().isPresent()) {
			if (state.getParameters().isPresent()) {
				// We have two maps to merge. 
				CollectionsUtils.mapMerge(result, state.getParameters().get(), resolvedVariant.get().getParameters().get());
			}
			else {
				result.putAll(resolvedVariant.get().getParameters().get());
			}
		}
		else {
			if (state.getParameters().isPresent()) {
				result.putAll(state.getParameters().get());
			}
			else {
				// No params specified either at state variant not at state level. 
				// Default takes care of this case.
			}
		}

		// Wrap in an immutable map.
		return new CaseInsensitiveImmutableMap<String>(result);
	}
	
	/**
	 * Live experiences instrumented on this state.
	 * @return
	 */
	public synchronized Set<Experience> getLiveExperiences() {
		
		if (liveExperiences == null) {

			SessionScopedTargetingStabile stabile = session.getTargetingStabile();
			HashSet<Experience> result = new LinkedHashSet<Experience>();

			for (Variation var: state.getInstrumentedVariations()) {
				if (!var.isOn() || session.getDisqualifiedVariations().contains(var)) continue;
				SessionScopedTargetingStabile.Entry entry = stabile.get(var.getName());
				if (entry == null) throw new CoreException.Internal("Targeted experience for variation [" + var.getName() + "] expected but not found in sessioin.");
				result.add(var.getExperience(entry.getExperienceName()).get());
			}
			liveExperiences = result;
		}
		return liveExperiences;
	}

	/**
	 * Get live experience in a given Variation, if any.
	 * @param test
	 * @return
	 */
	public Optional<Experience> getLiveExperience(Variation var) {
		
		for (Experience e: getLiveExperiences())
			if  (e.getVariation().getName().equals(var.getName())) return Optional.of(e);
		return Optional.empty();
	}
	
	/**
	 * @param path
	 */
	public void setResolvedStateVariant(Optional<StateVariant> variant) {
		this.resolvedVariant = variant;
	}
		
	//---------------------------------------------------------------------------------------------//
	//                                       SERIALIZATION                                         //
	//---------------------------------------------------------------------------------------------//

	private static final String FIELD_NAME_STATE = "state";
	private static final String FILED_NAME_STATUS = "status";
//	private static final String FIELD_NAME_PARAMS = "params";  Should be able to compute them. 
//	private static final String FIELD_NAME_KEY = "name";
//	private static final String FIELD_NAME_VALUE = "value";
	private static final String FIELD_NAME_EXPERIENCES = "exps";
	private static final String FIELD_NAME_VARIANT = "variant";
	private static final String FIELD_NAME_TEST = "test";
	private static final String FIELD_NAME_OFFSET = "offset";

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
		jsonGen.writeNumberField(FILED_NAME_STATUS, status.ordinal());
		
/*
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
*/
		// If we have a resolved variant, simply record  its index in the variants
		// array. Here we rely on this index being deterministic.
		if (resolvedVariant.isPresent()) {
			StateVariant variant = resolvedVariant.get();
			jsonGen.writeObjectFieldStart(FIELD_NAME_VARIANT);
			jsonGen.writeStringField(FIELD_NAME_TEST, variant.getVariation().getName());
			int offset = 0;
			for (StateVariant var: variant.getOnState().getVariants()) {
				if (var == variant) break;
				offset++;
			}
			jsonGen.writeNumberField(FIELD_NAME_OFFSET, offset);
			jsonGen.writeEndObject();			
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
		
		String stateName = (String) fields.get(FIELD_NAME_STATE);
		if (stateName == null) 
			throw new CoreException.Internal("Unable to deserialzie request: no state");
		
		if (!(stateName instanceof String)) 
			throw new CoreException.Internal("Unable to deserialzie request: state not string");
		
		Optional<State> stateOpt = schema.getState(stateName);
		
		if (!stateOpt.isPresent()) 
			throw new CoreException.Internal(String.format("State [%s] not in schema [%s]", stateName, schema.getMeta().getName()));

		CoreStateRequest result = new CoreStateRequest(session, stateOpt.get());
				
		result.status = Status.values()[(Integer)fields.get(FILED_NAME_STATUS)];

/*		
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
*/
		Object experiencesListObj = fields.get(FIELD_NAME_EXPERIENCES);
		if (experiencesListObj != null) {
			result.liveExperiences = new LinkedHashSet<Experience>();
			try {
				List<?> experiencesListRaw = (List<?>) experiencesListObj; 
				for (Object obj: experiencesListRaw) {
					String expQualifiedName = (String) obj;
					// qualified name = testName.expName.bool - need to parse.
					String[] tokens = expQualifiedName.split("\\.");
					result.liveExperiences.add(schema.getVariation(tokens[0]).get().getExperience(tokens[1]).get());
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
			Variation.OnState vos = schema.getVariation(testName).get().getOnState(schema.getState(stateName).get()).get();
			result.resolvedVariant = Optional.of(vos.getVariants().toArray(new StateVariant[0])[offset]);
		}

		return result;
	}
	
}

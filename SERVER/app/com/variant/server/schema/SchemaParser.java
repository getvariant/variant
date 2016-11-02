package com.variant.server.schema;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.event.impl.util.VariantStringUtils;
import com.variant.core.exception.Error;
import com.variant.core.exception.Error.Severity;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.UserHooker;
import com.variant.core.impl.VariantCore;
import com.variant.core.xdm.State;
import com.variant.core.xdm.Test;
import com.variant.core.xdm.impl.Keywords;
import com.variant.core.xdm.impl.StateParsedHookImpl;
import com.variant.core.xdm.impl.StatesParser;
import com.variant.core.xdm.impl.TestParsedHookImpl;
import com.variant.core.xdm.impl.TestsParser;

public class SchemaParser implements Keywords {
	
	private static final Logger LOG = LoggerFactory.getLogger(SchemaParser.class);
	
	/**
	 * Convert JsonParseException to ParserError.
	 * @param parseException
	 * @return
	 */
	private static void toParserError(JsonParseException parseException, String rawInput, ParserResponseImpl response) {
		
		String rawMessage = parseException.getMessage();
		// Pull out the actual message: it's on the first line.
		StringBuilder message = new StringBuilder(rawMessage.substring(0, rawMessage.indexOf(System.lineSeparator())));

		// Pull out line and column info from exception message.
		// Message starts with the repeat of the entire input, so skip that.
		String tail = VariantStringUtils.splice(parseException.getMessage(), "line\\: \\d*\\, column\\: \\d*");

		// the remainder is something like '; line: 33, column: 4]'
		tail = tail.replaceAll("[^0-9,]","");
		String[] tokens = tail.split(",");
		int line = Integer.parseInt(tokens[0]);
		int column = Integer.parseInt(tokens[1]);
		response.addMessage(Error.PARSER_JSON_PARSE, line, column, message.toString(), rawInput);

	}

	/**
	 * Pre-parser pass:
	 * 1. Remove comments.
	 * 2. (TODO) variable expansion?
	 * 
	 * @param schema
	 * @return
	 */
	private static String preParse(String schema) {
		
		// Lose comments from // to eol.
		
		StringBuilder result = new StringBuilder();
		String lines[] = schema.split("\n");
		for (String line: lines) {
			
			int commentIndex = line.indexOf("//");
			
			if (commentIndex == 0) {
				// full line - skip.
				continue;
			}
			else if (commentIndex >= 0) {
				// partial line: remove from // to eol.
				result.append(line.substring(0, commentIndex));
			}
			else {
				result.append(line);
			}
			result.append("\n");
		}
		return result.toString();
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
		
	/**
	 * Parse schema from string.
	 * @param schemaAsJsonString
	 * @return
	 * @throws VariantRuntimeException
	 */
	@SuppressWarnings("unchecked")
	public static ParserResponseImpl parse(VariantCore coreApi, String schemaAsJsonString) throws VariantRuntimeException {
		
		ParserResponseImpl response = new ParserResponseImpl();
		
		ObjectMapper jacksonDataMapper = new ObjectMapper();
		jacksonDataMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		
		Map<String, ?> cbb = null;
		
		try {
			//cbb = jacksonDataMapper.readValue(configAsJsonString, ConfigBinderBean.class);
			cbb = jacksonDataMapper.readValue(preParse(schemaAsJsonString), Map.class);
		}
		catch(JsonParseException parseException) {
			toParserError(parseException, schemaAsJsonString, response);
		} 
		catch (Exception e) {
			ParserMessage err = response.addMessage(Error.INTERNAL, e.getMessage());
			LOG.error(err.getText(), e);
		}
		
		Severity highSeverity = response.highestMessageSeverity();
		if (highSeverity != null && highSeverity.greaterOrEqualThan(Severity.FATAL)) return response;
		
		// Clean map will contain only entries with expected clauses with keys uppercased 
		Map<String, Object> cleanMap = new LinkedHashMap<String, Object>();
		
		// Pass 1. Uppercase all the expected clauses to support case insensitive key words.
		for (Map.Entry<String, ?> entry: cbb.entrySet()) {
			if (VariantStringUtils.equalsIgnoreCase(entry.getKey(), KEYWORD_STATES, KEYWORD_TESTS)) {
				cleanMap.put(entry.getKey().toUpperCase(), entry.getValue());
			}
			else {
				response.addMessage(Error.PARSER_UNSUPPORTED_CLAUSE, entry.getKey());
			}
		}
		
		// Pass2. Look at all clauses.  Expected ones are already uppercased.
		Object states = cleanMap.get(KEYWORD_STATES.toUpperCase());
		if (states == null) {
			response.addMessage(Error.PARSER_NO_STATES_CLAUSE);
		}
		else {
			
			// Parse all states
			StatesParser.parseStates(response.getSchema(), states, response);
			
			// Post user hook listeners.
			UserHooker hooker = coreApi.getUserHooker();
			for (State state: response.getSchema().getStates()) {
				try {
					hooker.post(new StateParsedHookImpl(state, response));
				}
				catch (VariantRuntimeException e) {
					response.addMessage(Error.HOOK_LISTENER_EXCEPTION, StateParsedHookImpl.class.getName(), e.getMessage());
				}
			}
		}

		highSeverity = response.highestMessageSeverity();
		if (highSeverity != null && highSeverity.greaterOrEqualThan(Severity.FATAL)) return response;

		Object tests = cleanMap.get(KEYWORD_TESTS.toUpperCase());
		if (tests == null) {
			response.addMessage(Error.PARSER_NO_TESTS_CLAUSE);
		}
		else {
			
			// Parse all tests
			TestsParser.parseTests(tests, response);
			
			// Post user hook listeners.
			UserHooker hooker = coreApi.getUserHooker();
			for (Test test: response.getSchema().getTests()) {
				try {
					hooker.post(new TestParsedHookImpl(test, response));
				}
				catch (VariantRuntimeException e) {
					response.addMessage(Error.HOOK_LISTENER_EXCEPTION, TestParsedHookImpl.class.getName(), e.getMessage());
				}
			}
		}
				
		return response;
	}

}

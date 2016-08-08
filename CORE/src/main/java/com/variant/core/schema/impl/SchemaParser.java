package com.variant.core.schema.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.event.impl.util.VariantStringUtils;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.UserHooker;
import com.variant.core.impl.VariantCore;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserMessage.Severity;

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
		response.addMessage(MessageTemplate.PARSER_JSON_PARSE, line, column, message.toString(), rawInput);

	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
		
	/**
	 * Parse schema from string.
	 * @param configAsJsonString
	 * @return
	 * @throws VariantRuntimeException
	 */
	@SuppressWarnings("unchecked")
	public static ParserResponseImpl parse(VariantCore coreApi, String configAsJsonString) throws VariantRuntimeException {
		
		ParserResponseImpl response = new ParserResponseImpl();
		
		ObjectMapper jacksonDataMapper = new ObjectMapper();
		jacksonDataMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		
		Map<String, ?> cbb = null;
		
		try {
			//cbb = jacksonDataMapper.readValue(configAsJsonString, ConfigBinderBean.class);
			cbb = jacksonDataMapper.readValue(configAsJsonString, Map.class);
		}
		catch(JsonParseException parseException) {
			toParserError(parseException, configAsJsonString, response);
		} 
		catch (Exception e) {
			ParserMessage err = response.addMessage(MessageTemplate.INTERNAL, e.getMessage());
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
				response.addMessage(MessageTemplate.PARSER_UNSUPPORTED_CLAUSE, entry.getKey());
			}
		}
		
		// Pass2. Look at all clauses.  Expected ones are already uppercased.
		Object states = cleanMap.get(KEYWORD_STATES.toUpperCase());
		if (states == null) {
			response.addMessage(MessageTemplate.PARSER_NO_STATES_CLAUSE);
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
					response.addMessage(MessageTemplate.HOOK_LISTENER_EXCEPTION, StateParsedHookImpl.class.getName(), e.getMessage());
				}
			}
		}

		highSeverity = response.highestMessageSeverity();
		if (highSeverity != null && highSeverity.greaterOrEqualThan(Severity.FATAL)) return response;

		Object tests = cleanMap.get(KEYWORD_TESTS.toUpperCase());
		if (tests == null) {
			response.addMessage(MessageTemplate.PARSER_NO_TESTS_CLAUSE);
		}
		else {
			
			// Parse all tests
			TestsParser.parseTests(tests, response, coreApi.getProperties());
			
			// Post user hook listeners.
			UserHooker hooker = coreApi.getUserHooker();
			for (Test test: response.getSchema().getTests()) {
				try {
					hooker.post(new TestParsedHookImpl(test, response));
				}
				catch (VariantRuntimeException e) {
					response.addMessage(MessageTemplate.HOOK_LISTENER_EXCEPTION, TestParsedHookImpl.class.getName(), e.getMessage());
				}
			}
		}
				
		return response;
	}

}

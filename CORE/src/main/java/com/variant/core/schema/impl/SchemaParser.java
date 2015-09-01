package com.variant.core.schema.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.exception.VariantException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.Test;
import com.variant.core.schema.View;
import com.variant.core.schema.parser.MessageTemplate;
import com.variant.core.schema.parser.Severity;
import com.variant.core.schema.parser.TestParsedEventListener;
import com.variant.core.schema.parser.ViewParsedEventListener;
import com.variant.core.util.VariantStringUtils;

public class SchemaParser {
	
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
		response.addError(MessageTemplate.PARSER_JSON_PARSE, line, column, message.toString(), rawInput);

	}
	
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 * 
	 * @param configAsJsonString
	 * @return
	 * @throws VariantRuntimeException
	 */
	public static ParserResponseImpl parse(String configAsJsonString)
	throws VariantRuntimeException {
		return parse(configAsJsonString, null, null);
	}
	
	/**
	 * 
	 * @param configAsJsonString
	 * @param viewParsedEventListeners
	 * @param testParsedEventListeners
	 * @return
	 * @throws VariantRuntimeException
	 */
	@SuppressWarnings("unchecked")
	public static ParserResponseImpl parse(
			String configAsJsonString,
			List<ViewParsedEventListener> viewParsedEventListeners,
			List<TestParsedEventListener> testParsedEventListeners) 
	throws VariantRuntimeException {
		
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
		catch (Exception exception) {
			response.addError(MessageTemplate.INTERNAL, exception.getMessage());
		}
		
		if (response.highestMessageSeverity().greaterOrEqualThan(Severity.FATAL)) return response;
		
		// Clean map will contain only entries with expected clauses with keys uppercased 
		Map<String, Object> cleanMap = new LinkedHashMap<String, Object>();
		
		// Pass 1. Uppercase all the expected clauses to support case insensitive key words.
		for (Map.Entry<String, ?> entry: cbb.entrySet()) {
			if (VariantStringUtils.equalsIgnoreCase(entry.getKey(), "views", "tests")) {
				cleanMap.put(entry.getKey().toUpperCase(), entry.getValue());
			}
			else {
				response.addError(MessageTemplate.PARSER_UNSUPPORTED_CLAUSE, entry.getKey());
			}
		}
		
		// Pass2. Look at all clauses.  Expected ones are already uppercased.
		Object views = cleanMap.get("VIEWS");
		if (views == null) {
			response.addError(MessageTemplate.PARSER_NO_VIEWS_CLAUSE);
		}
		else {
			ViewsParser.parseViews(views, response);
			if (viewParsedEventListeners != null) {
				for (ViewParsedEventListener listener: viewParsedEventListeners) {
					for (View view: response.getSchema().getViews()) {
						try {
							listener.viewParsed(view);
						}
						catch (VariantException e) {
							response.addError(MessageTemplate.BOOT_PARSER_LISTENER_EXCEPTION, listener.getClass().getName(), e.getMessage(), view.getName());
						}
					}
				}
			}
		}

		if (response.highestMessageSeverity().greaterOrEqualThan(Severity.FATAL)) return response;

		Object tests = cleanMap.get("TESTS");
		if (tests == null) {
			response.addError(MessageTemplate.PARSER_NO_TESTS_CLAUSE);
		}
		else {
			TestsParser.parseTests(tests, response);
			if (testParsedEventListeners != null) {
				for (TestParsedEventListener listener: testParsedEventListeners) {
					for (Test test: response.getSchema().getTests()) {
						listener.testParsed(test);
					}
				}
			}
		}
				
		return response;
	}

}

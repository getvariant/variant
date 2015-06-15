package com.variant.core.config.parser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.Variant;
import com.variant.core.config.View;
import com.variant.core.util.StringUtils;

public class ConfigParser {
	
	/**
	 * Convert JsonParseException to ParserError.
	 * @param parseException
	 * @return
	 */
	private static void toParserError(JsonParseException parseException, String rawInput, ParserResponse response) {
		
		String rawMessage = parseException.getMessage();
		// Pull out the actual message: it's on the first line.
		String message = rawMessage.substring(0, rawMessage.indexOf(System.lineSeparator()));

		// Pull out line and column info from exception message.
		// Message starts with the repeat of the entire input, so skip that.
		String tail = parseException.getMessage().substring(rawInput.length()).trim();
		// the remainder is something like '; line: 33, column: 4]'
		tail = tail.replaceAll("[^0-9,]","");
		String[] tokens = tail.split(",");
		int line = Integer.parseInt(tokens[0]);
		int column = Integer.parseInt(tokens[1]);
		
		response.addError(ParserErrorTemplate.JSON_PARSE, line, column, message);

	}
	
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	public static ParserResponse parse(String configAsJsonString) {
		
		ParserResponse response = new ParserResponse();
		
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
			response.addError(ParserErrorTemplate.INTERNAL, exception.getMessage());
		}
		
		if (response.highestSeverity().greaterOrEqualThan(ParserError.Severity.FATAL)) return response;
		
		// Cean map will contain only entries with expected clauses with keys uppercased 
		Map<String, Object> cleanMap = new LinkedHashMap<String, Object>();
		
		// Pass 1. Uppercase all the expected clauses to support case insensitive key words.
		for (Map.Entry<String, ?> entry: cbb.entrySet()) {
			if (StringUtils.equalsIgnoreCase(entry.getKey(), "views", "tests")) {
				cleanMap.put(entry.getKey().toUpperCase(), entry.getValue());
			}
			else {
				response.addError(ParserErrorTemplate.UNSUPPORTED_CLAUSE, entry.getKey());
			}
		}
		
		// Pass2. Look at all clauses.  Expected ones are already uppercased.
		Object views = cleanMap.get("VIEWS");
		if (views == null) {
			response.addError(ParserErrorTemplate.NO_VIEWS_CLAUSE);
		}
		else {
			ViewsParser.parseViews(views, response);
		}

		if (response.highestSeverity().greaterOrEqualThan(ParserError.Severity.FATAL)) return response;

		Object tests = cleanMap.get("TESTS");
		if (tests == null) {
			response.addError(ParserErrorTemplate.NO_TESTS_CLAUSE);
		}
		else {
			TestsParser.parseTests(tests, response);
		}
		
		// Only replace config if no fatal errors.
		if (response.highestSeverity().lessThan(ParserError.Severity.FATAL)) {
			Variant.setConfig(response.getConfig());
		}
		
		return response;
	}

}

package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.ParserError.JSON_PARSE;
import static com.variant.core.schema.parser.ParserError.NO_STATES_CLAUSE;
import static com.variant.core.schema.parser.ParserError.NO_TESTS_CLAUSE;
import static com.variant.core.schema.parser.ParserError.UNSUPPORTED_CLAUSE;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.event.impl.util.VariantStringUtils;
import com.variant.core.exception.Error.Severity;
import com.variant.core.exception.RuntimeError;
import com.variant.core.exception.RuntimeInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.UserHooker;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

public class SchemaParser implements Keywords {
	
	//private static final Logger LOG = LoggerFactory.getLogger(SchemaParser.class);
	
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
		response.addMessage(JSON_PARSE, line, column, message.toString(), rawInput);

	}

	private UserHooker hooker;
	
	/**
	 * Schema pre-parser.
	 * 1. Remove comments.
	 * 2. (TODO) variable expansion?
	 * 
	 * @param schema
	 * @return
	 */
	private String preParse(String annotatedJsonString) {
		
		// Lose comments from // to eol.
		
		StringBuilder result = new StringBuilder();
		String lines[] = annotatedJsonString.split("\n");
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
	
	public SchemaParser(UserHooker hooker) {
		this.hooker = hooker;
	}
	
	/**
	 * Parse schema from input stream. 
	 * @param annotatedJsonStream
	 * @return
	 */
	public ParserResponse parse(InputStream annotatedJsonStream) {
		try {
			String input = IOUtils.toString(annotatedJsonStream);
			return parse(input);
		} catch (IOException e) {
			throw new RuntimeInternalException("Unable to read input from stream", e);
		}
	}

	/**
	 * Parse schema from string.
	 * @param annotatedJsonString
	 * @return
	 * @throws VariantRuntimeException
	 */
	@SuppressWarnings("unchecked")
	public ParserResponse parse(String annotatedJsonString) throws VariantRuntimeException {
		
		// 1. Pre-parser phase
		String cleanJsonString = preParse(annotatedJsonString);
		
		// 2. Syntactical phase.
		ParserResponseImpl response = new ParserResponseImpl();
		
		ObjectMapper jacksonDataMapper = new ObjectMapper();
		jacksonDataMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		
		Map<String, ?> mappedJson = null;
		
		try {
			//cbb = jacksonDataMapper.readValue(configAsJsonString, ConfigBinderBean.class);
			mappedJson = jacksonDataMapper.readValue(preParse(cleanJsonString), Map.class);
		}
		catch(JsonParseException parseException) {
			toParserError(parseException, cleanJsonString, response);
		} 
		catch (Exception e) {
			throw new RuntimeInternalException(e);
		}
		
		// Don't attempt to parse if JSON failed.
		if (response.hasMessages()) return response;
		
		// 3. Semantical phase.
		// Clean map will contain only entries with expected clauses with keys uppercased 
		Map<String, Object> cleanMap = new LinkedHashMap<String, Object>();
		
		// Pass 1. Uppercase all the expected clauses to support case insensitive key words.
		for (Map.Entry<String, ?> entry: mappedJson.entrySet()) {
			if (VariantStringUtils.equalsIgnoreCase(entry.getKey(), KEYWORD_STATES, KEYWORD_TESTS)) {
				cleanMap.put(entry.getKey().toUpperCase(), entry.getValue());
			}
			else {
				response.addMessage(UNSUPPORTED_CLAUSE, entry.getKey());
			}
		}
		
		// Pass2. Look at all clauses.  Expected ones are already uppercased.
		Object states = cleanMap.get(KEYWORD_STATES.toUpperCase());
		if (states == null) {
			response.addMessage(NO_STATES_CLAUSE);
		}
		else {
			
			// Parse all states
			StatesParser.parseStates(response.getSchema(), states, response);
			
			// Post user hook listeners.
			for (State state: response.getSchema().getStates()) {
				try {
					hooker.post(new StateParsedHookImpl(state, response));
				}
				catch (VariantRuntimeException e) {
					response.addMessage(RuntimeError.HOOK_LISTENER_EXCEPTION, StateParsedHookImpl.class.getName(), e.getMessage());
				}
			}
		}

		if (!response.getMessages(Severity.FATAL).isEmpty()) return response;

		Object tests = cleanMap.get(KEYWORD_TESTS.toUpperCase());
		if (tests == null) {
			response.addMessage(NO_TESTS_CLAUSE);
		}
		else {
			
			// Parse all tests
			TestsParser.parseTests(tests, response);
			
			// Post user hook listeners.
			for (Test test: response.getSchema().getTests()) {
				try {
					hooker.post(new TestParsedHookImpl(test, response));
				}
				catch (VariantRuntimeException e) {
					response.addMessage(RuntimeError.HOOK_LISTENER_EXCEPTION, TestParsedHookImpl.class.getName(), e.getMessage());
				}
			}
		}
				
		return response;
	}

}
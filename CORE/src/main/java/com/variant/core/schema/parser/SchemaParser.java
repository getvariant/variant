package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.ParserError.JSON_PARSE;
import static com.variant.core.schema.parser.ParserError.NO_META_CLAUSE;
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
import com.variant.core.CommonError;
import com.variant.core.CoreException;
import com.variant.core.UserError.Severity;
import com.variant.core.VariantException;
import com.variant.core.impl.UserHooker;
import com.variant.core.schema.Hook;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.util.VariantStringUtils;

/**
 * Client and Server side parsers will extend this. The principal difference is that 
 * there are no hooks on the client, but for generality here we post them via an 
 * abstract hooker.
 * 
 * @author Igor
 *
 */
public abstract class SchemaParser implements Keywords {
		
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
	
	/**
	 * Schema pre-parser.
	 * 1. Remove comments (// to EOL.
	 * 2. (TODO) variable expansion?
	 * 
	 * @param annotatedJsonString
	 * @return
	 */
	private String preParse(String annotatedJsonString) {
		
		// Pass 1. Remove //-style comments.
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
			result.append('\n');
		}		

		return result.toString();
	}
	
	/**
	 * Concrete implementations will supply a hooker.
	 * @return
	 */
	protected abstract UserHooker getHooker();

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
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
			throw new CoreException.Internal("Unable to read input from stream", e);
		}
	}

	/**
	 * Parse schema from string.
	 * @param annotatedJsonString
	 * @return
	 * @throws VariantRuntimeException
	 */
	@SuppressWarnings("unchecked")
	public ParserResponse parse(String annotatedJsonString) {
		
		// 1. Pre-parser phase
		String cleanJsonString = preParse(annotatedJsonString);
		
		// 2. Syntactical phase.
		ParserResponseImpl response = new ParserResponseImpl();
		
		response.setSchemaSrc(annotatedJsonString);
		
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
			throw new CoreException.Internal(e);
		}
		
		// Don't attempt to parse if JSON failed.
		if (response.hasMessages()) {
			response.clearSchema();
			return response;
		}
		
		// 3. Semantical phase.
		// Clean map will contain only entries with expected clauses AND keys uppercased.
		Map<String, Object> cleanMap = new LinkedHashMap<String, Object>();
		
		// Pass 1.
		for (Map.Entry<String, ?> entry: mappedJson.entrySet()) {
			if (VariantStringUtils.equalsIgnoreCase(entry.getKey(), KEYWORD_META, KEYWORD_STATES, KEYWORD_TESTS)) {
				cleanMap.put(entry.getKey().toUpperCase(), entry.getValue());
			}
			else {
				response.addMessage(UNSUPPORTED_CLAUSE, entry.getKey());
			}
		}
		
		// Pass 2. Look at all clauses.  Expected ones are already uppercased.
		
		Object meta = cleanMap.get(KEYWORD_META.toUpperCase());
		if (meta == null) {
			response.addMessage(NO_META_CLAUSE);
		}
		else {			
			// Parse meta info
			MetaParser.parse(meta, response);
			// Init all schema scoped hooks.
			for (Hook hook: response.getSchema().getHooks()) getHooker().initHook(hook, response);
		}

		Object states = cleanMap.get(KEYWORD_STATES.toUpperCase());
		if (states == null) {
			response.addMessage(NO_STATES_CLAUSE);
		}
		else {
			
			// Parse all states
			StatesParser.parse(states, response);
			
			// Post parse time user hooks.
			for (State state: response.getSchema().getStates()) {
				try {
					getHooker().post(new StateParsedLifecycleEventImpl(state, response));
				}
				catch (VariantException e) {
					response.addMessage(CommonError.HOOK_UNHANDLED_EXCEPTION, StateParsedLifecycleEventImpl.class.getName(), e.getMessage());
					throw e;
				}
			}
		}

		if (!response.getMessages(Severity.FATAL).isEmpty()) {
			response.clearSchema();
			return response;
		}

		Object tests = cleanMap.get(KEYWORD_TESTS.toUpperCase());
		if (tests == null) {
			response.addMessage(NO_TESTS_CLAUSE);
		}
		else {
			
			// Parse all tests
			TestsParser.parse(tests, response);
						
			// Post parse time user hooks.
			for (Test test: response.getSchema().getTests()) {
				try {
					getHooker().post(new TestParsedLifecycleEventImpl(test, response));
				}
				catch (VariantException e) {
					response.addMessage(CommonError.HOOK_UNHANDLED_EXCEPTION, TestParsedLifecycleEventImpl.class.getName(), e.getMessage());
				}
			}
			
			// Init all test scoped hooks.
			for (Test test: response.getSchema().getTests()) {
				for (Hook hook: test.getHooks()) getHooker().initHook(hook, response);
			}

		}
		
		if (response.hasMessages(Severity.ERROR)) response.clearSchema();
		
		return response;
	}

}

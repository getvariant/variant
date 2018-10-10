package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.UNSUPPORTED_PROPERTY;
import static com.variant.core.schema.parser.error.SyntaxError.JSON_SYNTAX_ERROR;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.UserError.Severity;
import com.variant.core.impl.CoreException;
import com.variant.core.schema.Hook;
import com.variant.core.schema.Meta;
import com.variant.core.schema.parser.error.SemanticError;
import com.variant.core.schema.parser.error.SyntaxError;
import com.variant.core.util.IoUtils;
import com.variant.core.util.StringUtils;

/**
 * Client and Server side parsers will extend this. The principal difference is that 
 * there are no hooks on the client, but for generality here we post them via an 
 * abstract hooker.
 * 
 * @author Igor
 *
 */
public abstract class SchemaParser implements Keywords {
		
	// Parser response object in progress, if any.
	private ParserResponse response = null;
	
	/**
	 * Convert JsonParseException to ParserError.
	 * @param parseException
	 * @return
	 */
	private static void toParserError(JsonParseException parseException, String schemaSrc, ParserResponse response) {
		
		String rawMessage = parseException.getMessage();
		// Pull out the actual message: it's on the first line.
		StringBuilder message = new StringBuilder(rawMessage.substring(0, rawMessage.indexOf(System.lineSeparator())));

		// Pull out line and column info from exception message.
		// Message starts with the repeat of the entire input, so skip that.
		String tail = StringUtils.splice(parseException.getMessage(), "line\\: \\d*\\, column\\: \\d*");

		// the remainder is something like '; line: 33, column: 4]'
		tail = tail.replaceAll("[^0-9,]","");
		String[] tokens = tail.split(",");
		int line = Integer.parseInt(tokens[0]);
		int column = Integer.parseInt(tokens[1]);
		response.addMessage(new SyntaxError.Location(schemaSrc, line, column), JSON_SYNTAX_ERROR, message.toString());

	}
	
	/**
	 * Schema pre-parser.
	 * 
	 * 1. Remove multi-line non-nesting comments.
	 *    Pay attention to when we're inside or outside of quotation marks (both single or double).
	 * 
	 * 2. Remove single line comments: // to EOL.
	 *    Pay attention to when we're inside or outside of quotation marks (both single or double):
	 *    '//' inside quotation mark is just part of a JSON string.
	 * 
	 * 3. (TODO) variable expansion?
	 * 
	 * @param annotatedJsonString
	 * @return
	 */
	private String preParse(String annotatedJsonString) {
		
		// Pass 1. Remove multi-line non-nesting comments.
		StringBuilder pass1 = new StringBuilder();

		boolean quoted = false;
		boolean comment = false;
		char prev = 0;  // Just for compiler's sake.

		for (char c: annotatedJsonString.toCharArray()) {

			// If we're not in a comment and holing a '/' but it wasn't followed by '*', 
			// echo it.
			if (!quoted && !comment && prev == '/' && c != '*') pass1.append(prev);

			switch (c) {

				case '"': 
				case '\'':
					if (!comment) {
						quoted = ! quoted;
						pass1.append(c);
					}
					break;

				case '/':
					//System.out.println("/ " + prev + " " + comment + " " + quoted);
					if (quoted) pass1.append(c);
					else if (comment && prev == '*') {
						comment = false;
						// This is special case: we don't want this echoed
						// at the top of next iteration.
						c = '\0';  
					}
					break;
					
				case '*':
					//System.out.println("* " + prev + " " + comment + " " + quoted);
					if (quoted) pass1.append(c);
					else if (prev == '/') {
						comment = true;
					}
					break;
				
				case '\n':
					// Don't loose line breaks, even inside of a multi-line comment,
					// so that the json parser's sytax error refers to the right line number in the source.
					pass1.append(c);
					break;
					
				default:
					if (!comment) pass1.append(c);

			}			
			prev = c;
		}

		// Pass 2. Remove single-line comments - from '//' to EOL.
		boolean first = true;
		quoted = false;
		comment = false;
		prev = 0;
		StringBuilder pass2 = new StringBuilder();

		for (char c: pass1.toString().toCharArray()) {
			
			if (first) {
				first = false;
				if (c != '/') pass2.append(c);
			}
			else {
				// If prev unquoted char was / and this one isn't, print the prev char as
				// we hold the first / in case it's followed by another /.
				if (!quoted && !comment && c != '/' && prev == '/') pass2.append('/');
				
				switch (c) {

				case '"': 
				case '\'':
					if (!comment) {
						quoted = ! quoted;
						pass2.append(c);
					}
					break;

				case '/':
					if (quoted) pass2.append(c);
					else if (prev == '/') comment = true;
					break;
					
				case '\n':
					comment = false;
					pass2.append(c);
					break;
					
				default:
					if (!comment) pass2.append(c);
				}
			}			
			prev = c;
		}


		// TODO: Pass 3. Expansion Variables.

		return pass2.toString();
	}
	
	/**
	 * Concrete implementations will supply comp and run time services which cannot be implemented
	 * in the core library..
	 * @return
	 */
	public abstract HooksService getHooksService();
	public abstract FlusherService getFlusherService();
	
	private static SemanticError.Location rootLocation = new SemanticError.Location("/");

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 * 
	 */
	public SchemaParser() {}
	
	/**
	 * Server will need access to reponse in progress.
	 * @return
	 */
	public ParserResponse responseInProgress() {
		return response;
	}

	/**
	 * Parse schema from input stream. 
	 * @param annotatedJsonStream
	 * @return
	 */
	public ParserResponse parse(InputStream annotatedJsonStream) {
		try {
			String input = IoUtils.toString(annotatedJsonStream);
			return parse(input);
		} catch (IOException e) {
			throw new CoreException.Internal("Unable to read input from stream", e);
		}
		finally {
			response = null;
		}
	}

	/**
	 * Parse schema from string.
	 * @param annotatedJsonString
	 * @return
	 * @throws VariantRuntimeException
	 */
	@SuppressWarnings("unchecked")
	public ParserResponse parse(String schemaSrc) {
		
		// 1. Pre-parser phase
		String schemaPreParsed = preParse(schemaSrc);
		
		// 2. Syntactical phase.
		response = new ParserResponse(this);
		
		response.setSchemaSrc(schemaSrc);
		
		ObjectMapper JacksonParser = new ObjectMapper();
		JacksonParser.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		
		Map<String, ?> mappedJson = null;
		
		try {
			//cbb = jacksonDataMapper.readValue(configAsJsonString, ConfigBinderBean.class);
			mappedJson = JacksonParser.readValue(schemaPreParsed, Map.class);
		}
		catch(JsonParseException parseException) {
			toParserError(parseException, schemaSrc, response);
		} 
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
		
		// Don't attempt to parse if bad JSON syntax.
		if (response.hasMessages()) {
			response.clearSchema();
			return response;
		}
	
		// 3. Semantical phase.

		// Pass 1.
		// Clean map will contain only expected clauses AND keys uppercased.
		Map<String, Object> cleanMap = new LinkedHashMap<String, Object>();
		for (Map.Entry<String, ?> entry: mappedJson.entrySet()) {
			if (StringUtils.equalsIgnoreCase(entry.getKey(), KEYWORD_META, KEYWORD_STATES, KEYWORD_VARIATIONS)) {
				cleanMap.put(entry.getKey().toUpperCase(), entry.getValue());
			}
			else {
				response.addMessage(rootLocation.plusProp(entry.getKey()), UNSUPPORTED_PROPERTY, entry.getKey());
			}
		}
		
		// Pass 2. Let the actual parsing commence.
		HooksService hooksService = getHooksService();
		FlusherService flusherService = getFlusherService();
		
		Object metaObj = cleanMap.get(KEYWORD_META.toUpperCase());
		if (metaObj == null) {
			response.addMessage(rootLocation, PROPERTY_MISSING, KEYWORD_META);
		}
		else {			
			// Parse meta section
			MetaParser.parse(metaObj, rootLocation.plusObj(KEYWORD_META), response);
						
			// If we have the Meta section, init all schema scoped hooks and the flusher.
			Meta meta = response.getSchema().getMeta();
			if (meta != null) {
				for (Hook hook: meta.getHooks()) hooksService.initHook(hook, response);

				// If no flusher was supplied, flusher service is expected to init the default flusher.
				flusherService.initFlusher(meta.getFlusher());
			}
		}

		Object states = cleanMap.get(KEYWORD_STATES.toUpperCase());
		if (states == null) {
			response.addMessage(rootLocation, PROPERTY_MISSING, KEYWORD_STATES);
		}
		else {
			// Parse all states
			StatesParser.parse(states, rootLocation, response, hooksService);		
		}

		if (!response.getMessages(Severity.FATAL).isEmpty()) {
			response.clearSchema();
			return response;
		}

		Object tests = cleanMap.get(KEYWORD_VARIATIONS.toUpperCase());
		if (tests == null) {
			response.addMessage(rootLocation, PROPERTY_MISSING, KEYWORD_VARIATIONS);
		}
		else {
			// Parse all tests
			VariationsParser.parse(tests, rootLocation.plusObj(KEYWORD_VARIATIONS), response, hooksService);			
		}
		
		response.setMessageListener(null);
		
		if (response.hasMessages(Severity.ERROR)) response.clearSchema();
		
		return response;
	}

}

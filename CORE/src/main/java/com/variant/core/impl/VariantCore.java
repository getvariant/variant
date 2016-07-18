package com.variant.core.impl;

import static com.variant.core.schema.impl.MessageTemplate.BOOT_CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN;
import static com.variant.core.schema.impl.MessageTemplate.BOOT_CONFIG_FILE_NOT_FOUND;
import static com.variant.core.schema.impl.MessageTemplate.BOOT_CONFIG_RESOURCE_NOT_FOUND;
import static com.variant.core.schema.impl.MessageTemplate.BOOT_EVENT_PERSISTER_NO_INTERFACE;
import static com.variant.core.schema.impl.MessageTemplate.INTERNAL;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.VariantCorePropertyKeys;
import com.variant.core.VariantCoreSession;
import com.variant.core.event.EventPersister;
import com.variant.core.event.impl.EventWriter;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.hook.HookListener;
import com.variant.core.hook.UserHook;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.Test.OnState.Variant;
import com.variant.core.schema.impl.ParserResponseImpl;
import com.variant.core.schema.impl.SchemaImpl;
import com.variant.core.schema.impl.SchemaParser;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.Severity;
import com.variant.core.session.CoreSessionService;
import com.variant.core.util.VariantIoUtils;

/**
 * The Variant CORE API.
 * 
 * @author Igor
 * @since 0.5
 */
public class VariantCore implements Serializable {

	private static final Logger LOG = LoggerFactory.getLogger(VariantCore.class);
	
	private Schema schema = null;
	private EventWriter eventWriter = null;
	private UserHooker hooker = new UserHooker();
	private CorePropertiesImpl properties;
	private VariantRuntime runtime;
	private VariantComptime comptime;
	private CoreSessionService sessionService;
		
	//---------------------------------------------------------------------------------------------//
	//                                     PRIVARE HELPERS                                         //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Setup system properties
	 * Process command line args.
	 * 
	 * @param resourceName
	 */
	private void setupSystemProperties(String...resourceNames) {

		properties = new CorePropertiesImpl(this);

		// Override system props in left-to-right scan.
		for (int i = resourceNames.length - 1; i >= 0; i--) {
			String name = resourceNames[i];
			try {
				properties.overrideWith(VariantIoUtils.openResourceAsStream(name), name);
			}
			catch (Exception e) {
				throw new RuntimeException("Unable to read resurce [" + name + "]", e);
			}
		}
		
		// Override with /variant.props if supplied on classpath.
		try {
			properties.overrideWith(VariantIoUtils.openResourceAsStream("/variant.props"), "/variant.props");
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processed application properties resource file [/variant.props]");
			}
		}
		catch (Exception e) {} // Not an error if wasn't found.

		String runTimePropsResourceName = System.getProperty(CorePropertiesImpl.COMMANDLINE_RESOURCE_NAME);
		String runTimePropsFileName = System.getProperty(CorePropertiesImpl.COMMANDLINE_FILE_NAME);
		
		if (runTimePropsResourceName != null && runTimePropsFileName!= null) {
			throw new VariantRuntimeUserErrorException(BOOT_CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN);
		}
		
		if (runTimePropsResourceName != null) {
			try {
				properties.overrideWith(
						VariantIoUtils.openResourceAsStream(runTimePropsResourceName), 
						"-D" + CorePropertiesImpl.COMMANDLINE_RESOURCE_NAME + "=" + runTimePropsResourceName);
			}
			catch (Exception e) {
				throw new VariantRuntimeUserErrorException(BOOT_CONFIG_RESOURCE_NOT_FOUND, e, runTimePropsResourceName);
			}
		}
		else if (runTimePropsFileName != null) {
			try {
				properties.overrideWith(
						VariantIoUtils.openFileAsStream(runTimePropsFileName),
						 "-D" + CorePropertiesImpl.COMMANDLINE_FILE_NAME + "=" + runTimePropsFileName);
			}
			catch (Exception e) {
				throw new VariantRuntimeUserErrorException(BOOT_CONFIG_FILE_NOT_FOUND, e, runTimePropsFileName);
			}			
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	private void instantiate() throws Exception {
				
		//
		// Init comptime service. 
		//
		comptime = new VariantComptime();

		//
		// Instantiate event persister.
		//
		String eventPersisterClassName = properties.get(VariantCorePropertyKeys.EVENT_PERSISTER_CLASS_NAME, String.class);
		
		EventPersister eventPersister = null;
		try {
			Object eventPersisterObject = Class.forName(eventPersisterClassName).newInstance();
			if (eventPersisterObject instanceof EventPersister) {
				eventPersister = (EventPersister) eventPersisterObject;
				eventPersister.initialized(new VariantCoreInitParamsImpl(this, VariantCorePropertyKeys.EVENT_PERSISTER_CLASS_INIT));
			}
			else {
				throw new VariantRuntimeUserErrorException (BOOT_EVENT_PERSISTER_NO_INTERFACE, eventPersisterClassName, EventPersister.class.getName());
			}
		}
		catch (VariantRuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new VariantInternalException(
					"Unable to instantiate event persister class [" + eventPersisterClassName +"]", e);
		}
				
		// Instantiate event writer.
		eventWriter = new EventWriter(eventPersister, properties);
				
		// Instantiate runtime.
		runtime = new VariantRuntime(this);

		// Instantiate session service
		sessionService = new CoreSessionService(this);
	}
	
	/**
	 * Expose session service to tests.
	 * @return
	 */
	CoreSessionService getSessionService() {
		return sessionService;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	/**
	 * <p>Obtain an instance of the Core API. Takes 0 or more of String arguments. If supplied, 
	 * each argument is understood as a Java class path resource name. Each resource 
	 * is expected to contain a set of application properties, as specified by Java's 
	 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html#load-java.io.Reader-">Properties.load()</a> 
	 * method.
	 * 
	 * <p>When, at runtime, the container looks for the value of a particular property key, 
	 * these files are scanned left to right and the first value found is returned. 
	 * If a value wasn't found in any of the supplied files, or if no files were supplied, 
	 * the default value is used, as defiled in the <code>/variant/defaults.props</code> file 
	 * found inside the <code>variant-core-&lt;version&gt;.jar</code> file.
	 *
	 * @arg resourceNames 0 or more properties files as classpath resource names.
	 * @return An implementation of the {@link Variant} interface.
     *
	 * @since 0.6
	 */
	public VariantCore(String...resourceNames)  {
			
		long now = System.currentTimeMillis();
		try {
			setupSystemProperties(resourceNames);
			instantiate();
		}
		catch (final VariantRuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			String message = "Unable to instantiate Variant Core";
			LOG.error(message + ": " + e.getMessage());
			throw new VariantInternalException(message, e);
		}
		LOG.info(
				String.format("Variant Core %s bootstrapped in %s.",
						comptime.getCoreVersion(),
						DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")));
	}
		
	/**
	 * <p>This API's application properties
	 * 
	 * @return An instance of the {@link CorePropertiesImpl} type.
	 * 
	 * @since 0.6
	 */
	public CorePropertiesImpl getProperties() {
		return properties;
	}

	/**
	 * <p>Parse and, if no errors, optionally deploy a new experiment schema.
	 * 
	 * @param stream The schema to be parsed and deployed, as a java.io.InputStream.
	 * @param deploy The new test schema will be deployed if this is true and no parse errors 
	 *        were encountered.
	 *        
	 * @return An instance of the {@link com.variant.core.schema.parser.ParserResponse} type that
	 *         may be further examined about the outcome of this operation.
	 * 
	 * @since 0.5
	 */
	public ParserResponse parseSchema(InputStream stream, boolean deploy) {
		
		String input = null;
		try {
			input = IOUtils.toString(stream);
		} catch (IOException e) {
			throw new VariantInternalException("Unable to read input from stream", e);
		}
		return parseSchema(input, deploy);
	}

	/**
	 * <p>Parse and, if no errors, deploy a new experiment schema.  Same as 
     * <code>parseSchema(stream, true)</code>.
     * 
	 * @param stream The schema to be parsed and deployed, as a java.io.InputStream.
	 *         
	 * @return An instance of the {@link com.variant.core.schema.parser.ParserResponse} object, which
	 *         may be further examined about the outcome of this operation.
     *
	 * @since 0.5
	 */
	public ParserResponse parseSchema(InputStream stream) {

		return parseSchema(stream, true);
	}

	/**
	 * <p>Get currently deployed test schema, if any.
	 * 
	 * @return Current test schema as an instance of the {@link com.variant.core.schema.Schema} object.
	 * 
	 * @since 0.5
	 */
	public Schema getSchema() {
		return schema;	
	}
		
	/**
	 * Get or create user session. The contract of this method is that multiple calls with the same argument
	 * will return the same object, provided the session did not expire between calls.  It is an error to
	 * call this method on an idle instance, i.e. before a valid schema has been parsed. If the session has
	 * not expired, but the schema has changed since it was created, the VariantSchemaChanged unchecked exception
	 * will be thrown.
	 * 
	 * @param id Session ID.
	 * @since 0.6
	 * @return An instance of {@link VariantCoreSession}.
	 */
	public VariantCoreSession getSession(String id, boolean create) {
		return sessionService.getSession(id, create);
	}
	
	/**
	 * Get user session. The contract of this method is that multiple calls with the same argument
	 * will return the same object, provided the session did not expire between calls.  It is an error to
	 * call this method on an idle instance, i.e. before a valid schema has been parsed. 
	 * 
	 * @param id Session ID.
	 * @since 0.5 
	 * @return An instance of {@link VariantCoreSession}.
	 */
	public VariantCoreSession getSession(String id) {
		return sessionService.getSession(id, true);
	}

	/**
	 * Save user session in session store.
	 * @param session
	 * TODO Make this async
	 */
	public void saveSession(VariantCoreSession session) {
		sessionService.saveSession(session);
	}

	/**
	 * <p>Register a {@link com.variant.core.hook.HookListener}. The caller must provide 
	 * an implementation of the {@link com.variant.core.hook.HookListener} interface 
	 * which listens to a pre-defined {@link com.variant.core.hook.UserHook} type. Whenever 
	 * Variant reaches a hook given by the {@link com.variant.core.hook.UserHook} type
	 * parameter, the hook listener is notified. 
	 * 
	 * <p>Any number of listeners may listen for the same {@link com.variant.core.hook.UserHook} 
	 * type. If more than one listener is registered for a particular 
	 * {@link com.variant.core.hook.UserHook} type, the order of notification is undefined.
	 * 
	 * @param listener An instance of a caller-provided implementation of the 
	 *        {@link com.variant.core.hook.HookListener} interface.
	 *        
	 * @since 0.5
	 */
	public void addHookListener(HookListener<? extends UserHook> listener) {
		if (listener == null) throw new IllegalArgumentException("Argument cannot be null");
		hooker.addListener(listener);		
	}

	/**
	 * <p>Remove all previously registered (with {@link #addHookListener(HookListener)} listeners.
	 * 
	 * @since 0.5
	 */
	public void clearHookListeners() {
		hooker.clear();		
	}

	/**
	 * Expose runtime to tests via package visible getter.
	 * @return
	 */
	public VariantRuntime getRuntime() {
		return runtime;
	}

	/** 
	 * @return
	 *
	public SessionService getSessionService() {
		return sessionService;
	}
	*/
	/**
	 * 
	 */
	public ParserResponse parseSchema(String schema) {
		
		return parseSchema(schema, true);
	}

	/**
	 * 
	 * @param string
	 * @param deploy
	 * @return
	 */
	public ParserResponse parseSchema(String string, boolean deploy) {

		long now = System.currentTimeMillis();
		
		// (Re)discover and process all annotations.
		// AnnotationProcessor.process();

		ParserResponseImpl response;

		// Lose comments, i.e. from // to eol.
		StringBuilder schemaAsStringNoComments = new StringBuilder();
		String lines[] = string.split("\n");
		for (String line: lines) {
			
			int commentIndex = line.indexOf("//");
			
			if (commentIndex == 0) {
				// full line - skip.
				continue;
			}
			else if (commentIndex >= 0) {
				// partial line: remove from // to eol.
				schemaAsStringNoComments.append(line.substring(0, commentIndex));
			}
			else {
				schemaAsStringNoComments.append(line);
			}
			schemaAsStringNoComments.append("\n");
		}
		
		try {
			response = SchemaParser.parse(this, schemaAsStringNoComments.toString());
		}
		catch (Throwable t) {
			response = new ParserResponseImpl();
			ParserMessage err = response.addMessage(INTERNAL, t.getMessage());
			LOG.error(err.getText(), t);
		}

		// Only replace the schema if no ERROR or higher level errors.
		Severity highSeverity = response.highestMessageSeverity();
		if (highSeverity == null || highSeverity.lessThan(Severity.ERROR)) {
			
			if (schema != null) {
				((SchemaImpl)schema).setInternalState(SchemaImpl.InternalState.UNDEPLOYED);
			}
			schema = response.getSchema();
			((SchemaImpl)schema).setInternalState(SchemaImpl.InternalState.DEPLOYED);
			
			StringBuilder msg = new StringBuilder();
			msg.append("New schema ID [").append(schema.getId()).append("] deployed in ");
			msg.append(DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")).append(":");
			for (Test test: schema.getTests()) {
				msg.append("\n   ").append(test.getName()).append(" {");
				boolean first = true;
				for (Experience exp: test.getExperiences()) {
					if (first) first = false;
					else msg.append(", ");
					msg.append(exp.getName());
					if (exp.isControl()) msg.append(" (control)");
				}
				msg.append("}");
				if (!test.isOn()) msg.append(" OFF");
			}
			LOG.info(msg.toString());
		}
		else {
			((SchemaImpl) response.getSchema()).setInternalState(SchemaImpl.InternalState.FAILED);
			LOG.error("New schema was not deployed due to parser error(s).");
		}
		
		return response;
	}

	/**
	 * 
	 * @return
	 */
	public EventWriter getEventWriter() {
		return eventWriter;
	}
	
	/**
	 * 
	 * @return
	 */
	public UserHooker getUserHooker() {
		return hooker;
	}
	
	/**
	 * Shutdown event writer when garbage collected
	 * to prevent async thread leak.
	 */
	@Override
	public void finalize() {
		eventWriter.shutdown();
	}
	
	/**
	 * 
	 * @return
	 */
	public VariantComptime getComptime() {
		return comptime;
	}
	
	/**
	 */
	private static final long serialVersionUID = 1L;

}

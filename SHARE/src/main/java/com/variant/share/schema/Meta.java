package com.variant.share.schema;

import java.util.List;
import java.util.Optional;


/**
 * Representation of the <code>/meta</code> schema element.
 * <p>
 * 
 * @since 0.9
 */
public interface Meta {

	/**
	 * This schema's name, as provided by the <code>/meta/name</code> element.
	 * 
	 * @return Schema name. Cannot be null.
	 * @since 0.9
	 */
	String getName();

	/**
	 * This schema's comment, as provided by the <code>/meta/comment</code> element.
	 * 
	 * @return An {@link Optional}, containing the schema comment if defined, 
	 * or empty if <code>/meta/comment</code> element was omitted.
	 * @since 0.9
	 */
	Optional<String> getComment();
	
	/**
	 * List of schema-scoped lifecycle hooks, as provided by the <code>/meta/hooks</code> element. 
	 * 
	 * @return An {@link Optional}, containing immutable list of {@link MetaScopedHook} objects in the order they were defined,
	 *         or empty if the </code>/meta/hooks</code> element was omitted.
	 * @since 0.9
	 */
	Optional<List<MetaScopedHook>> getHooks();

	/**
	 * This schema's trace event flusher, as proided by the <code>/meta/flusher</code> element. If no such flusher
	 * is provided in the schema, the configurable system wide default is used at run time. If no such default is configured,
	 * the <a target="_blank" href="https://github.com/getvariant/variant-extapi-standard/blob/master/src/main/java/com/variant/extapi/std/flush/TraceEventFlusherNull.java">TraceEventFlusherAppLogger&#128279;</a> 
	 * is used.
	 * 
	 * @return An {@link Optional}, containing the {@link Flusher} object if defined, 
	 *         or empty if the </code>/meta/flusher</code> element was omitted.
	 * @since 0.9
	 */
	Optional<Flusher> getFlusher();
	
}

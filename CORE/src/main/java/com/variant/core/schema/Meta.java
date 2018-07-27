package com.variant.core.schema;

import java.util.List;


/**
 * Representation of the <code>meta</code> section of an variation schema.
 * <p>
 * 
 * @since 0.9
 */
public interface Meta {

	/**
	 * <p>This schema's name.
	 * 
	 * @return Schema name, as provided in the meta clause.
	 * @since 0.9
	 */
	String getName();

	/**
	 * <p>This schema's comment.
	 * 
	 * @return Schema comment, as provided in the meta clause.
	 * @since 0.9
	 */
	String getComment();
	
	/**
	 * <p>List of schema-scoped life-cycle hooks.
	 * 
	 * @return A list of {@link Hook} objects in the ordinal order.
	 * @since 0.9
	 */
	List<Hook> getHooks();

	/**
	 * <p>This schema's declared {@link Flusher}. If no flusher is declared by the schema, the system wide default
	 * is used.
	 * 
	 * @return An object of type {@link Flusher} if flusher was declared in this schema, or {@code null} otherwise.
	 * @since 0.9
	 */
	Flusher getFlusher();
	
}

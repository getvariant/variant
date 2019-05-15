package com.variant.core.schema;

import java.util.Optional;

/**
 * Representation of the <code>/meta/flusher</code> schema element.
 * 
 * @since 0.8
 */

public interface Flusher {
	
	/**
	 * The canonical name of the class implementing this trace event flusher, as provided by the <code>/meta/flusher/class</code> schema element.
	 * Variant instantiates an instance of this class at the schema parse time. A variation schema may specify at most one trace event flusher.
	 * 
	 * @return The string value provided by the <code>/meta/flusher/class</code> schema element. Cannot be null.
	 * @since 0.8
	 */
	public String getClassName();

	/**
	 * Arbitrary object used to initialize a newly instantiated trace event flusher, as provided by the 
	 * <code>/meta/flusher/init</code> schema element. Variant will pass this object to the constructor 
	 * of the flusher class.
	 * 
	 * @return The Optional container, with the serialized string value of the <code>/meta/flusher/init</code> element,
	 *         if defined, or empty if <code>/meta/flusher/init</code> element was omitted.
	 * @since 0.8
	 */
	public Optional<String> getInit();
		
}

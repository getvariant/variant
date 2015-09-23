package com.variant.core.impl;

import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;
import com.impetus.annovention.listener.ClassAnnotationDiscoveryListener;
import com.variant.core.Variant;
import com.variant.core.annotations.ParserEventListener;

import static com.variant.core.schema.parser.MessageTemplate.*;

import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.parser.TestParsedEventListener;
import com.variant.core.schema.parser.StateParsedEventListener;

/**
 * Package visible annotation processor.
 * 
 * @author Igor.
 *
 */
class AnnotationProcessor {
	
	static void process() {

		Discoverer discoverer = new ClasspathDiscoverer();

		
		discoverer.addAnnotationListener(new ViewParsedListener());
		discoverer.discover(true /*classes*/, false /*fields*/, true /*methods*/, false /*parameters*/, true /*visible*/, true /*invisible*/);

	}
	
	/**
	 *
	 */
	private static class ViewParsedListener implements ClassAnnotationDiscoveryListener {
		
		@Override
		public String[] supportedAnnotations() {
			return new String[] {ParserEventListener.class.getName()};
		}

		@Override
		public void discovered(String className, String annotationName) {

			try {
				Class<?> clazz = Class.forName(className);
				ParserEventListener ann = clazz.getAnnotation(ParserEventListener.class);
				switch(ann.value()) {
				case TEST:
					try {
						@SuppressWarnings("unchecked")
						TestParsedEventListener listener = ((Class<TestParsedEventListener>)clazz).newInstance();
						Variant.Factory.getInstance().addListener(listener);
					}
					catch (ClassCastException e) {
						throw new VariantRuntimeException(BOOT_PARSER_LISTENER_NO_INTERFACE, className, ParserEventListener.class.getName(), TestParsedEventListener.class.getName());
					}
					break;
					
				case VIEW:
					try {
						@SuppressWarnings("unchecked")
						StateParsedEventListener listener = ((Class<StateParsedEventListener>)clazz).newInstance();
						Variant.Factory.getInstance().addListener(listener);
					}
					catch (ClassCastException e) {
						throw new VariantRuntimeException(BOOT_PARSER_LISTENER_NO_INTERFACE, className, ParserEventListener.class.getName(), StateParsedEventListener.class.getName());
					}
					break;
				}

			}
			catch (Exception e) {
				throw new VariantInternalException("Exception while processing annotations.", e);
			}
		}
	}
}

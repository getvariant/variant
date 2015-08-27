package com.variant.web;

import com.variant.core.annotations.ParserEventListener;
import com.variant.core.schema.SchemaElement;
import com.variant.core.schema.View;
import com.variant.core.schema.ViewParsedEventListener;

/**
 * Additional parse checks not implemented by the core library and specific to the Servlet environment.
 * @author Igor
 *
 */
@ParserEventListener(SchemaElement.VIEW)
public class ViewParsedEventListenerImpl implements ViewParsedEventListener{

	@Override
	public void viewParsed(View view) {
		System.out.println("*******************************************************************");
	}
	

}

package com.variant.client.servlet.adapter.impl;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import com.variant.client.VariantClient;
import com.variant.client.servlet.adapter.VariantServletClient;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.hook.HookListener;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.util.VariantArrayUtils;

/**
 * @author Igor Urisman
 */
public class ServletClientImpl implements VariantServletClient {

	private VariantClient client;
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 */		
	public ServletClientImpl(String...resourceNames) {
		String[] newArgs = VariantArrayUtils.prepend("/com/variant/client/conf/servlet-adapter.props", resourceNames, String.class);
		this.client = VariantClient.Factory.getInstance(newArgs);
	}

	/**
	 */
	@Override
	public VariantProperties getProperties() {
		return client.getProperties();
	}

	/**
	 */
	@Override
	public void addHookListener(HookListener<?> listener) {
		client.addHookListener(listener);
	}
	
	/**
	 */
	@Override
	public void clearHookListeners() {
		client.clearHookListeners();
	}

	/**
	 */
	@Override
	public ParserResponse parseSchema(InputStream stream, boolean deploy) {
		return client.parseSchema(stream, deploy);
	}

	/**
	 */
	@Override
	public ParserResponse parseSchema(InputStream stream) {
		return client.parseSchema(stream);
	}

	/**
	 */
	@Override
	public Schema getSchema() {
		return client.getSchema();
	}

	/**
	 */
	@Override
	public VariantSession getSession(boolean create, Object... userData) {
		if (userData.length != 1)
			throw new IllegalArgumentException("Invalid user data: single element array expected");
		else if (!(userData[0] instanceof HttpServletRequest))
			throw new IllegalArgumentException("Invalid user data: HttpSerlvetRequest expected");

		return client.getSession((HttpServletRequest) userData[0]);
	}

	/**
	 */
	@Override
	public VariantSession getSession(Object...userData) {
		return getSession(true, userData);
	}

	/**
	 */
	@Override
	public VariantSession getSession(HttpServletRequest request) {
		return client.getSession(request, true);
	}

	@Override
	public VariantSession getSession(HttpServletRequest request, boolean create) {
		// TODO Auto-generated method stub
		return null;
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 */
	public VariantClient getBareClient() {
		return client;
	}

}

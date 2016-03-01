package com.variant.web.adapter;

import com.variant.web.VariantWeb;
import com.variant.webnative.VariantWebnative;

public class VariantFilter extends com.variant.webnative.adapter.VariantFilter {

	protected VariantWebnative getWebApi() { return new VariantWeb();}
	protected VariantWebnative getWebApi(String configName) { return new VariantWeb(configName);}

}

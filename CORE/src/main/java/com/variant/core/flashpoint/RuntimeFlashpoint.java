package com.variant.core.flashpoint;

import com.variant.core.VariantSession;

public interface RuntimeFlashpoint extends Flashpoint {

	/**
	 * Session being qualified.
	 * @return
	 */
	public VariantSession getSession() ;
	
}

package com.variant.core.session;

import static com.variant.core.error.ErrorTemplate.BOOT_TARGETING_PERSISTER_NO_INTERFACE;

import java.io.Serializable;

import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantInternalException;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;

/**
 * 
 * @author Igor
 *
 */
public class VariantSessionImpl implements VariantSession, Serializable {
	
	///
	private static final long serialVersionUID = 1L;

	private String id;
	private TargetingPersister targetingPersister;
	
	/**
	 * 
	 * @param id
	 */
	VariantSessionImpl(String id) {
		this.id = id;
	}

	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 */
	@Override
	public String getId() {
		return id;
	}

	@Override
	public TargetingPersister getTargetingPersister() {
		return targetingPersister;
	}

	@Override
	public boolean equals(Object o) {
		try {
			VariantSessionImpl other = (VariantSessionImpl) o;
			return id.equals(other.id);
		}
		catch(ClassCastException e) {
			return false;
		}
	}

	/**
	 * 
	 */
	@Override
	public void initTargetingPersister(Object userData) {

		
		String className = VariantProperties.getInstance().targetingPersisterClassName();
		
		try {
			Object object = Class.forName(className).newInstance();
			if (object instanceof TargetingPersister) {
				targetingPersister = (TargetingPersister) object;
			}
			else {
				throw new VariantBootstrapException(BOOT_TARGETING_PERSISTER_NO_INTERFACE, className, TargetingPersister.class.getName());
			}
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to instantiate targeting persister class [" + className +"]", e);
		}
		
		targetingPersister.initialized(this, userData);

	}


}

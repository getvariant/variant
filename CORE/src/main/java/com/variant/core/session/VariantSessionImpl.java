package com.variant.core.session;

import java.io.Serializable;

import com.variant.core.Variant;
import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.session.TargetingPersister.UserData;

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
	public void initTargetingPersister(UserData... userData) throws VariantBootstrapException {

		
		String className = VariantProperties.getInstance().targetingPersisterClassName();
		
		try {
			Object object = Class.forName(className).newInstance();
			if (object instanceof TargetingPersister) {
				targetingPersister = (TargetingPersister) object;
			}
			else {
				throw new VariantBootstrapException(
						"Targeting persister class [" + className + 
						"] must implement interface [" + TargetingPersister.class.getName() + "]");
			}
		}
		catch (Exception e) {
			throw new VariantBootstrapException(
					"Unable to instantiate targeting persister class [" + className +"]", e);
		}
		
		targetingPersister.initialized(this, userData);

	}


}

package com.variant.core.session;

import java.io.Serializable;

import com.variant.core.Variant;
import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantSession;
import com.variant.core.session.TargetingPersister.UserData;

/**
 * 
 * @author Igor
 *
 */
class VariantSessionImpl implements VariantSession, Serializable {
	
	///
	private static final long serialVersionUID = 1L;

	private String id;
	private TargetingPersister targetingPersister;
	
	/**
	 * 
	 */
	@Override
	public void initTargetingPersister(UserData... userData) throws VariantBootstrapException {

		TargetingPersister.Config config = Variant.getConfig().getTargetingPersisterConfig();
		
		if (config.getClassName() == null) {
			throw new IllegalArgumentException("Property [className] must be set");
		}
		
		try {
			Object object = Class.forName(config.getClassName()).newInstance();
			if (object instanceof TargetingPersister) {
				targetingPersister = (TargetingPersister) object;
			}
			else {
				throw new VariantBootstrapException(
						"Targeting persister class [" + config.getClassName() + 
						"] must implement interface [" + TargetingPersister.class.getName() + "]");
			}
		}
		catch (Exception e) {
			throw new VariantBootstrapException(
					"Unable to instantiate targeting persister class [" + config.getClassName() +"]",
					e
			);
		}
		
		targetingPersister.initialized(config, userData);

	}

	/**
	 * 
	 * @param id
	 */
	VariantSessionImpl(String id) {
		this.id = id;
	}
	
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


}

package com.variant.core.schema.impl;

import com.variant.core.schema.Test;

class TestOnViewVariantImpl implements Test.OnView.Variant{

	private TestExperienceImpl experience;
	private String path;
	
	/**
	 * 
	 * @param experience
	 * @param path
	 */
	TestOnViewVariantImpl(TestExperienceImpl experience, String path) {
		this.experience = experience;
		this.path = path;
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @return
	 */
	public Test.Experience getExperience() {
		return experience;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getPath() {
		return path;
	}

}

package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.List;

import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.VariantSpace;
import com.variant.core.schema.Test;
import com.variant.core.schema.View;

/**
 * An element of the onViews array of the test definition.
 * @author Igor
 */
public class TestOnViewImpl implements Test.OnView {

	private ViewImpl view;
	private TestImpl test;
	private boolean isNonvariant = false;
	private List<Variant> variants = new ArrayList<Variant>();
	private VariantSpace variantSpace;

	/**
	 * @param view
	 */
	TestOnViewImpl(ViewImpl view, TestImpl test) {
		this.view = view;
		this.test = test;
	}
	
	/**
	 * 
	 * @param isNonvariant
	 */
	void setNonvariant(boolean isNonvariant) {
		this.isNonvariant = isNonvariant;
	}
	
	/**
	 * 
	 * @param variant
	 */
	void addVariant(Variant variant) {
		variants.add(variant);
	}
		
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @return
	 */
	@Override
	public View getView() {
		return view;
	}
	
	public Test getTest() {
		return test;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public boolean isNonvariant() {
		return isNonvariant;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public List<Variant> getVariants() {
		return variants;
	}
			
	//---------------------------------------------------------------------------------------------//
	//                                    PUBLIC EXTENSION                                         //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 * Get (and, if not yet, build) his object's variant space. 
	 * Note that we have the test and its covariant set at the time of construction, 
	 * but variants are added one by one later. Caller must only call this when last 
	 * variant was added.
	 * 
	 * @return
	 * @throws VariantRuntimeException 
	 */
	public VariantSpace variantSpace() throws VariantRuntimeException {
		if (variantSpace == null) variantSpace = new VariantSpace(this);
		return variantSpace;
	}

}

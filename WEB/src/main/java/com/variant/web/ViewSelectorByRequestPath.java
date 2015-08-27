package com.variant.web;

import java.util.Collection;

import com.variant.core.schema.View;
import com.variant.core.schema.ViewSelectorByPath;

/**
 * 
 * @author Igor
 *
 *
public class ViewSelectorByRequestPath implements ViewSelectorByPath {

	private static class Candidate {
		private View view;
	}

	private Candidate match(String path, View view) {
		StringBuilder expandedPattern = new StringBuilder();
		for (int i = 0; i < view.getPath().length(); i++) {
			char c = view.getPath().charAt(i);
			if (i == 0 && c != '/') throw new 
				
		}
	}
	
    //---------------------------------------------------------------------------------------------//
	//                                    PUBLIC INTERFACE                                         //
	//---------------------------------------------------------------------------------------------//

	@Override
	public View select(String path, Collection<View> views) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
*/
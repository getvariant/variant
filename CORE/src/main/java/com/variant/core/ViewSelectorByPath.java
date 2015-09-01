package com.variant.core;

import java.util.Collection;

import com.variant.core.schema.View;

/**
 * View path matching classes must implement this.
 * 
 * @author Igor
 *
 */
public interface ViewSelectorByPath {

	public View select(String path, Collection<View> views);
	
	/**
	 * Default implementation: exact match.
	 * @author Igor
	 *
	 */
	public static class Default implements ViewSelectorByPath {

		@Override
		public View select(String path, Collection<View> views) {
			for (View v: views) {
				if (v.getPath().equals(path)) return v;
			}
			return null;

		}
		
	}
}

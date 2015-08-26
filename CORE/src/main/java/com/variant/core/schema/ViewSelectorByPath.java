package com.variant.core.schema;

import java.util.Collection;

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

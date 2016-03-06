package com.variant.webnative;

public class StateSelectorByRequestPathTestFacade {

	public static boolean match(String pattern, String string) {
		return StateSelectorByRequestPath.match(pattern, string);
	}
}

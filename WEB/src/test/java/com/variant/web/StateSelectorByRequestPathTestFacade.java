package com.variant.web;

import com.variant.web.StateSelectorByRequestPath;

public class StateSelectorByRequestPathTestFacade {

	public static boolean match(String pattern, String string) {
		return StateSelectorByRequestPath.match(pattern, string);
	}
}

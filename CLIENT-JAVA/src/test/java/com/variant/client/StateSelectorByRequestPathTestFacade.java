package com.variant.client;

import com.variant.client.StateSelectorByRequestPath;

public class StateSelectorByRequestPathTestFacade {

	public static boolean match(String pattern, String string) {
		return StateSelectorByRequestPath.match(pattern, string);
	}
}

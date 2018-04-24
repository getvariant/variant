package com.variant.client.test.util;

import java.io.IOException;
import java.util.List;

import com.variant.core.util.LogTailer;
import com.variant.core.util.LogTailer.Entry;

public class ClientLogTailer {

	public static List<Entry> last(int lines) throws IOException {
		return LogTailer.last(lines, "logs/application.log");
	}
}

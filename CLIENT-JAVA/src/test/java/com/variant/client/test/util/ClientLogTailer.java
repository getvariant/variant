package com.variant.client.test.util;

import java.io.IOException;
import java.util.List;

import com.variant.share.util.LogTailer;
import com.variant.share.util.LogTailer.Entry;

public class ClientLogTailer {

	public static List<Entry> last(int lines) throws IOException {
		return LogTailer.last(lines, "logs/application.log");
	}
	
	public static List<Entry> lastAndEcho(int lines) throws IOException {
		List<Entry> result = last(lines);
		System.out.println("**************** ECHO **************");
		for (Entry e: result) System.out.println(e.toString());
		System.out.println("************************************");
		
		return result;
	}

}

package com.variant.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.variant.core.util.Tuples.Pair;

public class VariantIoUtils {

	/**
	 * Open one at a time, until found.
	 * @param resourceName always treated as absolute path, even if does not start with a /. 
	 * @return A pair of input stream and the underlying file's fully qualified OS name.
	 */
	public static Pair<InputStream, String> openResourceAsStream(String resName) throws IOException {

		// We're using a TCCL, i.e. expect res name not to start with a / — strip it if given.
		String realName = resName.substring(resName.startsWith("/") ? 1 : 0);
		URL res = Thread.currentThread().getContextClassLoader().getResource(realName);
		return res == null ? null : new Pair<InputStream, String>(res.openStream(), res.getFile());
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static InputStream openFileAsStream(String fileName) {
		try {
			return new FileInputStream(new File(fileName));
		}
		catch (FileNotFoundException e) {
			throw new RuntimeException("File [" + fileName + "] does not exist.", e);
		}
	}

}

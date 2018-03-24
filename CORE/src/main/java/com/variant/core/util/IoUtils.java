package com.variant.core.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.variant.core.util.Tuples.Pair;

public class IoUtils {

	/**
	 * Open a resource as an InputStream.
	 * @param resourceName is always treated as absolute path, even if does not start with a /. 
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

	/**
	 * Clone of apache commons io toString(InputStream)
	 * @param is
	 * @return
	 * @throws IOException 
	 */
	public static String toString(InputStream is) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		int length;
		while ((length = is.read(buffer)) != -1) {
		    result.write(buffer, 0, length);
		}
		return result.toString("UTF-8");
	}
	
	/**
	 * Copy file.
	 * @param from
	 * @param to
	 * @throws IOException 
	 */
	public static void fileCopy(String from, String to) throws IOException {
		Path in = new File(from).toPath();
		Path out = new File(to).toPath();
		Files.createDirectories(out.getParent());
		Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * Create an empty directory.  Drop if first if exists.
	 * @param name
	 * @throws IOException 
	 */
	public static void emptyDir(String name) throws IOException {
		delete(name);
		Files.createDirectories(new File(name).toPath());
	}
	
	/**
	 * Delete a file. If file is a directory, recursively deletes the contents.
	 * No-op if doesn't exist.
	 * @param name
	 * @return
	 */
	public static void delete(String name) throws IOException {
		File file = new File(name);
		if (file.exists()) {
			if (file.isDirectory()) for (File f: file.listFiles()) Files.delete(f.toPath());
			Files.delete(file.toPath());
		}
	}
	
}

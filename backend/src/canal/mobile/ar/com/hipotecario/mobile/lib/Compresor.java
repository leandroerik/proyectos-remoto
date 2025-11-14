package ar.com.hipotecario.mobile.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compresor {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	public Compresor() {

	}

	public static byte[] gzip(String str) throws IOException {
		return new Compresor().compress(str);
	}

	public static String fromGzip(byte[] data) throws IOException {
		return new String(new Compresor().uncompress(new ByteArrayInputStream(data)));
	}

	public byte[] compress(String str) throws IOException {

		InputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"));
		GZIPOutputStream gzos = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			gzos = new GZIPOutputStream(baos);
			copy(is, gzos);
			gzos.finish(); // Important!
			return baos.toByteArray();
		} finally {
			closeQuietly(gzos);
		}
	}

	private long copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE]; // 4K buffer
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/*
	 * GZIP uncompress an InputStream and return the result as a new byte[] array:
	 */

	public byte[] uncompress(InputStream is) throws IOException {
		// InputStream is = new ByteArrayInputStream(bc);
		GZIPInputStream gzis = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			gzis = new GZIPInputStream(is);
			copy(gzis, baos);
			return baos.toByteArray();
		} finally {
			closeQuietly(gzis);
		}
	}

	private void closeQuietly(OutputStream output) {
		try {
			if (output != null) {
				output.close();
			}
		} catch (IOException i) {
			// ignore any IOException's, this is closeQuietly
		}
	}

	private void closeQuietly(InputStream input) {
		try {
			if (input != null) {
				input.close();
			}
		} catch (IOException i) {
			// ignore any IOException's, this is closeQuietly
		}
	}
}

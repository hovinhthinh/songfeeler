package util;

import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by thinhhv on 11/12/2014.
 */
public class Compressor {
	private static final Logger LOG = Logger.getLogger(Compressor.class);

	public static byte[] compressByteArray(byte[] arr) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out);
			gzipOutputStream.write(arr);
			gzipOutputStream.close();
		} catch (IOException e) {
			LOG.info("error when compressing byte array");
			return null;
		}
		byte[] result = out.toByteArray();
		LOG.info("compression ratio: " + (arr.length == 0 ? "empty array" : String.format("%.1f%%", (100.0f * result.length) / arr.length)));
		return result;
	}

	public static byte[] decompressByteArray(byte[] arr) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			InputStream in = new GZIPInputStream(new ByteArrayInputStream(arr));
			byte[] buffer = new byte[8192];
			int c;
			while ((c = in.read(buffer)) > 0) {
				out.write(buffer, 0, c);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			LOG.info("error when decompressing byte array");
			return null;
		}
		return out.toByteArray();
	}
}

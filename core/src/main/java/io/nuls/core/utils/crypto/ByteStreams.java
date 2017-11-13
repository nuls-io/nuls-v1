package io.nuls.core.utils.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ByteStreams {
	
	private static final int BUF_SIZE = 0x1000; // 4K

	public static byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    copy(in, out);
	    return out.toByteArray();
	}
	
	public static long copy(InputStream from, OutputStream to)
		      throws IOException {
	    Utils.checkNotNull(from);
	    Utils.checkNotNull(to);
	    byte[] buf = new byte[BUF_SIZE];
	    long total = 0;
	    while (true) {
	      int r = from.read(buf);
	      if (r == -1) {
	        break;
	      }
	      to.write(buf, 0, r);
	      total += r;
	    }
	    return total;
	 }
}

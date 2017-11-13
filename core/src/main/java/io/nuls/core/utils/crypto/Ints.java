package io.nuls.core.utils.crypto;

public class Ints {

	public static int fromBytes(byte b1, byte b2, byte b3, byte b4) {
		return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
	}
}

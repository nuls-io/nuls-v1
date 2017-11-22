package io.nuls.core.utils.crypto;

/**
 * 16进制编码解码类
 */
public class Hex {
	/**
	 * 对字节数据进行16进制编码。
	 * 
	 * @param src 源字节数组
	 * @return String 编码后的字符串
	 */
	public static String encode(byte[] src) {
		StringBuffer strbuf = new StringBuffer(src.length * 2);
		int i;

		for (i = 0; i < src.length; i++) {
			if (((int) src[i] & 0xff) < 0x10) {
                strbuf.append("0");
            }

			strbuf.append(Long.toString((int) src[i] & 0xff, 16));
		}

		return strbuf.toString();
	}

	/**
	 * 对16进制编码的字符串进行解码。
	 * 
	 * @param hexString 源字串
	 * @return byte[] 解码后的字节数组
	 */
	public static byte[] decode(String hexString) {
		byte[] bts = new byte[hexString.length() / 2];
		for (int i = 0; i < bts.length; i++) {
			bts[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
		}
		return bts;
	}

}
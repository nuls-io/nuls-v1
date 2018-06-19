/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.sdk.crypto;

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

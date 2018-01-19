/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.core.utils.crypto;

import io.nuls.core.utils.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 字节流工具
 * @author ln
 *
 */
public class ByteArrayTool {

	private ByteArrayOutputStream array;
	
	public ByteArrayTool() {
		array = new ByteArrayOutputStream();
	}
	
	public ByteArrayTool(int size) {
		array = new ByteArrayOutputStream(size);
	}
	
	public ByteArrayTool(byte[] bytes) {
		array = new ByteArrayOutputStream(bytes.length);
		try {
			array.write(bytes);
		} catch (IOException e) {
			Log.error(e);
		}
	}
	
	public void append(byte b) {
		try {
			array.write(new byte[]{b});
		} catch (IOException e) {
			Log.error(e);
		}
	}
	
	public void append(int i) {
		append((byte)i);
	}
	
	public void append(byte[] bytes) {
		try {
			array.write(bytes);
		} catch (IOException e) {
			Log.error(e);
		}
	}
	
	public void append(long val) {
		try {
			Utils.uint32ToByteStreamLE(val, array);
		} catch (IOException e) {
			Log.error(e);
		}
	}
	
	public void append64(long val) {
		try {
			Utils.int64ToByteStreamLE(val, array);
		} catch (IOException e) {
			Log.error(e);
		}
	}
	
	public byte[] toArray() {
		try {
			return array.toByteArray();
		} finally {
			try {
				array.close();
			} catch (IOException e) {
				Log.error(e);
			}
		}
	}
	
	public void close() {
		try {
			array.close();
		} catch (IOException e) {
			Log.error(e);
		}
	}
}

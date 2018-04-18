/**
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
 */
package io.nuls.protocol.model;

import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.crypto.ByteArrayTool;
import io.nuls.core.utils.log.Log;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * 类型
 * @author ln
 *
 */
public class KeyValue {
	
	public static final String CHARSET = NulsConfig.DEFAULT_ENCODING;
	
	protected String code;
	protected String name;
	protected byte[] value;
	
	public KeyValue() {
	}
	
	public KeyValue(String code, String name, byte[] value) {
		this.code = code;
		this.name = name;
		this.value = value;
	}

	public KeyValue(byte[] content) {
		try {
			int cursor = 0;
			int length = content[cursor] & 0xFF;
			cursor++;
		
			code = new String(Arrays.copyOfRange(content, cursor, cursor + length), CHARSET);
			cursor += length;
			
			length = content[cursor] & 0xFF;
			cursor++;
			
			name = new String(Arrays.copyOfRange(content, cursor, cursor + length), CHARSET);
			cursor += length;
			
			VarInt varInt = new VarInt(content, cursor);
			cursor += varInt.getOriginalSizeInBytes();
			
			value = Arrays.copyOfRange(content, cursor, (int)(cursor + varInt.value));
			
		} catch (UnsupportedEncodingException e) {
			Log.error(e);
		}
	}
	
	public byte[] toByte() {
		
		ByteArrayTool byteArray = new ByteArrayTool();
		try {
			byte[] codeBytes = code.getBytes(CHARSET);
			byteArray.append(codeBytes.length);
			byteArray.append(codeBytes);
		
			byte[] nameBytes = name.getBytes(CHARSET);
			byteArray.append(nameBytes.length);
			byteArray.append(nameBytes);
			
			byteArray.append(new VarInt(value.length).encode());
			byteArray.append(value);
		} catch (UnsupportedEncodingException e) {
			Log.error(e);
		}
		
		return byteArray.toArray();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}
	
	public String getValueToString() {
		try {
			return new String(value, NulsConfig.DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			Log.error(e);
		}
		return "";
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("KeyValue [code=");
		builder.append(code);
		builder.append(", name=");
		builder.append(name);
		builder.append(", value=");
		try {
			builder.append(new String(value, NulsConfig.DEFAULT_ENCODING));
		} catch (UnsupportedEncodingException e) {
			Log.error(e);
		}
		builder.append("]");
		return builder.toString();
	}
}

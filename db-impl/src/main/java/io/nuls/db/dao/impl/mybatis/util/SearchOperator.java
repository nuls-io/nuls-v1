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
package io.nuls.db.dao.impl.mybatis.util;

/**
 * 查询操作符
 * 
 * @author zhouwei
 * 
 */
public enum SearchOperator {
	eq("等于", "="),
	ne("不等于", "!="),
	gt("大于", ">"),
	gte("大于等于", ">="),
    lt("小于","<"),
	lte("小于等于", "<="),
    like("模糊匹配", "like"),
	notLike("不匹配","not like"),
	prefixLike("前缀模糊匹配", "like"),
	prefixNotLike("前缀模糊不匹配", "not like"),
	suffixLike("后缀模糊匹配", "like"),
	suffixNotLike("后缀模糊不匹配", "not like"),
	isNull("空", "is null"),
	isNotNull("非空", "is not null"),
	in("包含", "in"),
	notIn("不包含", "not in"),
	custom("自定义默认的", null);

	private final String info;
	private final String symbol;

	SearchOperator(final String info, String symbol) {
		this.info = info;
		this.symbol = symbol;
	}

	public String getInfo() {
		return info;
	}

	public String getSymbol() {
		return symbol;
	}
}

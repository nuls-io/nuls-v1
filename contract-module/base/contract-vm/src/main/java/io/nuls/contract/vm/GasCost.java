/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.contract.vm;

public class GasCost {

    public static final int COMPARISON = 1;//比较字节码
    public static final int CONSTANT = 1;//简单数值类型字节码
    public static final int LDC = 1;//数值常量，字符串常量（长度 * LDC）
    public static final int CONTROL = 5;//控制字节码
    public static final int TABLESWITCH = 2;//switch字节码（大小 * TABLESWITCH）
    public static final int LOOKUPSWITCH = 2;//switch字节码（大小 * LOOKUPSWITCH）
    public static final int CONVERSION = 1;//数值转换
    public static final int EXTENDED = 1;//null判断
    public static final int MULTIANEWARRAY = 1;//多维数组（大小 * MULTIANEWARRAY）
    public static final int LOAD = 1;//把本地变量送到栈顶
    public static final int ARRAYLOAD = 5;//把数组的某项送到栈顶
    public static final int MATH = 1;//数学操作及移位操作
    public static final int REFERENCE = 10;//对象相关操作
    public static final int NEWARRAY = 1;//一维数组（大小 * NEWARRAY）
    public static final int STACK = 2;//栈操作
    public static final int STORE = 1;//把栈顶的值存入本地变量
    public static final int ARRAYSTORE = 5;//把栈项的值存到数组里
    public static final int TRANSFER = 1000;//转账交易
    public static final int SHA3 = 500;//SHA3调用
    public static final int VERIFY_SIGNATURE = 500;//验证签名

}

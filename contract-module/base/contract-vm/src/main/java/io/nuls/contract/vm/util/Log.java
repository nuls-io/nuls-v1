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
package io.nuls.contract.vm.util;

import io.nuls.contract.vm.OpCode;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;

import java.util.Arrays;

public class Log {

    // TODO: 2018/4/27

    public static void loadClass(String className) {
        String log = "load class: " + className;
        log(log);
    }

    public static void runMethod(MethodCode methodCode) {
        String log = "run method: " + methodCode.className + "." + methodCode.name + " " + methodCode.desc;
        log(log);
    }

    public static void continueMethod(MethodCode methodCode) {
        String log = "continue method: " + methodCode.className + "." + methodCode.name + " " + methodCode.desc;
        log(log);
    }

    public static void endMethod(MethodCode methodCode) {
        String log = "end method: " + methodCode.className + "." + methodCode.name + " " + methodCode.desc;
        log(log);
    }

    public static void nativeMethod(MethodCode methodCode) {
        String log = "native method: " + methodCode.className + "." + methodCode.name + " " + methodCode.desc;
        log(log);
    }

    public static void nativeMethodResult(Result result) {
        String log = "native method result: " + result;
        log(log);
    }

    public static void opcode(OpCode opCode, Object... args) {
        String log = opCode.name();
        if (args != null && args.length > 0) {
            log += " " + Arrays.toString(args);
        }
        log(log);
    }

    public static void result(OpCode opCode, Object result, Object... args) {
        String log = opCode + " " + Arrays.toString(args) + " " + result;
        log(log);
    }

    public static void log(String log) {
        //System.out.println(log);
    }

}

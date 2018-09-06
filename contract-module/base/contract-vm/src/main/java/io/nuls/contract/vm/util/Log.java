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
        String log = "run method: " + methodCode.getClassCode().getName() + "." + methodCode.getName() + " " + methodCode.getDesc();
        log(log);
    }

    public static void continueMethod(MethodCode methodCode) {
        String log = "continue method: " + methodCode.getClassCode().getName() + "." + methodCode.getName() + " " + methodCode.getDesc();
        log(log);
    }

    public static void endMethod(MethodCode methodCode) {
        String log = "end method: " + methodCode.getClassCode().getName() + "." + methodCode.getName() + " " + methodCode.getDesc();
        log(log);
    }

    public static void nativeMethod(MethodCode methodCode) {
        String log = "native method: " + methodCode.getClassCode().getName() + "." + methodCode.getName() + " " + methodCode.getDesc();
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

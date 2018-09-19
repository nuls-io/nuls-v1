package io.nuls.contract.vm.natives.java.util;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

import java.util.Locale;

public class NativeLocale {

    public static final String TYPE = "java/util/Locale";

    public static boolean isSupport(MethodCode methodCode) {
        if (methodCode.isClass(TYPE) && "initDefault".equals(methodCode.name)) {
            return true;
        } else {
            return false;
        }
    }

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.name) {
            case "initDefault":
                result = initDefault(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static Result initDefault(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Locale locale = Locale.US;
        String language = locale.getLanguage();
        String script = locale.getScript();
        String country = locale.getCountry();
        String variant = locale.getVariant();

        Object[] args = new Object[]{
                frame.heap.newString(language),
                frame.heap.newString(script),
                frame.heap.newString(country),
                frame.heap.newString(variant),
                null
        };

        MethodCode methodCode1 = frame.methodArea.loadMethod(TYPE, "getInstance",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lsun/util/locale/LocaleExtensions;)Ljava/util/Locale;");

        frame.vm.run(methodCode1, args, true);

        ObjectRef objectRef = (ObjectRef) frame.vm.getResult().getValue();

        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

}

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
        if (methodCode.isClass(TYPE) && "initDefault".equals(methodCode.getName())) {
            return true;
        } else {
            return false;
        }
    }

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.getName()) {
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
                frame.getHeap().newString(language),
                frame.getHeap().newString(script),
                frame.getHeap().newString(country),
                frame.getHeap().newString(variant),
                null
        };

        MethodCode methodCode1 = frame.getMethodArea().loadMethod(TYPE, "getInstance",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lsun/util/locale/LocaleExtensions;)Ljava/util/Locale;");

        frame.getVm().run(methodCode1, args, true);

        ObjectRef objectRef = (ObjectRef) frame.getVm().getResult().getValue();

        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

}

package io.nuls.contract.vm;

import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.ClassCodeLoader;
import io.nuls.contract.vm.program.impl.ProgramConstants;
import org.apache.commons.lang3.ArrayUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class VMFactory {

    public static final Map<String, ClassCode> VM_INIT_CLASS_CODES = new LinkedHashMap(1024);

    public static VM VM;

    private static final String[] IGNORE_CLINIT = new String[]{
            "java/io/File",
            "java/io/FileDescriptor",
            "java/io/ObjectInputStream",
            "java/io/ObjectOutputStream",
            "java/io/ObjectStreamClass",
            "java/io/ObjectStreamClass$FieldReflector",
            "java/lang/SecurityManager",
            "java/lang/invoke/BoundMethodHandle",
            "java/lang/invoke/BoundMethodHandle$SpeciesData",
            "java/lang/invoke/DirectMethodHandle",
            "java/lang/invoke/Invokers",
            "java/lang/invoke/LambdaForm",
            "java/lang/invoke/LambdaForm$BasicType",
            "java/lang/invoke/LambdaForm$NamedFunction",
            "java/lang/invoke/MethodHandle",
            "java/lang/invoke/MethodHandles$Lookup",
            "java/lang/invoke/MethodType",
            "java/lang/ref/Reference",
            "java/lang/reflect/AccessibleObject",
            "java/math/BigDecimal",
            "java/net/InetAddress",
            "java/net/NetworkInterface",
            "java/nio/charset/Charset",
            "java/security/ProtectionDomain",
            "java/security/Provider",
            "java/time/OffsetTime",
            "java/time/ZoneOffset",
            "java/util/Locale",
            "java/util/Locale$1",
            "java/util/Random",
            "sun/misc/URLClassPath",
            "sun/security/util/Debug",
            "sun/util/locale/BaseLocale",
    };

    public static final String[] INIT_CLASS_CACHE = new String[]{
            "java/lang/Byte$ByteCache",
            "java/lang/Short$ShortCache",
            "java/lang/Character$CharacterCache",
            //"java/lang/Integer$IntegerCache",
            "java/lang/Long$LongCache",
    };

    public static void init() {
        String[] classes = ArrayUtils.addAll(ProgramConstants.VM_INIT_CLASS_NAMES, ProgramConstants.CONTRACT_USED_CLASS_NAMES);
        classes = ArrayUtils.addAll(classes, ProgramConstants.CONTRACT_LAZY_USED_CLASS_NAMES);
        classes = ArrayUtils.addAll(classes, ProgramConstants.SDK_CLASS_NAMES);
        classes = ArrayUtils.addAll(classes, INIT_CLASS_CACHE);
        for (String className : classes) {
            VM_INIT_CLASS_CODES.put(className, ClassCodeLoader.loadFromResource(className));
        }
        for (int i = 0; i < classes.length; i++) {
            VM_INIT_CLASS_CODES.putAll(ClassCodeLoader.loadAll(classes[i], ClassCodeLoader::loadFromResource));
        }
        VM = newVM();
        MethodArea.INIT_CLASS_CODES.putAll(VM.getMethodArea().getClassCodes());
        MethodArea.INIT_METHOD_CODES.putAll(VM.getMethodArea().getMethodCodes());
        VM.getHeap().objects.commit();
        Heap.INIT_OBJECTS.putAll(VM.getHeap().objects);
        VM.getHeap().arrays.commit();
        Heap.INIT_ARRAYS.putAll(VM.getHeap().arrays);
    }

    public static VM createVM() {
        return new VM(VM);
    }

    public static VM newVM() {
        VM vm = new VM();
        for (String key : VM_INIT_CLASS_CODES.keySet()) {
            ClassCode classCode = VM_INIT_CLASS_CODES.get(key);
            if (ArrayUtils.contains(IGNORE_CLINIT, key)) {
                continue;
            }
            //System.out.println(key);
            vm.getMethodArea().loadClassCode(classCode);
        }
        return vm;
    }

}

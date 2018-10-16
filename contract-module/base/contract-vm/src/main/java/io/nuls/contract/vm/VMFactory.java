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
package io.nuls.contract.vm;

import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.ClassCodeLoader;
import io.nuls.contract.vm.program.impl.ProgramConstants;
import org.apache.commons.lang3.ArrayUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class VMFactory {

    public static final Map<String, ClassCode> VM_INIT_CLASS_CODES = new LinkedHashMap(1024);

    public static final VM VM;

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
            "java/lang/Integer$IntegerCache",
            "java/lang/Long$LongCache",
    };

    static {
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
        MethodArea.INIT_CLASS_CODES.putAll(VM.methodArea.getClassCodes());
        MethodArea.INIT_METHOD_CODES.putAll(VM.methodArea.getMethodCodes());
        Heap.INIT_OBJECTS.putAll(VM.heap.objects);
        Heap.INIT_ARRAYS.putAll(VM.heap.arrays);
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
            vm.methodArea.loadClassCode(classCode);
        }
        return vm;
    }

}

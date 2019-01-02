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

import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.ClassCodeLoader;

import java.util.LinkedHashMap;
import java.util.Map;

public class VMFactory {

    public static final VM VM;

    private static final String[] CLINIT_CLASSES = new String[]{
            "io/nuls/contract/sdk/Address",
            "io/nuls/contract/sdk/Block",
            "io/nuls/contract/sdk/BlockHeader",
            "io/nuls/contract/sdk/Contract",
            "io/nuls/contract/sdk/Event",
            "io/nuls/contract/sdk/Msg",
            "io/nuls/contract/sdk/Utils",
            "io/nuls/contract/sdk/annotation/View",
            "io/nuls/contract/sdk/annotation/Required",
            "io/nuls/contract/sdk/annotation/Payable",
            "java/lang/Boolean",
            "java/lang/Byte",
            "java/lang/Short",
            "java/lang/Character",
            "java/lang/Integer",
            "java/lang/Long",
            "java/lang/Float",
            "java/lang/Double",
            "java/lang/String",
            "java/lang/StringBuilder",
            "java/math/BigInteger",
            "java/math/BigDecimal",
            "java/util/List",
            "java/util/ArrayList",
            "java/util/LinkedList",
            "java/util/Map",
            "java/util/Map$Entry",
            "java/util/HashMap",
            "java/util/LinkedHashMap",
            "java/util/Set",
            "java/util/HashSet",
            "java/lang/Exception",
            "java/lang/RuntimeException",
            "java/lang/Object",
            "java/io/ObjectStreamField",
            "java/lang/String$CaseInsensitiveComparator",
            "java/lang/String$1",
            "java/lang/Class",
            "java/lang/AbstractStringBuilder",
            "java/lang/Throwable",
            "java/lang/StackTraceElement",
            "java/util/AbstractList",
            "java/util/AbstractCollection",
            "java/lang/IllegalArgumentException",
            "java/lang/StringIndexOutOfBoundsException",
            "java/lang/IndexOutOfBoundsException",
            "java/util/Arrays",
            "java/lang/Math",
            "java/lang/AssertionError",
            "java/lang/Error",
            "java/lang/System",
            "java/util/Collections$EmptySet",
            "java/util/Collections$1",
            "java/util/AbstractSet",
            "java/util/Collections",
            "java/util/Collections$EmptyList",
            "java/util/Collections$EmptyMap",
            "java/util/AbstractMap",
            "java/util/RandomAccess",
            "java/util/Collections$UnmodifiableRandomAccessList",
            "java/util/Collections$UnmodifiableList",
            "java/util/Collections$UnmodifiableCollection",
            "java/util/Collection",
            "java/lang/NullPointerException",
            "java/lang/OutOfMemoryError",
            "java/lang/VirtualMachineError",
            "java/lang/Integer$IntegerCache",
            "sun/misc/VM",
            "java/util/Properties",
            "java/util/Hashtable",
            "java/util/Dictionary",
            "java/util/Hashtable$Entry",
            "java/lang/IllegalStateException",
            "java/lang/NumberFormatException",
            "java/lang/Number",
            "java/lang/CharacterData",
            "java/lang/CharacterDataLatin1",
            "java/lang/CharacterData00",
            "java/lang/CharacterData01",
            "java/lang/CharacterData02",
            "java/lang/CharacterData0E",
            "java/lang/CharacterDataPrivateUse",
            "java/lang/CharacterDataUndefined",
            "java/lang/SecurityManager",
            "java/lang/ThreadGroup",
            "java/lang/Byte$ByteCache",
            "java/lang/Short$ShortCache",
            "java/lang/Character$CharacterCache",
            "java/lang/CharSequence",
            "java/lang/CharacterName",
            "java/lang/ref/Reference$Lock",
            "java/lang/ref/Reference$1",
            "java/lang/Thread",
            "java/lang/RuntimePermission",
            "java/security/BasicPermission",
            "java/security/Permission",
            "java/lang/StrictMath",
            "java/lang/ArithmeticException",
            "java/lang/Void",
            "java/lang/InternalError",
            "java/math/MutableBigInteger",
            "java/lang/ArrayIndexOutOfBoundsException",
            "java/lang/reflect/Array",
            "java/util/regex/Pattern",
            "java/util/regex/Pattern$4",
            "java/util/regex/Pattern$Node",
            "java/util/regex/Pattern$LastNode",
            "java/text/Normalizer$Form",
            "java/lang/Enum",
            "java/text/Normalizer",
            "sun/text/normalizer/NormalizerBase$Mode",
            "sun/text/normalizer/NormalizerBase$1",
            "sun/text/normalizer/NormalizerBase",
            "sun/text/normalizer/NormalizerBase$NFDMode",
            "sun/text/normalizer/NormalizerBase$NFKDMode",
            "sun/text/normalizer/NormalizerBase$NFCMode",
            "sun/text/normalizer/NormalizerBase$NFKCMode",
            "sun/text/normalizer/NormalizerBase$QuickCheckResult",
            "sun/text/Normalizer",
            "sun/text/normalizer/ICUData",
            "java/io/InputStream",
            "sun/text/normalizer/ICUData$1",
            "java/security/AccessController",
            "java/security/PrivilegedAction",
            "java/lang/Long$LongCache",
            "sun/reflect/Reflection",
            "java/util/HashMap$Node",
            "java/util/HashMap$TreeNode",
            "java/lang/Comparable",
            "java/lang/reflect/Type",
            "sun/reflect/generics/repository/ClassRepository",
            "sun/reflect/generics/factory/GenericsFactory",
            "sun/reflect/generics/repository/GenericDeclRepository",
            "sun/reflect/generics/repository/AbstractRepository",
            "sun/reflect/generics/tree/Tree",
            "sun/reflect/generics/scope/ClassScope",
            "sun/reflect/generics/scope/AbstractScope",
            "java/lang/reflect/GenericDeclaration",
            "sun/reflect/generics/factory/CoreReflectionFactory",
            "sun/reflect/generics/scope/Scope",
            "sun/reflect/generics/tree/ClassSignature",
            "sun/reflect/generics/tree/ClassTypeSignature",
            "sun/reflect/generics/visitor/Reifier",
            "sun/reflect/generics/tree/TypeTree",
            "sun/reflect/generics/visitor/TypeTreeVisitor",
            "java/lang/reflect/ParameterizedType",
            "java/util/Iterator",
            "java/util/LinkedHashMap$Entry",
            "sun/nio/cs/StandardCharsets",
            "sun/nio/cs/StandardCharsets$Aliases",
            "sun/nio/cs/StandardCharsets$1",
            "sun/util/PreHashedMap",
            "sun/nio/cs/StandardCharsets$Classes",
            "sun/nio/cs/StandardCharsets$Cache",
            "sun/nio/cs/FastCharsetProvider",
            "java/nio/charset/spi/CharsetProvider",
            "java/lang/StringBuffer",
            "java/util/Objects",
            "java/util/Comparator",
            "java/util/ArrayList$SubList",
            "java/util/StringJoiner",
            "java/lang/Iterable",
            "java/util/Locale$Cache",
            "java/util/Locale$Category",
            "java/util/Locale$1",
            "sun/util/locale/LocaleObjectCache",
            "java/lang/ref/ReferenceQueue",
            "java/lang/ref/ReferenceQueue$Null",
            "java/lang/ref/ReferenceQueue$1",
            "java/lang/ref/ReferenceQueue$Lock",
            "java/lang/Runtime",
            "java/util/concurrent/ConcurrentHashMap$Segment",
            "java/math/SignedMutableBigInteger",
            "java/math/RoundingMode",
            "java/math/MathContext",
            "java/math/BigDecimal$StringBuilderHelper",
            "java/math/BigDecimal$LongOverflow",
            "java/util/ListIterator",
            "java/util/function/UnaryOperator",
            "sun/security/action/GetBooleanAction",
            "java/util/Spliterators$EmptySpliterator$OfRef",
            "java/util/Spliterators$EmptySpliterator",
            "java/util/Spliterators",
            "java/util/Spliterators$EmptySpliterator$OfInt",
            "java/util/Spliterators$EmptySpliterator$OfLong",
            "java/util/Spliterators$EmptySpliterator$OfDouble",
            "java/util/Spliterator",
            "java/util/Spliterators$IteratorSpliterator",
            "java/util/function/Predicate",
            "java/lang/UnsupportedOperationException",
            "java/util/stream/StreamSupport",
            "java/util/stream/Stream",
            "java/util/stream/AbstractPipeline",
            "java/util/stream/ReferencePipeline$Head",
            "java/util/stream/StreamOpFlag$Type",
            "java/util/stream/StreamOpFlag$MaskBuilder",
            "java/util/EnumMap$1",
            "java/util/EnumMap",
            "java/util/function/Consumer",
            "java/util/ArrayList$ListItr",
            "java/util/ArrayList$Itr",
            "java/util/ConcurrentModificationException",
            "java/util/ArrayList$ArrayListSpliterator",
            "java/util/BitSet",
            "java/lang/NegativeArraySizeException",
            "java/util/AbstractList$ListItr",
            "java/util/AbstractList$Itr",
            "java/util/AbstractList$1",
            "java/util/RandomAccessSubList",
            "java/util/SubList",
            "java/util/AbstractSequentialList",
            "java/util/LinkedList$Node",
            "java/util/NoSuchElementException",
            "java/util/LinkedList$ListItr",
            "java/util/LinkedList$DescendingIterator",
            "java/util/LinkedList$1",
            "java/util/LinkedList$LLSpliterator",
            "java/util/function/BiConsumer",
            "java/util/function/BiFunction",
            "java/util/function/Function",
            "java/util/HashMap$KeySet",
            "java/util/HashMap$Values",
            "java/util/HashMap$EntrySet",
            "java/util/AbstractMap$1",
            "java/util/AbstractMap$2",
            "java/util/LinkedHashMap$LinkedKeySet",
            "java/util/LinkedHashMap$LinkedValues",
            "java/util/LinkedHashMap$LinkedEntrySet",
            "java/util/HashMap$KeySpliterator",
            "java/util/HashMap$HashMapSpliterator",
            "java/io/PrintStream",
            "java/lang/Throwable$WrappedPrintStream",
            "java/lang/Throwable$PrintStreamOrWriter",
            "java/lang/Throwable$1",
            "java/util/IdentityHashMap",
            "java/util/Collections$SetFromMap",
            "java/lang/Throwable$WrappedPrintWriter",
            "java/io/PrintWriter",
            "java/lang/ClassCastException",
            "java/lang/StackOverflowError",
    };

    static {
        VM = initVM();
        MethodArea.INIT_CLASS_CODES.putAll(VM.methodArea.getClassCodes());
        MethodArea.INIT_METHOD_CODES.putAll(VM.methodArea.getMethodCodes());
        Heap.INIT_OBJECTS.putAll(VM.heap.objects);
        Heap.INIT_ARRAYS.putAll(VM.heap.arrays);
    }

    public static VM createVM() {
        return new VM(VM);
    }

    private static VM initVM() {
        VM vm = new VM();
        Map<String, ClassCode> classCodes = new LinkedHashMap<>(1024);
        for (String className : CLINIT_CLASSES) {
            ClassCode classCode = ClassCodeLoader.loadFromResource(className);
            classCodes.put(classCode.name, classCode);
        }
        vm.methodArea.loadClassCodes(classCodes);
        return vm;
    }

}

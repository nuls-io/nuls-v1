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
package io.nuls.contract.vm.program.impl;

import io.nuls.contract.sdk.*;
import io.nuls.contract.sdk.annotation.Payable;
import io.nuls.contract.sdk.annotation.Required;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class ProgramConstants {

    public static final String CONTRACT_INTERFACE_NAME = classNameReplace(Contract.class.getName());

    public static final String EVENT_INTERFACE_NAME = classNameReplace(Event.class.getName());

    public static final Class[] SDK_CLASSES = new Class[]{
            Address.class,
            Block.class,
            BlockHeader.class,
            Contract.class,
            Event.class,
            Msg.class,
            Utils.class,
            View.class,
            Required.class,
            Payable.class,
    };

    public static final Class[] CONTRACT_USED_CLASSES = new Class[]{
            Boolean.class,
            Byte.class,
            Short.class,
            Character.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            String.class,
            StringBuilder.class,
            BigInteger.class,
            BigDecimal.class,
            List.class,
            ArrayList.class,
            LinkedList.class,
            Map.class,
            Map.Entry.class,
            HashMap.class,
            LinkedHashMap.class,
            Set.class,
            HashSet.class,
            Exception.class,
            RuntimeException.class,
            Collection.class,
    };

    public static final Class[] CONTRACT_LAZY_USED_CLASSES = new Class[]{
            Object.class,
            Class.class,
            Iterator.class,
    };

    public static final Class[] VM_INIT_CLASSES = new Class[]{
            ClassCastException.class,
            StackOverflowError.class,
    };

    public static final String[] SDK_CLASS_NAMES = new String[SDK_CLASSES.length];

    public static final String[] CONTRACT_USED_CLASS_NAMES = new String[CONTRACT_USED_CLASSES.length];

    public static final String[] CONTRACT_LAZY_USED_CLASS_NAMES = new String[CONTRACT_LAZY_USED_CLASSES.length];

    public static final String[] VM_INIT_CLASS_NAMES = new String[VM_INIT_CLASSES.length];

    static {
        for (int i = 0; i < SDK_CLASSES.length; i++) {
            SDK_CLASS_NAMES[i] = classNameReplace(SDK_CLASSES[i].getName());
        }
        for (int i = 0; i < CONTRACT_USED_CLASSES.length; i++) {
            CONTRACT_USED_CLASS_NAMES[i] = classNameReplace(CONTRACT_USED_CLASSES[i].getName());
        }
        for (int i = 0; i < CONTRACT_LAZY_USED_CLASSES.length; i++) {
            CONTRACT_LAZY_USED_CLASS_NAMES[i] = classNameReplace(CONTRACT_LAZY_USED_CLASSES[i].getName());
        }
        int length = VM_INIT_CLASSES.length;
        for (int i = 0; i < length; i++) {
            VM_INIT_CLASS_NAMES[i] = classNameReplace(VM_INIT_CLASSES[i].getName());
        }
    }

    public static String classNameReplace(String s) {
        return s.replace('.', '/');
    }

}

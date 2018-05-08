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
package io.nuls.kernel.utils;

import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class TransactionManager {

    private static final Map<Class<? extends Transaction>, Class> TX_SERVICE_MAP = new HashMap<>();

    public static void init() throws Exception {
        List<TransactionProcessor> beanList = SpringLiteContext.getBeanList(TransactionProcessor.class);
        for (TransactionProcessor processor : beanList) {
            registerProcessor(processor);
        }
    }

    private static void registerProcessor(TransactionProcessor processor) {
        Method[] methods = processor.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (!method.getName().equals("onCommit")) {
                continue;
            }
            Class paramType = method.getParameterTypes()[0];
            putTx(paramType, processor.getClass());
            break;
        }
    }

    public static final void putTx(Class<? extends Transaction> txClass, Class<? extends TransactionProcessor> txProcessorClass) {
        TX_SERVICE_MAP.put(txClass, txProcessorClass);
    }

    public static TransactionProcessor getProcessor(Class<? extends Transaction> txClass) {
        Class<? extends TransactionProcessor> txProcessorClass = TX_SERVICE_MAP.get(txClass);
        if (null == txProcessorClass) {
            return null;
        }
        return SpringLiteContext.getBean(txProcessorClass);
    }
}

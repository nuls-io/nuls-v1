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

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.core.SpringLiteContext;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class TransactionManager {

    private static final Map<Class<? extends Transaction>, Class<? extends TransactionProcessor>> TX_SERVICE_MAP = new HashMap<>();
    private static final Map<Integer, Class<? extends Transaction>> TYPE_TX_MAP = new HashMap<>();

    public static void init() throws Exception {
        List<TransactionProcessor> beanList = SpringLiteContext.getBeanList(TransactionProcessor.class);
        for (TransactionProcessor processor : beanList) {
            registerProcessor(processor);
        }
    }

    private static void registerProcessor(TransactionProcessor processor) {
        Method[] methods = processor.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (!"onCommit".equals(method.getName())) {
                continue;
            }
            Class paramType = method.getParameterTypes()[0];
            if (paramType.equals(Transaction.class)) {
                continue;
            }
            putTx(paramType, processor.getClass());
            break;
        }
    }

    public static final void putTx(Class<? extends Transaction> txClass, Class<? extends TransactionProcessor> txProcessorClass) {
        if (null != txProcessorClass) {
            TX_SERVICE_MAP.put(txClass, txProcessorClass);
        }
        try {
            Transaction tx = txClass.newInstance();
            TYPE_TX_MAP.put(tx.getType(), txClass);
        } catch (InstantiationException e) {
            Log.error(e);
        } catch (IllegalAccessException e) {
            Log.error(e);
        }
    }

    private static TransactionProcessor getProcessor(Class<? extends Transaction> txClass) {
        Class<? extends TransactionProcessor> txProcessorClass = TX_SERVICE_MAP.get(txClass);
        if (null == txProcessorClass) {
            return null;
        }
        return SpringLiteContext.getBean(txProcessorClass);
    }

    public static List<TransactionProcessor> getProcessorList(Class<? extends Transaction> txClass) {
        List<TransactionProcessor> list = new ArrayList<>();
        Class clazz = txClass;
        while (!clazz.equals(Transaction.class)) {
            TransactionProcessor txService = TransactionManager.getProcessor(clazz);
            if (null != txService) {
                list.add(0, txService);
            }
            clazz = clazz.getSuperclass();
        }
        return list;
    }

    public static List<TransactionProcessor> getAllProcessorList() {
        try {
            return SpringLiteContext.getBeanList(TransactionProcessor.class);
        } catch (Exception e) {
            Log.error(e);
            return new ArrayList<>();
        }
    }

    public static Transaction getInstance(NulsByteBuffer byteBuffer) throws Exception {
        int txType = (int) new NulsByteBuffer(byteBuffer.getPayloadByCursor()).readVarInt();
        Class<? extends Transaction> txClass = TYPE_TX_MAP.get(txType);
        if (null == txClass) {
            throw new NulsRuntimeException(KernelErrorCode.DATA_NOT_FOUND);
        }
        Transaction tx = byteBuffer.readNulsData(txClass.newInstance());
        return tx;
    }


    public static List<Transaction> getInstances(NulsByteBuffer byteBuffer, long txCount) throws Exception {
        List<Transaction> list = new ArrayList<>();
        for (int i = 0; i < txCount; i++) {
            list.add(getInstance(byteBuffer));
        }
        return list;
    }
}

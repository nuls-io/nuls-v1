/**
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
 */
package io.nuls.protocol.utils;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.service.intf.TransactionService;
import io.nuls.protocol.utils.io.NulsByteBuffer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class TransactionManager {

    private static final Map<Integer, Class<? extends Transaction>> TX_MAP = new HashMap<>();
    private static final Map<Class<? extends Transaction>, Class> TX_SERVICE_MAP = new HashMap<>();

    public static final void putTx(int txType, Class<? extends Transaction> txClass, Class<? extends TransactionService> txServiceClass) {
        if (TX_MAP.containsKey(txType)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "Transaction type repeating!");
        }
        TX_MAP.put(txType, txClass);
        TX_SERVICE_MAP.put(txClass, txServiceClass);
    }

    public static final Class<? extends Transaction> getTxClass(int txType) {
        return TX_MAP.get(txType);
    }

    public static Transaction getInstanceByType(int txType) throws NulsException {
        Class<? extends Transaction> txClass = getTxClass(txType);
        if (null == txClass) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "transaction type not exist!");
        }
        Transaction tx = null;
        try {
            tx = txClass.getConstructor().newInstance();
        } catch (Exception e) {
           throw new NulsException(e);
        }
        return tx;
    }

    public static List<Transaction> getInstances(NulsByteBuffer byteBuffer, long txCount) throws Exception {
        List<Transaction> list = new ArrayList<>();
        for (int i = 0; i < txCount; i++) {
            list.add(getInstance(byteBuffer));
        }
        return list;
    }

    public static Transaction getInstance(NulsByteBuffer byteBuffer) throws Exception {
        int txType = (int) new NulsByteBuffer(byteBuffer.getPayloadByCursor()).readVarInt();
        Class<? extends Transaction> txClass = getTxClass(txType);
        if (null == txClass) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "transaction type not exist!");
        }
        Transaction tx = byteBuffer.readNulsData(txClass.getConstructor().newInstance());
        return tx;
    }

    public static TransactionService getService(Class<? extends Transaction> txClass) {
        Class<? extends TransactionService> txServiceClass = TX_SERVICE_MAP.get(txClass);
        if (null == txServiceClass) {
            return null;
        }
        return NulsContext.getServiceBean(txServiceClass);
    }
}

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
package io.nuls.core.chain.manager;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.io.NulsByteBuffer;

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
    private static final Map<Class<? extends Transaction>, TransactionService> TX_SERVICE_MAP = new HashMap<>();

    public static final void putTx(int txType, Class<? extends Transaction> txClass, TransactionService txService) {
        if (TX_MAP.containsKey(txType)) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "Transaction type repeating!");
        }
        TX_MAP.put(txType, txClass);
        TX_SERVICE_MAP.put(txClass, txService);
    }

    public static final Class<? extends Transaction> getTxClass(int txType) {
        return TX_MAP.get(txType);
    }

    public static Transaction getInstanceByType(int txType) throws Exception {
        Class<? extends Transaction> txClass = getTxClass(txType);
        if (null == txClass) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "transaction type not exist!");
        }
        Transaction tx = txClass.getConstructor().newInstance();
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
        return TX_SERVICE_MAP.get(txClass);
    }

    public static void main(String[] args) throws Exception {
        //Transaction transferTransaction = TransactionManager.getInstance(new NulsByteBuffer(Hex.decode("02ffd9cc831f62010000fe40420f00117465737420313838207472616e73666572ffffffff000001000100000020022a978d91e0a98ed2e0b09adf5ed0d393b76f1ff728b0f033b151f07f83264e008c0100473045022100d0ed288a44caa622c4fc6c10a7f44f1548f37fbadfd62958f43ffc30baaa6bf102200ea94bd5f73613e264b231563f06801f03e3dcd26893ec5b2bb8a69f10eaa85441043040f3ceb0d5a9c1e19b3009bb5dd77b5f4415ad839eba781b616e3bb61bc7f3b50bbf03eb9ba8f7d75b029e0cf7e59c07635e8d5d36f84b84ad135da5a66a920200003c916004000000170100baa4b44f3acfcd8900e9133a3434f161462f567b410000000000000000010014971dc7813785e9ebe9f7974ebbc200cf084e152a01c08148e1c76b0100170100f62a53127b0c3586553bb72806e3dce23fe3ec7d3f000000000000000001001410f10fbf9767cfdb722f299b6b41ed51d2426e7f")));
        //Transaction transferTransaction1 = TransactionManager.getInstance(new NulsByteBuffer(Hex.decode("02ffd9cc831f62010000fe40420f00117465737420313838207472616e73666572ffffffff000001000100000020022a978d91e0a98ed2e0b09adf5ed0d393b76f1ff728b0f033b151f07f83264e008c0100473045022100d0ed288a44caa622c4fc6c10a7f44f1548f37fbadfd62958f43ffc30baaa6bf102200ea94bd5f73613e264b231563f06801f03e3dcd26893ec5b2bb8a69f10eaa85441043040f3ceb0d5a9c1e19b3009bb5dd77b5f4415ad839eba781b616e3bb61bc7f3b50bbf03eb9ba8f7d75b029e0cf7e59c07635e8d5d36f84b84ad135da5a66a920201c08148e1c76b0100170100f62a53127b0c3586553bb72806e3dce23fe3ec7d3f000000000000000001001410f10fbf9767cfdb722f299b6b41ed51d2426e7f00003c916004000000170100baa4b44f3acfcd8900e9133a3434f161462f567b410000000000000000010014971dc7813785e9ebe9f7974ebbc200cf084e152a")));
        //UtxoOutput utxoOutput1 = (TransferTransaction)TransferTransaction
        System.out.println("  ");
    }
}

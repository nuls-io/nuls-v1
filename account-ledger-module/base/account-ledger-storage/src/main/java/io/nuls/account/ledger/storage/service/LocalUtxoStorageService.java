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
package io.nuls.account.ledger.storage.service;

import io.nuls.db.model.Entry;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Result;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Facjas
 * @date 2018/5/10.
 */
public interface LocalUtxoStorageService {

    Result saveUTXO(byte[] key, byte[] value);

    Result batchSaveUTXO(Map<byte[], byte[]> utxos);

    Result deleteUTXO(byte[] key);

    Result batchDeleteUTXO(Set<byte[]> utxos);

    List<Entry<byte[], byte[]>> loadAllCoinList();

    Result batchSaveAndDeleteUTXO(List<Entry<byte[], byte[]>> utxosToSave, List<byte[]> utxosToDelete);

    byte[] getUtxoBytes(byte[] owner);

    Coin getUtxo(byte[] owner);
}

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
package io.nuls.ledger.service.impl;

import io.nuls.account.entity.Address;
import io.nuls.cache.service.intf.CacheService;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.entity.Balance;
import io.nuls.ledger.entity.UtxoBalance;
import io.nuls.ledger.entity.UtxoOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author Niels
 * @date 2017/11/17
 */
public class
LedgerCacheService {
    private static LedgerCacheService instance = new LedgerCacheService();
    private CacheService<String, Balance> cacheService;
    private CacheService<String, UtxoOutput> utxoCacheService;


    private LedgerCacheService() {
        cacheService = NulsContext.getServiceBean(CacheService.class);
        cacheService.createCache(LedgerConstant.STANDING_BOOK, 1024);
        utxoCacheService = NulsContext.getServiceBean(CacheService.class);
        utxoCacheService.createCache(LedgerConstant.UTXO, 1024);
    }

    public static LedgerCacheService getInstance() {
        return instance;
    }

    public void clear() {
        this.cacheService.clearCache(LedgerConstant.STANDING_BOOK);
        this.utxoCacheService.clearCache(LedgerConstant.UTXO);
    }

    public void destroy() {
        this.cacheService.removeCache(LedgerConstant.STANDING_BOOK);
        this.utxoCacheService.removeCache(LedgerConstant.UTXO);
    }

    public void putBalance(String address, Balance balance) {
        if (null == balance || StringUtils.isBlank(address)) {
            return;
        }
        cacheService.putElement(LedgerConstant.STANDING_BOOK, address, balance);
    }

    public void removeBalance(String address) {
        cacheService.removeElement(LedgerConstant.STANDING_BOOK, address);
    }

    public Balance getBalance(String address) {
        return cacheService.getElement(LedgerConstant.STANDING_BOOK, address);
    }

    public void putUtxo(String key, UtxoOutput output) {
        utxoCacheService.putElement(LedgerConstant.UTXO, key, output);

        String address = Address.fromHashs(output.getAddress()).getBase58();
        UtxoBalance balance = (UtxoBalance) getBalance(address);
        if (balance == null) {
            balance = new UtxoBalance();
            List<UtxoOutput> outputs = new ArrayList<>();
            outputs.add(output);
            balance.setUnSpends(outputs);
            putBalance(address, balance);
        } else {
            balance.getUnSpends().add(output);
            Collections.sort(balance.getUnSpends());
        }
    }

    public UtxoOutput getUtxo(String key) {
        return utxoCacheService.getElement(LedgerConstant.UTXO, key);
    }

    public void removeUtxo(String key) {
        UtxoOutput output = getUtxo(key);
        utxoCacheService.removeElement(LedgerConstant.UTXO, key);
        if (output != null) {
            UtxoBalance balance = (UtxoBalance) getBalance(Address.fromHashs(output.getAddress()).getBase58());
            if (balance != null) {
                balance.getUnSpends().remove(output);
            }
        }
    }

    public boolean updateUtxoStatus(String key, int newStatus, int oldStatus) {

        if (!utxoCacheService.containsKey(LedgerConstant.UTXO, key)) {
            return false;
        }
        UtxoOutput output = utxoCacheService.getElement(LedgerConstant.UTXO, key);
        if (output.getStatus() != oldStatus) {
            return false;
        }
        output.setStatus(newStatus);
        return true;
    }
}

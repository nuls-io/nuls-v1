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
package io.nuls.account.ledger.base.util;

import io.nuls.account.model.Account;
import io.nuls.account.service.AccountService;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.VarInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * author Facjas
 * date 2018/5/27.
 */
@Component
public class AccountLegerUtils {

    @Autowired
    private static AccountService accountService;

    private final static int TX_HASH_LENGTH = NulsDigestData.HASH_LENGTH;

    public static boolean isLocalAccount(byte[] address) {
        Collection<Account> localAccountList = accountService.getAccountList().getData();
        if (localAccountList == null || localAccountList.size() == 0) {
            return false;
        }

        for (Account account : localAccountList) {
            if (Arrays.equals(account.getAddress().getAddressBytes(), address)) {
                return true;
            }
        }
        return false;
    }

    public static List<byte[]> getLocalAddresses() {
        List<byte[]> result = new ArrayList<>();
        Collection<Account> localAccountList = accountService.getAccountList().getData();
        if (localAccountList == null || localAccountList.size() == 0) {
            return result;
        }
        List<byte[]> destAddresses = new ArrayList<>();
        for (Account account : localAccountList) {
            destAddresses.add(account.getAddress().getAddressBytes());
        }

        return destAddresses;
    }

    public static List<byte[]> getRelatedAddresses(Transaction tx) {
        List<byte[]> result = new ArrayList<>();
        if (tx == null) {
            return result;
        }
        Collection<Account> localAccountList = accountService.getAccountList().getData();
        if (localAccountList == null || localAccountList.size() == 0) {
            return result;
        }
        List<byte[]> destAddresses = new ArrayList<>();
        for (Account account : localAccountList) {
            destAddresses.add(account.getAddress().getAddressBytes());
        }

        return getRelatedAddresses(tx, destAddresses);
    }

    public static List<byte[]> getRelatedAddresses(Transaction tx, List<byte[]> addresses) {
        List<byte[]> result = new ArrayList<>();
        if (tx == null) {
            return result;
        }
        if (addresses == null || addresses.size() == 0) {
            return result;
        }
        //获取交易中的地址
        List<byte[]> sourceAddresses = tx.getAllRelativeAddress();
        if (sourceAddresses == null || sourceAddresses.size() == 0) {
            return result;
        }

        for (byte[] tempSourceAddress : sourceAddresses) {
            for (byte[] tempDestAddress : addresses) {
                if (Arrays.equals(tempDestAddress, tempSourceAddress)) {
                    result.add(tempSourceAddress);
                    continue;
                }
            }
        }
        return result;
    }

    public static boolean isLocalTransaction(Transaction tx) {

        if (tx == null) {
            return false;
        }
        Collection<Account> localAccountList = accountService.getAccountList().getData();
        if (localAccountList == null || localAccountList.size() == 0) {
            return false;
        }
        List<byte[]> addresses = tx.getAllRelativeAddress();
        for (int j = 0; j < addresses.size(); j++) {
            if (AccountLegerUtils.isLocalAccount(addresses.get(j))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTxRelatedToAddress(Transaction tx, byte[] address){
        List<byte[]> sourceAddresses = tx.getAllRelativeAddress();
        for (byte[] tmpAddress : sourceAddresses){
            if(Arrays.equals(tmpAddress, address)){
                return true;
            }
        }
        return false;
    }

    public static byte[] getTxHashBytes(byte[] fromBytes) {
        if(fromBytes == null || fromBytes.length < TX_HASH_LENGTH) {
            return null;
        }
        byte[] txBytes = new byte[TX_HASH_LENGTH];
        System.arraycopy(fromBytes, 0, txBytes, 0, TX_HASH_LENGTH);
        return txBytes;
    }

    public static String getTxHash(byte[] fromBytes) {
        byte[] txBytes = getTxHashBytes(fromBytes);
        if(txBytes != null) {
            return Hex.encode(txBytes);
        }
        return null;
    }

    public static byte[] getIndexBytes(byte[] fromBytes) {
        if(fromBytes == null || fromBytes.length < TX_HASH_LENGTH) {
            return null;
        }
        int length = fromBytes.length - TX_HASH_LENGTH;
        byte[] indexBytes = new byte[length];
        System.arraycopy(fromBytes, TX_HASH_LENGTH, indexBytes, 0, length);
        return indexBytes;
    }

    public static Integer getIndex(byte[] fromBytes) {
        byte[] indexBytes = getIndexBytes(fromBytes);
        if(indexBytes != null) {
            VarInt varInt = new VarInt(indexBytes, 0);
            return Math.toIntExact(varInt.value);
        }
        return null;
    }
}

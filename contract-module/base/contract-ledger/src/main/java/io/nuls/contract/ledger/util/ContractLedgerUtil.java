/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.ledger.util;

import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.storage.service.ContractAddressStorageService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.AddressTool;

import java.util.ArrayList;
import java.util.List;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/6
 */
@Component
public class ContractLedgerUtil {

    @Autowired
    private static ContractAddressStorageService contractAddressStorageService;

    public static boolean isExistContractAddress(byte[] addressBytes) {
        if(addressBytes == null) {
            return false;
        }
        return contractAddressStorageService.isExistContractAddress(addressBytes);
    }

    public static boolean isLegalContractAddress(byte[] addressBytes) {
        if(addressBytes == null) {
            return false;
        }
        return AddressTool.validContractAddress(addressBytes);
    }

    /**
     * 获取tx中是智能合约地址的地址列表
     *
     * @param tx
     * @return
     */
    public static List<byte[]> getRelatedAddresses(Transaction tx) {
        List<byte[]> result = new ArrayList<>();
        if (tx == null) {
            return result;
        }
        List<byte[]> txAddressList = tx.getAllRelativeAddress();
        if (txAddressList == null || txAddressList.size() == 0) {
            return result;
        }
        List<byte[]> destAddresses = new ArrayList<>();
        for (byte[] txAddress : txAddressList) {
            if(isLegalContractAddress(txAddress)) {
                result.add(txAddress);
            }
        }

        return result;
    }

    /**
     * 判断交易是否与智能合约相关
     *
     * @param tx
     * @return
     */
    public static boolean isRelatedTransaction(Transaction tx) {
        if (tx == null) {
            return false;
        }
        if(tx.getType() == ContractConstant.TX_TYPE_CONTRACT_TRANSFER) {
            return true;
        }
        List<byte[]> txAddressList = tx.getAllRelativeAddress();
        for (int j = 0, length = txAddressList.size(); j < length; j++) {
            if (isLegalContractAddress(txAddressList.get(j))) {
                return true;
            }
        }
        return false;
    }

}

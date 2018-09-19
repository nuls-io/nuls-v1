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
package io.nuls.utxo.accounts.service.impl;


import io.nuls.account.constant.AccountErrorCode;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.utxo.accounts.constant.UtxoAccountsErrorCode;
import io.nuls.utxo.accounts.model.UtxoAccountsBalance;
import io.nuls.utxo.accounts.service.UtxoAccountsBalanceService;
import io.nuls.utxo.accounts.storage.po.LockedBalance;
import io.nuls.utxo.accounts.storage.po.UtxoAccountsBalancePo;
import io.nuls.utxo.accounts.storage.service.UtxoAccountsStorageService;

import java.math.BigDecimal;
import java.util.List;

public class UtxoAccountsBalanceServiceImpl implements UtxoAccountsBalanceService {
    @Autowired
    UtxoAccountsStorageService utxoAccountsStorageService;
    public Result<UtxoAccountsBalance> getUtxoAccountsBalance(byte[] owner){
        if (!AddressTool.validAddress(AddressTool.getStringAddressByBytes(owner))) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        try {
            Result<UtxoAccountsBalancePo> utxoAccountsBalance=utxoAccountsStorageService.getUtxoAccountsBalanceByAddress(owner);
            long synBlockHeight=utxoAccountsStorageService.getHadSynBlockHeight();
            if(null==utxoAccountsBalance || null==utxoAccountsBalance.getData()){
                return Result.getFailed(UtxoAccountsErrorCode.DATA_NOT_FOUND);
            }
            UtxoAccountsBalancePo dbAccountsBalance =utxoAccountsBalance.getData();
            UtxoAccountsBalance accountBalance=new UtxoAccountsBalance();
            accountBalance.setOwner(dbAccountsBalance.getOwner());
            long totalNa=dbAccountsBalance.getOutputBalance()-(dbAccountsBalance.getInputBalance());
            totalNa+=dbAccountsBalance.getContractToBalance();
            totalNa-=dbAccountsBalance.getContractFromBalance();
            accountBalance.setBalance(Na.valueOf(totalNa));
            long permanentLockedNa=dbAccountsBalance.getLockedPermanentBalance()-(dbAccountsBalance.getUnLockedPermanentBalance());
            long lockedNa=permanentLockedNa;
            List<LockedBalance> timeLockedBalance=dbAccountsBalance.getLockedTimeList();
            long currentTime=TimeService.currentTimeMillis();
            for(LockedBalance balance:timeLockedBalance){
                if(balance.getLockedTime()>currentTime){
                    lockedNa+=balance.getLockedBalance();
                }else{
                    break;
                }
            }
            List<LockedBalance> heightLockedBalance=dbAccountsBalance.getLockedHeightList();
            for(LockedBalance balance:heightLockedBalance){
                if(balance.getLockedTime()>synBlockHeight){
                    lockedNa+=balance.getLockedBalance();
                }else{
                    break;
                }
            }
            accountBalance.setHadLocked(Na.valueOf(lockedNa));
            return Result.getSuccess().setData(accountBalance);
        } catch (NulsException e) {
            Log.error(e);
        }
        return Result.getFailed(UtxoAccountsErrorCode.SYS_UNKOWN_EXCEPTION);
    }
}

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
package io.nuls.utxo.accounts.storage.service.impl;

import io.nuls.db.service.BatchOperation;
import io.nuls.db.service.DBService;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.script.Script;
import io.nuls.kernel.script.SignatureUtil;
import io.nuls.utxo.accounts.storage.constant.UtxoAccountsStorageConstant;
import io.nuls.utxo.accounts.storage.po.LocalCacheBlockBalance;
import io.nuls.utxo.accounts.storage.po.UtxoAccountsBalancePo;
import io.nuls.utxo.accounts.storage.po.UtxoAccountsSynInfo;
import io.nuls.utxo.accounts.storage.service.UtxoAccountsStorageService;
import io.nuls.ledger.service.LedgerService;
import io.nuls.protocol.service.BlockService;

import java.util.List;

@Service
public class UtxoAccountsStorageServiceImpl implements UtxoAccountsStorageService, InitializingBean {
    @Autowired
    private DBService dbService;
    @Autowired
    BlockService blockService;
    @Autowired
    LedgerService ledgerService;
    @Override
    public Result saveUtxoAccountsInfo(byte[] addressBytes, UtxoAccountsBalancePo balance) {
        try {
            return dbService.putModel(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_CONFIRMED_BALANCE, addressBytes, balance);
        } catch (Exception e) {
            return Result.getFailed();
        }
    }

    @Override
    public Result saveByteUtxoAccountsInfo(byte[] addressBytes, UtxoAccountsBalancePo balance) {
        try {
            return dbService.put(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_CONFIRMED_BALANCE, addressBytes, balance.serialize());
        } catch (Exception e) {
            return Result.getFailed();
        }
    }

    @Override
    public Result batchSaveByteUtxoAcountsInfo(List<UtxoAccountsBalancePo> list) {
        BatchOperation batch = dbService.createWriteBatch(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_CONFIRMED_BALANCE);
       try {
           for (UtxoAccountsBalancePo balance : list) {
               batch.put(balance.getOwner(), balance.serialize());
           }
           Result batchResult = batch.executeBatch();
           return batchResult;
       }catch (Exception e){
           return Result.getFailed();
       }
    }

    @Override
    public Result deleteUtxoAcountsInfo(byte[] addressBytes) {
        return dbService.delete(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_CONFIRMED_BALANCE,addressBytes);

    }


    @Override
    public long getHadSynBlockHeight() {
        //获取当前解析的高度
        //初始时候返回-1
       UtxoAccountsSynInfo utxoAccountsSynInfo= dbService.getModel(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_BLOCK_CACHE, UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_BLOCK_SYN_KEY, UtxoAccountsSynInfo.class);
       if(utxoAccountsSynInfo==null) {
          return -1;
       }
       return utxoAccountsSynInfo.getHadSynBlockHeight();
    }

    @Override
    public Result saveHadSynBlockHeight(long height) {
        UtxoAccountsSynInfo utxoAccountsSynInfo=new UtxoAccountsSynInfo(height);
        utxoAccountsSynInfo.setUpdateTimeMillion(System.currentTimeMillis());
        Result result=dbService.putModel(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_BLOCK_CACHE,UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_BLOCK_SYN_KEY,utxoAccountsSynInfo);
        return result;
    }

    @Override
    public Result<UtxoAccountsBalancePo> getUtxoAccountsBalanceByAddress(byte[] addressBytes) throws NulsException {
        //获取账号信息
        if (addressBytes == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        byte[] bytes = null;
        if(addressBytes[2] == 3){
            Script scriptPubkey = SignatureUtil.createOutputScript(addressBytes);
            bytes = scriptPubkey.getProgram();
        }else{
         bytes = addressBytes;
        }
        byte []balance = dbService.get(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_CONFIRMED_BALANCE, bytes);
        UtxoAccountsBalancePo b=new UtxoAccountsBalancePo();
        b.parse(balance,0);
        if (b.getOwner()==null){
            return Result.getSuccess().setData(null);
        }
        return Result.getSuccess().setData(b);
    }

    @Override
    public Result<LocalCacheBlockBalance> getLocalCacheBlock(long height)  throws NulsException {
//        LocalCacheBlockBalance balance=dbService.getModel(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_BLOCK_CACHE,String.valueOf(height).getBytes(),LocalCacheBlockBalance.class);
        byte []block = dbService.get(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_BLOCK_CACHE, String.valueOf(height).getBytes());
        LocalCacheBlockBalance localCacheBlockBalance=new LocalCacheBlockBalance();
        localCacheBlockBalance.parse(block,0);
        if(localCacheBlockBalance.getHash()==null){
            return Result.getSuccess().setData(null);
        }
        return Result.getSuccess().setData(localCacheBlockBalance);
    }

    @Override
    public Result saveLocalCacheBlock(long height,LocalCacheBlockBalance localCacheBlockBalance) {
//        Result result=dbService.putModel(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_BLOCK_CACHE,String.valueOf(height).getBytes(),localCacheBlockBalance);
        try {
            return dbService.put(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_BLOCK_CACHE, String.valueOf(height).getBytes(), localCacheBlockBalance.serialize());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.getFailed();
        }
    }


    @Override
    public Result deleteLocalCacheBlock(long height) {
        return dbService.delete(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_BLOCK_CACHE,String.valueOf(height).getBytes());
    }

    @Override
    public Transaction getTx(NulsDigestData hash) {
        return ledgerService.getTx(hash);
    }

    @Override
    public Result<Block> getBlock(long height) {
        return blockService.getBlock(height,true);
    }


    @Override
    public void afterPropertiesSet() throws NulsException {
        dbService.createArea(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_CONFIRMED_BALANCE);
        dbService.createArea(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_BLOCK_CACHE);
//        dbService.createArea(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_LOCKEDTIME_BALANCE);
//        dbService.createArea(UtxoAccountsStorageConstant.DB_NAME_UTXO_ACCOUNTS_LOCKEDHEIGHT_BALANCE);

    }
}

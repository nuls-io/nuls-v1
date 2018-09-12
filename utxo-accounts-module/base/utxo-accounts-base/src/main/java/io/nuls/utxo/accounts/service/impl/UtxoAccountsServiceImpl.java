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

import io.nuls.contract.dto.ContractResult;
import io.nuls.contract.dto.ContractTransfer;
import io.nuls.contract.service.ContractService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.utxo.accounts.util.UtxoAccountsUtil;
import io.nuls.utxo.accounts.constant.UtxoAccountsConstant;
import io.nuls.utxo.accounts.service.UtxoAccountsService;
import io.nuls.utxo.accounts.storage.constant.UtxoAccountsStorageConstant;
import io.nuls.utxo.accounts.storage.po.LocalCacheBlockBalance;
import io.nuls.utxo.accounts.storage.po.LockedBalance;
import io.nuls.utxo.accounts.storage.po.UtxoAccountsBalancePo;
import io.nuls.utxo.accounts.storage.service.UtxoAccountsStorageService;

import java.nio.ByteBuffer;
import java.util.*;

@Service
public class UtxoAccountsServiceImpl implements UtxoAccountsService {
    private static ByteBuffer buffer = ByteBuffer.allocate(8);
    @Autowired
    UtxoAccountsStorageService utxoAccountsStorageService;
    @Autowired
    ContractService contractService;

    private boolean isPermanentLocked(int txType) {
        if (txType == UtxoAccountsConstant.TX_TYPE_REGISTER_AGENT || txType == UtxoAccountsConstant.TX_TYPE_JOIN_CONSENSUS) {
            return true;
        }
        return false;
    }

    private boolean isPermanentUnLocked(int txType) {
        if (txType == UtxoAccountsConstant.TX_TYPE_CANCEL_DEPOSIT || txType == UtxoAccountsConstant.TX_TYPE_STOP_AGENT) {
            return true;
        }
        return false;
    }

    private byte[] getInputAddress(Coin from) {
        byte[] fromHash;
        int fromIndex;
        byte[] owner = from.getOwner();
        // owner拆分出txHash和index
        fromHash = UtxoAccountsUtil.getTxHashBytes(owner);
        fromIndex = UtxoAccountsUtil.getIndex(owner);
        NulsDigestData fromHashObj = new NulsDigestData();
        try {
            fromHashObj.parse(fromHash, 0);
            Transaction outPutTx = utxoAccountsStorageService.getTx(fromHashObj);
            return outPutTx.getCoinData().getTo().get(fromIndex).getOwner();
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }
    }

    private boolean buildUtxoAccountsMap(Map<String, UtxoAccountsBalancePo> utxoAccountsMap, Block block) {
        List<Transaction> txs = block.getTxs();
        int txIndex = 0;
        for (Transaction tx : txs) {
            if (tx.getCoinData() == null) {
                return true;
            }
            List<Coin> from = tx.getCoinData().getFrom();
            List<Coin> to = tx.getCoinData().getTo();

            for (Coin inputCoin : from) {
                byte[] inputOwner = getInputAddress(inputCoin);
                inputCoin.setOwner(inputOwner);
                buildUtxoAccountsBalance(utxoAccountsMap, inputCoin, tx, txIndex, true);
            }
            for (Coin outputCoin : to) {
                buildUtxoAccountsBalance(utxoAccountsMap, outputCoin, tx, txIndex, false);
            }
            //增加智能合约内部交易逻辑
            if (tx.getType() == UtxoAccountsConstant.TX_TYPE_CALL_CONTRACT) {
                ContractResult contractExecuteResult = contractService.getContractExecuteResult(tx.getHash());
                List<ContractTransfer> transferList = contractExecuteResult.getTransfers();
                buildContractTranfersBalance(utxoAccountsMap, transferList, block.getHeader().getHeight(), txIndex);
            }
            txIndex++;
        }
        return true;
    }

    /**
     * build utxoAccounts Map(address,balance)
     *
     * @param utxoAccountsMap
     * @param coin
     * @param tx
     * @param txIndex
     * @param isInput
     */
    private void buildUtxoAccountsBalance(Map<String, UtxoAccountsBalancePo> utxoAccountsMap, Coin coin, Transaction tx, int txIndex, boolean isInput) {
        long netBlockHeight = NulsContext.getInstance().getNetBestBlockHeight();
        String address = AddressTool.getStringAddressByBytes(coin.getOwner());
        UtxoAccountsBalancePo balance = utxoAccountsMap.get(address);
        if (null == balance) {
            balance = new UtxoAccountsBalancePo();
            balance.setOwner(coin.getOwner());
            utxoAccountsMap.put(address, balance);
        }
        if (isInput) {
            if (isPermanentUnLocked(tx.getType())) {
                //remove balance
                balance.setUnLockedPermanentBalance(balance.getUnLockedPermanentBalance() + (coin.getNa().getValue()));
            }
            balance.setInputBalance(balance.getInputBalance() + (coin.getNa().getValue()));
        } else {
            if (isPermanentLocked(tx.getType())) {
                //add locked balance
                if (coin.getLockTime() == -1) {
                    balance.setLockedPermanentBalance(balance.getLockedPermanentBalance() + (coin.getNa().getValue()));
                }
            } else {
                //add lockedTime output
                if (coin.getLockTime() > 0) {
                    //by time
                    if (coin.getLockTime() > TimeService.currentTimeMillis()) {
                        LockedBalance lockedBalance = new LockedBalance();
                        lockedBalance.setLockedBalance(coin.getNa().getValue());
                        lockedBalance.setLockedTime(coin.getLockTime());
                        balance.getLockedTimeList().add(lockedBalance);
                    } else {
                        //by height
                        if (coin.getLockTime() > netBlockHeight) {
                            LockedBalance lockedBalance = new LockedBalance();
                            lockedBalance.setLockedBalance(coin.getNa().getValue());
                            lockedBalance.setLockedTime(coin.getLockTime());
                            balance.getLockedHeightList().add(lockedBalance);
                        }
                    }

                }
            }
            balance.setOutputBalance(balance.getOutputBalance() + (coin.getNa()).getValue());
        }
//        balance.setOwner(coin.getOwner());
        balance.setBlockHeight(tx.getBlockHeight());
        balance.setTxIndex(txIndex);
    }

    /**
     * @param utxoAccountsMap
     * @param transferList
     * @param blockHeight
     * @param txIndex
     */
    private void buildContractTranfersBalance(Map<String, UtxoAccountsBalancePo> utxoAccountsMap,
                                              List<ContractTransfer> transferList, long blockHeight, int txIndex) {
        for (ContractTransfer contractTransfer : transferList) {
            byte[] from = contractTransfer.getFrom();
            byte[] to = contractTransfer.getTo();
            String addressFrom = AddressTool.getStringAddressByBytes(from);
            String addressTo = AddressTool.getStringAddressByBytes(to);
            UtxoAccountsBalancePo balanceFrom = utxoAccountsMap.get(addressFrom);
            UtxoAccountsBalancePo balanceTo = utxoAccountsMap.get(addressTo);
            if (null == balanceFrom) {
                balanceFrom = new UtxoAccountsBalancePo();
                balanceFrom.setOwner(from);
                utxoAccountsMap.put(addressFrom, balanceFrom);
            }
            balanceFrom.setBlockHeight(blockHeight);
            balanceFrom.setTxIndex(txIndex);
            balanceFrom.setContractFromBalance(balanceFrom.getContractFromBalance() + contractTransfer.getValue().getValue());
            if (null == balanceTo) {
                balanceTo = new UtxoAccountsBalancePo();
                balanceTo.setOwner(to);
                utxoAccountsMap.put(addressTo, balanceTo);
            }
            balanceTo.setBlockHeight(blockHeight);
            balanceTo.setTxIndex(txIndex);
            balanceTo.setContractToBalance(balanceTo.getContractToBalance() + contractTransfer.getValue().getValue());
        }
    }

    /**
     * build utxoAccounts  to list
     *
     * @param utxoAccountsMap
     * @return
     * @throws NulsException
     */
    private List<UtxoAccountsBalancePo> utxoAccountsMapToList(Map<String, UtxoAccountsBalancePo> utxoAccountsMap, LocalCacheBlockBalance preSnapshot)
            throws NulsException {
        List<UtxoAccountsBalancePo> list = new ArrayList<>();
        List<UtxoAccountsBalancePo> preList = new ArrayList<>();
        preSnapshot.setBalanceList(preList);
        Collection<UtxoAccountsBalancePo> utxoAccountsBalances = utxoAccountsMap.values();
        for (UtxoAccountsBalancePo balance : utxoAccountsBalances) {
            UtxoAccountsBalancePo localBalance = utxoAccountsStorageService.getUtxoAccountsBalanceByAddress(balance.getOwner()).getData();
            if (localBalance == null) {
                list.add(balance);
                UtxoAccountsBalancePo preBalance = new UtxoAccountsBalancePo();
                preBalance.setOwner(balance.getOwner());
                preList.add(preBalance);
            } else {
                preList.add(localBalance);
                UtxoAccountsBalancePo newBalance = new UtxoAccountsBalancePo();
                newBalance.setOwner(localBalance.getOwner());
                newBalance.setBlockHeight(balance.getBlockHeight());
                newBalance.setTxIndex(balance.getTxIndex());
                newBalance.setOutputBalance(localBalance.getOutputBalance() + (balance.getOutputBalance()));
                newBalance.setInputBalance(localBalance.getInputBalance() + (balance.getInputBalance()));
                newBalance.setContractFromBalance(localBalance.getContractFromBalance() + (balance.getContractFromBalance()));
                newBalance.setContractToBalance(localBalance.getContractToBalance() + (balance.getContractToBalance()));
                newBalance.setLockedPermanentBalance(localBalance.getLockedPermanentBalance() + (balance.getLockedPermanentBalance()));
                newBalance.setUnLockedPermanentBalance(localBalance.getUnLockedPermanentBalance() + (balance.getUnLockedPermanentBalance()));
                clearLockedBalance(localBalance, balance, newBalance);
                list.add(newBalance);
            }
        }
        return list;
    }

    private void clearLockedBalance(UtxoAccountsBalancePo dbBalance, UtxoAccountsBalancePo addBalance, UtxoAccountsBalancePo newBalance) {
        List<LockedBalance> newTimeList = new ArrayList<>();
        List<LockedBalance> newHeightList = new ArrayList<>();
        List<LockedBalance> heightList = dbBalance.getLockedHeightList();
        List<LockedBalance> timeList = dbBalance.getLockedTimeList();
        long netBlockHeight = NulsContext.getInstance().getNetBestBlockHeight();
        for (LockedBalance heightBalance : heightList) {
            if (heightBalance.getLockedTime() > netBlockHeight) {
                newHeightList.add(heightBalance);
            } else {
                break;
            }
        }
        newHeightList.addAll(addBalance.getLockedHeightList());
        newHeightList.sort(LockedBalance::compareByLockedTime);
        newBalance.setLockedHeightList(newHeightList);
        long serverTime = TimeService.currentTimeMillis();
        for (LockedBalance timeBalance : timeList) {
            if (timeBalance.getLockedTime() > serverTime) {
                newTimeList.add(timeBalance);
            } else {
                break;
            }
        }
        newTimeList.addAll(addBalance.getLockedTimeList());
        newTimeList.sort(LockedBalance::compareByLockedTime);
        newBalance.setLockedTimeList(newTimeList);

    }


    public boolean rollbackBlock(LocalCacheBlockBalance block) throws NulsException {
        Log.info("rollbackBlock:" + block.getBlockHeight());
        if (block.getBlockHeight() == 0) {
            utxoAccountsStorageService.deleteLocalCacheBlock(block.getBlockHeight());
            return true;
        }
        //更新最新高度
        utxoAccountsStorageService.saveHadSynBlockHeight(block.getBlockHeight() - 1);
        LocalCacheBlockBalance localPreBlock = utxoAccountsStorageService.getLocalCacheBlock(block.getBlockHeight()).getData();
        //批量更新上一区块数据
        List<UtxoAccountsBalancePo> list = localPreBlock.getBalanceList();
        if (list.size() > 0) {
            utxoAccountsStorageService.batchSaveByteUtxoAcountsInfo(list);
        }
        //删除本地缓存区块
        utxoAccountsStorageService.deleteLocalCacheBlock(block.getBlockHeight());
        return true;
    }

    @Override
    public boolean validateIntegrityBootstrap(long hadSynBlockHeight) throws NulsException {
        Log.info("utxoAccountsModule validateIntegrityBootstrap hadSynBlockHeight:" + hadSynBlockHeight);
        LocalCacheBlockBalance localCacheNextBlock = null;
        try {
            localCacheNextBlock = utxoAccountsStorageService.getLocalCacheBlock(hadSynBlockHeight + 1).getData();
        } catch (NulsException e) {
            Log.error(e);
            return false;
        }
        if (localCacheNextBlock == null) {
            //无不一致数据
            return true;
        }
        List<UtxoAccountsBalancePo> utxoAccountsBalances = localCacheNextBlock.getBalanceList();
        if (utxoAccountsBalances == null) {
            //无交易数据
            return true;
        }
        return rollbackBlock(localCacheNextBlock);
    }


    /**
     * @param blockHeight
     * @return
     */
    @Override
    public boolean synBlock(long blockHeight) {
        Log.debug("synBlock begin===blockHeight:" + blockHeight);
        Block nodeBlock = utxoAccountsStorageService.getBlock(blockHeight).getData();
        if (nodeBlock == null) {
            Log.error("utxoAccounts getBlock faile,blockHeight:" + blockHeight);
            return false;
        }
        boolean hadRoll = false;
        try {
            //get local pre block info /从本地取上一个已同步的区块
            LocalCacheBlockBalance localLatestCacheBlock = utxoAccountsStorageService.getLocalCacheBlock(blockHeight - 1).getData();
            //rollback judge /判断回滚
            while (localLatestCacheBlock != null && !nodeBlock.getHeader().getPreHash().equals(localLatestCacheBlock.getHash())) {
                //roll back info /进行数据回滚
                rollbackBlock(localLatestCacheBlock);
                blockHeight--;
                //get pre block
                localLatestCacheBlock = utxoAccountsStorageService.getLocalCacheBlock(blockHeight - 1).getData();
                nodeBlock = utxoAccountsStorageService.getBlock(blockHeight).getData();
                hadRoll = true;
            }
            if (hadRoll) {
                return false;
            }
        } catch (NulsException e) {
            Log.error(e);
            Log.error("block syn error======blockHeight:" + blockHeight);
            return false;
        }
        //begin syn block/开始同步区块
        //analysis block/解析区块
        Map<String, UtxoAccountsBalancePo> utxoAccountsMap = new HashMap<>();
        if (!buildUtxoAccountsMap(utxoAccountsMap, nodeBlock)) {
            return false;
        }
        List<UtxoAccountsBalancePo> list = new ArrayList<>();

        LocalCacheBlockBalance localCacheBlockBalance = new LocalCacheBlockBalance();
//        LocalCacheBlockBalance preSnapshot=new LocalCacheBlockBalance();
        try {
            list = utxoAccountsMapToList(utxoAccountsMap, localCacheBlockBalance);
        } catch (NulsException e) {
            Log.info("utxoAccountsMapToList error======blockHeight:" + blockHeight);
            return false;
        }
        localCacheBlockBalance.setHash(nodeBlock.getHeader().getHash());
        localCacheBlockBalance.setPreHash(nodeBlock.getHeader().getPreHash());
//        localCacheBlockBalance.setBalanceList(list);
        localCacheBlockBalance.setBlockHeight(blockHeight);
        //save cache block info/缓存最近解析信息
        utxoAccountsStorageService.saveLocalCacheBlock(blockHeight, localCacheBlockBalance);
//        utxoAccountsStorageService.saveLocalCacheChangeSnapshot(blockHeight,localCacheBlockBalance);


        utxoAccountsStorageService.batchSaveByteUtxoAcountsInfo(list);
        //update latest block height/更新最近高度
        utxoAccountsStorageService.saveHadSynBlockHeight(blockHeight);

        //delete overdue cache data/删除过期缓存数据
        if (blockHeight > UtxoAccountsStorageConstant.MAX_CACHE_BLOCK_NUM) {
            utxoAccountsStorageService.deleteLocalCacheBlock(blockHeight - UtxoAccountsStorageConstant.MAX_CACHE_BLOCK_NUM);
        }
        Log.debug("utxoAccounts synBlock success==blockHeight:" + blockHeight);
        return true;
    }

}

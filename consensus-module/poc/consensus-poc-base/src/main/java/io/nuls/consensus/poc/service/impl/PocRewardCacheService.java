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

package io.nuls.consensus.poc.service.impl;

import io.nuls.account.ledger.model.TransactionInfo;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.model.Account;
import io.nuls.account.service.AccountService;
import io.nuls.consensus.poc.model.RewardItem;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.ledger.service.LedgerService;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.model.tx.CoinBaseTransaction;
import io.nuls.protocol.service.BlockService;

import java.util.*;

/**
 * @author: Niels Wang
 * @date: 2018/5/24
 */
@Component
public class PocRewardCacheService {

    private long endHeight;
    private long totalRewardHeight = Long.MAX_VALUE;
    private Na totalReward = Na.ZERO;
    private Map<String, Na> totalMap = new HashMap<>();

    private Na todayReward = Na.ZERO;
    private Map<String, Na> todayMap = new HashMap<>();

    private Map<String, Map<Long, RewardItem>> todayRewardMap = new HashMap<>();

    @Autowired
    private AccountLedgerService accountLedgerService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private LedgerService ledgerService;

    public void initCache() {
        Collection<Account> accountList = accountService.getAccountList().getData();
        if (null == accountList || accountList.isEmpty()) {
            return;
        }

        long startTime = TimeService.currentTimeMillis() - 24 * 3600000L;
        long startHeight = NulsContext.getInstance().getBestHeight() - (24 * 3600 / ProtocolConstant.BLOCK_TIME_INTERVAL_SECOND);

        if (startHeight <= 0) {
            startHeight = 1;
        }
        long index = startHeight;
        while (true) {
            Block block = blockService.getBlock(index++).getData();
            if (null == block) {
                break;
            }
            if (block.getHeader().getHeight() < 1) {
                break;
            }
            if (block.getHeader().getTime() < startTime) {
                continue;
            }
            this.addBlock(block);
        }
        //本地账户的历史收益累计
        for (Account account : accountList) {
            List<TransactionInfo> list = accountLedgerService.getTxInfoList(account.getAddress().getAddressBytes()).getData();
            if (list == null || list.isEmpty()) {
                continue;
            }
            calcRewardHistory(account.getAddress().getBase58(), list, startHeight);
        }

        long totalValue = ledgerService.getWholeUTXO();
        this.totalRewardHeight = NulsContext.getInstance().getBestHeight();
        this.totalReward = Na.valueOf(totalValue - Na.MAX_NA_VALUE);
    }

    private void calcRewardHistory(String address, List<TransactionInfo> list, long startHeight) {
        byte[] addressByte = AddressTool.getAddress(address);
        for (TransactionInfo info : list) {
            if (info.getTxType() != ProtocolConstant.TX_TYPE_COINBASE) {
                continue;
            }
            CoinBaseTransaction tx = (CoinBaseTransaction) ledgerService.getTx(info.getTxHash());
            if (null == tx) {
                continue;
            }
            if (info.getBlockHeight() >= startHeight) {
                continue;
            }
            if (null == tx.getCoinData().getTo() || tx.getCoinData().getTo().isEmpty()) {
                continue;
            }
            for (Coin coin : tx.getCoinData().getTo()) {
                if (!Arrays.equals(addressByte, coin.getOwner())) {
                    continue;
                }
                Na na = totalMap.get(address);
                if (na == null) {
                    na = Na.ZERO;
                }
                na = na.add(coin.getNa());
                totalMap.put(address, na);
            }
        }

    }

    public void addBlock(Block block) {
        if (block.getHeader().getHeight() <= endHeight) {
            return;
        }

        CoinBaseTransaction tx = (CoinBaseTransaction) block.getTxs().get(0);
        this.calcReward(block.getHeader().getHeight(), tx);
    }

    private void calcReward(long height, CoinBaseTransaction tx) {
        if (null != tx.getCoinData().getTo() && !tx.getCoinData().getTo().isEmpty()) {
            long startTime = TimeService.currentTimeMillis() - 24 * 3600000L;
            for (Coin coin : tx.getCoinData().getTo()) {
                addRewardItem(height, tx.getTime(), coin, startTime);
            }
        }
        if (height > endHeight) {
            endHeight = height;
        }

    }


    private void addRewardItem(long height, long time, Coin coin, long startTime) {
        String address = AddressTool.getStringAddressByBytes(coin.getOwner());
        Map<Long, RewardItem> map = todayRewardMap.get(address);
        if (null == map) {
            map = new HashMap<>();
            todayRewardMap.put(address, map);
        }
        if (time > startTime) {
            map.put(height, new RewardItem(time, coin.getNa()));
        }

        Na tna = todayMap.get(address);
        if (tna == null) {
            tna = Na.ZERO;
        }
        tna = tna.add(coin.getNa());
        todayMap.put(address, tna);
        Na na = totalMap.get(address);
        if (na == null) {
            na = Na.ZERO;
        }
        na = na.add(coin.getNa());
        totalMap.put(address, na);
        if (height > totalRewardHeight) {
            totalReward = totalReward.add(coin.getNa());
        }
    }

    public void rollback(Block block) {
        if (block.getHeader().getHeight() > endHeight) {
            return;
        }

        CoinBaseTransaction tx = (CoinBaseTransaction) block.getTxs().get(0);
        if (null != tx.getCoinData().getTo() && !tx.getCoinData().getTo().isEmpty()) {
            for (Coin coin : tx.getCoinData().getTo()) {
                String address = AddressTool.getStringAddressByBytes(coin.getOwner());
                Map<Long, RewardItem> map = todayRewardMap.get(address);
                if (null == map) {
                    continue;
                }
                map.remove(block.getHeader().getHeight());

                Na na = totalMap.get(address);
                if (na == null) {
                    continue;
                }
                na = na.subtract(coin.getNa());
                todayMap.put(address, na);
            }
        }
        if (endHeight == block.getHeader().getHeight()) {
            endHeight = block.getHeader().getHeight() - 1;
        }
    }

    public Na getReward(String address) {
        Na na = totalMap.get(address);
        if (null == na) {
            na = Na.ZERO;
        }
        return na;
    }

    public Na getAllReward() {
        return totalReward;
    }


    public Na getAllRewardToday() {
        return todayReward;
    }

    public Na getRewardToday(String address) {
        Na na = todayMap.get(address);
        if (null == na) {
            na = Na.ZERO;
        }
        return na;
    }

    public void calcRewards() {
        List<String> list = new ArrayList<>(todayRewardMap.keySet());
        Na na = Na.ZERO;
        Map<String, Na> resultMap = new HashMap<>();
        long startTime = TimeService.currentTimeMillis() - 24 * 3600000L;
        for (String address : list) {
            Na reward = Na.ZERO;
            List<RewardItem> rewardItems = new ArrayList<>(todayRewardMap.get(address).values());
            for (RewardItem item : rewardItems) {
                if (item.getTime() < startTime) {
                    continue;
                }
                reward = reward.add(item.getNa());
            }
            na = na.add(reward);
            resultMap.put(address, reward);
        }
        this.todayReward = na;
        this.todayMap = resultMap;
    }
}

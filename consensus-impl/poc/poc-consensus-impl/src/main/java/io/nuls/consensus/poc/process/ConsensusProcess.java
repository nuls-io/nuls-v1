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
 */
package io.nuls.consensus.poc.process;

import io.nuls.account.entity.Account;
import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.manager.ChainManager;
import io.nuls.consensus.poc.manager.RoundManager;
import io.nuls.consensus.poc.model.MeetingMember;
import io.nuls.consensus.poc.model.MeetingRound;
import io.nuls.consensus.poc.provider.ConsensusSystemProvider;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.date.TimeService;
import io.nuls.network.service.NetworkService;
import io.nuls.poc.constant.ConsensusStatus;

/**
 * Created by ln on 2018/4/13.
 */
public class ConsensusProcess {

    private ChainManager chainManager;
    private RoundManager roundManager;
    private TxMemoryPool txMemoryPool;

    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);

    private boolean hasPacking;

    public ConsensusProcess(ChainManager chainManager, RoundManager roundManager, TxMemoryPool txMemoryPool) {
        this.chainManager = chainManager;
        this.roundManager = roundManager;
        this.txMemoryPool = txMemoryPool;
    }

    public void process() {

        boolean canPackage = checkCanPackage();

        if(!canPackage) {
            return;
        }

        doWork();
    }

    private void doWork() {

        MeetingRound round = roundManager.getCurrentRound();

        //check i am is a consensus node
        Account myAccount = round.getLocalPacker();
        if (myAccount == null) {
            return;
        }
        MeetingMember member = round.getMember(myAccount.getAddress().getBase58());
        if (!hasPacking && member.getPackStartTime() <= TimeService.currentTimeMillis()) {
            packing(member, round);
            hasPacking = true;
        }
    }

    private void packing(MeetingMember member, MeetingRound round) {
        // TODO
    }

    private boolean checkCanPackage() {

        // TODO load config

        // wait consensus ready running
        if(ConsensusSystemProvider.getConsensusStatus().ordinal() <= ConsensusStatus.WAIT_START.ordinal()) {
            return false;
        }

        // check network status
        if(networkService.getAvailableNodes().size() == 0) {
            return false;
        }

        return true;
    }
}

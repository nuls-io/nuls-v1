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
package io.nuls.consensus.service.tx;

import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.event.notice.EntrustConsensusNotice;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.db.dao.DelegateAccountDataService;
import io.nuls.db.dao.DelegateDataService;
import io.nuls.db.entity.DelegatePo;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class JoinConsensusTxService implements TransactionService<PocJoinConsensusTransaction> {
    private ConsensusCacheManager manager = ConsensusCacheManager.getInstance();
    private DelegateDataService delegateDataService = NulsContext.getServiceBean(DelegateDataService.class);

    @Override
    public void onRollback(PocJoinConsensusTransaction tx) throws NulsException {
        this.manager.delDelegate(tx.getTxData().getExtend().getHash());
        delegateDataService.delete(tx.getTxData().getExtend().getHash());
    }

    @Override
    public void onCommit(PocJoinConsensusTransaction tx) throws NulsException {
        manager.changeDelegateStatus(tx.getTxData().getExtend().getHash(), ConsensusStatusEnum.IN);
        DelegatePo po = new DelegatePo();
        po.setId(tx.getTxData().getExtend().getHash());
        po.setStatus(ConsensusStatusEnum.IN.getCode());
        delegateDataService.updateSelective(po);
        EntrustConsensusNotice notice = new EntrustConsensusNotice();
        notice.setEventBody(tx);
        NulsContext.getServiceBean(EventBroadcaster.class).publishToLocal(notice);
    }

    @Override
    public void onApproval(PocJoinConsensusTransaction tx) throws NulsException {
        Consensus<Delegate> cd = tx.getTxData();
        cd.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
        manager.cacheDelegate(cd);
        DelegatePo po = ConsensusTool.delegateToPojo(cd);
        delegateDataService.save(po);
    }
}

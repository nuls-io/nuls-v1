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
package io.nuls.consensus.poc.service.impl.tx;

import io.nuls.consensus.poc.protocol.constant.ConsensusStatusEnum;
import io.nuls.consensus.poc.protocol.event.notice.EntrustConsensusNotice;
import io.nuls.consensus.poc.protocol.model.Deposit;
import io.nuls.consensus.poc.protocol.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.poc.protocol.utils.ConsensusTool;
import io.nuls.core.exception.NulsException;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.entity.DepositPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.transactional.annotation.PROPAGATION;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.entity.Consensus;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.service.intf.TransactionService;

/**
 * @author Niels
 * @date 2018/1/8
 */
@DbSession(transactional = PROPAGATION.NONE)
public class JoinConsensusTxService implements TransactionService<PocJoinConsensusTransaction> {
//    private ConsensusCacheManager manager = ConsensusCacheManager.getInstance();
    private DepositDataService depositDataService = NulsContext.getServiceBean(DepositDataService.class);
    private AgentDataService accountDataService = NulsContext.getServiceBean(AgentDataService.class);

    @Override
    @DbSession
    public void onRollback(PocJoinConsensusTransaction tx, Block block) throws NulsException {

//        manager.realDeleteDeposit(tx.getTxData().getHexHash());

        DepositPo delPo = new DepositPo();
        delPo.setId(tx.getTxData().getHexHash());
        delPo.setDelHeight(tx.getBlockHeight());
        depositDataService.realDeleteById(delPo);
    }

    @Override
    @DbSession
    public void onCommit(PocJoinConsensusTransaction tx, Block block) throws NulsException {
        Consensus<Deposit> cd = tx.getTxData();
        cd.getExtend().setBlockHeight(tx.getBlockHeight());
        cd.getExtend().setTxHash(tx.getHash().getDigestHex());
        cd.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());

        DepositPo po = ConsensusTool.depositToPojo(cd, tx.getHash().getDigestHex());
        po.setBlockHeight(tx.getBlockHeight());
        po.setTime(tx.getTime());
        depositDataService.save(po);

        EntrustConsensusNotice notice = new EntrustConsensusNotice();
        notice.setEventBody(tx);
        NulsContext.getServiceBean(EventBroadcaster.class).publishToLocal(notice);
    }

    @Override
    public void onApproval(PocJoinConsensusTransaction tx, Block block) {
//        Consensus<Deposit> cd = tx.getTxData();
//        cd.getExtend().setBlockHeight(tx.getBlockHeight());
//        cd.getExtend().setStatus(ConsensusStatusEnum.WAITING.getCode());
//        cd.getExtend().setTxHash(tx.getHash().getDigestHex());
//        manager.putDeposit(cd);
    }
}

/*
 *
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
package io.nuls.consensus.service.tx;

import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.cache.manager.tx.TxCacheManager;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Deposit;
import io.nuls.consensus.entity.tx.CancelDepositTransaction;
import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.db.dao.DepositDataService;
import io.nuls.db.dao.TxAccountRelationDataService;
import io.nuls.db.entity.DepositPo;
import io.nuls.db.entity.TxAccountRelationPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class CancelDepositTxService implements TransactionService<CancelDepositTransaction> {

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private DepositDataService depositDataService = NulsContext.getServiceBean(DepositDataService.class);

    private ConsensusCacheManager manager = ConsensusCacheManager.getInstance();

    private TxAccountRelationDataService relationDataService = NulsContext.getServiceBean(TxAccountRelationDataService.class);


    @Override
    @DbSession
    public void onRollback(CancelDepositTransaction tx) {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        PocJoinConsensusTransaction pjcTx = (PocJoinConsensusTransaction) joinTx;
        Consensus<Deposit> cd = pjcTx.getTxData();
        cd.getExtend().setStatus(ConsensusStatusEnum.IN.getCode());
        DepositPo dpo = new DepositPo();
        dpo.setId(cd.getHexHash());
        dpo.setDelHeight(0L);
        this.depositDataService.updateSelective(dpo);
//        StopConsensusNotice notice = new StopConsensusNotice();
//        notice.setEventBody(tx);
//        NulsContext.getServiceBean(EventBroadcaster.class).publishToLocal(notice);
        this.ledgerService.unlockTxRollback(tx.getTxData().getDigestHex());

        Consensus<Deposit> depositConsensus = manager.getDepositById(cd.getHexHash());
        depositConsensus.setDelHeight(0L);
        manager.putDeposit(depositConsensus);

        Set<String> set = new HashSet<>();
        set.add(cd.getAddress());
        relationDataService.deleteRelation(tx.getHash().getDigestHex(), set);
    }

    @Override
    @DbSession
    public void onCommit(CancelDepositTransaction tx) throws NulsException {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        PocJoinConsensusTransaction pjcTx = (PocJoinConsensusTransaction) joinTx;
        Consensus<Deposit> cd = pjcTx.getTxData();
        DepositPo dpo = new DepositPo();
        dpo.setDelHeight(tx.getBlockHeight());
        dpo.setId(cd.getHexHash());
        this.depositDataService.deleteById(dpo);
        this.ledgerService.unlockTxSave(tx.getTxData().getDigestHex());
        manager.delDeposit(pjcTx.getTxData().getHexHash(), tx.getBlockHeight());

        TxAccountRelationPo po = new TxAccountRelationPo();
        po.setAddress(pjcTx.getTxData().getAddress());
        po.setTxHash(tx.getHash().getDigestHex());
        relationDataService.save(po);

        if (NulsContext.LOCAL_ADDRESS_LIST.contains(pjcTx.getTxData().getAddress())) {
            tx.setMine(true);
        }
    }

    @Override
    @DbSession
    public void onApproval(CancelDepositTransaction tx) {
        Transaction joinTx = ledgerService.getTx(tx.getTxData());
        if (joinTx == null) {
            joinTx = TxCacheManager.TX_CACHE_MANAGER.getTx(tx.getTxData());
        }
        PocJoinConsensusTransaction realTx = (PocJoinConsensusTransaction) joinTx;
        this.ledgerService.unlockTxApprove(tx.getTxData().getDigestHex());
        manager.delDeposit(realTx.getTxData().getHexHash(), tx.getBlockHeight());

    }
}

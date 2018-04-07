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
package io.nuls.consensus.service.tx;

import io.nuls.account.entity.Address;
import io.nuls.consensus.constant.PunishType;
import io.nuls.consensus.entity.YellowPunishData;
import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.PunishLogDataService;
import io.nuls.db.dao.TxAccountRelationDataService;
import io.nuls.db.entity.PunishLogPo;
import io.nuls.db.entity.TxAccountRelationPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.transactional.annotation.PROPAGATION;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Niels
 * @date 2018/1/8
 */
@DbSession(transactional = PROPAGATION.NONE)
public class YellowPunishTxService implements TransactionService<YellowPunishTransaction> {

    private PunishLogDataService punishLogDataService = NulsContext.getServiceBean(PunishLogDataService.class);

    private TxAccountRelationDataService relationDataService = NulsContext.getServiceBean(TxAccountRelationDataService.class);


    @Override
    @DbSession
    public void onRollback(YellowPunishTransaction tx) throws NulsException {
        YellowPunishData data = tx.getTxData();
        this.punishLogDataService.deleteByHeight(data.getHeight());
        String txHash = tx.getHash().getDigestHex();
        Set<String> addressSet = new HashSet<>();
        for (Address address : data.getAddressList()) {
            addressSet.add(address.getBase58());
        }
        relationDataService.deleteRelation(txHash, addressSet);
    }

    @Override
    @DbSession
    public void onCommit(YellowPunishTransaction tx) {
        YellowPunishData data = tx.getTxData();
        String txHash = tx.getHash().getDigestHex();
        Set<String> set = new HashSet<>();
        for (Address address : data.getAddressList()) {
            PunishLogPo plpo = new PunishLogPo();
            plpo.setAddress(address.getBase58());
            plpo.setHeight(data.getHeight());
            plpo.setId(StringUtils.getNewUUID());
            plpo.setTime(tx.getTime());
            Block block = NulsContext.getServiceBean(BlockService.class).getBlock(tx.getBlockHeight());
            BlockRoundData roundData = new BlockRoundData(block.getHeader().getExtend());
            plpo.setRoundIndex(roundData.getRoundIndex());
            plpo.setType(PunishType.YELLOW.getCode());
            punishLogDataService.save(plpo);
            String adrs = address.toString();
            if (!set.add(adrs)) {
                continue;
            }
            TxAccountRelationPo po = new TxAccountRelationPo();
            po.setAddress(adrs);
            po.setTxHash(txHash);
            relationDataService.save(po);

        }
    }

    @Override
    public void onApproval(YellowPunishTransaction tx) {
    }
}

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

import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
import io.nuls.consensus.poc.protocol.tx.entity.RedPunishData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.PunishLogDataService;
import io.nuls.db.entity.PunishLogPo;
import io.nuls.db.transactional.annotation.DbSession;
import io.nuls.db.transactional.annotation.PROPAGATION;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.service.intf.TransactionService;

/**
 * @author Niels
 * @date 2018/1/8
 */
@DbSession(transactional = PROPAGATION.NONE)
public class RedPunishTxService implements TransactionService<RedPunishTransaction> {
    private PunishLogDataService punishLogDataService = NulsContext.getServiceBean(PunishLogDataService.class);

    @Override
    @DbSession
    public void onRollback(RedPunishTransaction tx, Block block) throws NulsException {
        RedPunishData data = tx.getTxData();
        this.punishLogDataService.deleteByHeight(data.getHeight());
    }

    @Override
    @DbSession
    public void onCommit(RedPunishTransaction tx, Block block) throws NulsException {
        RedPunishData data = tx.getTxData();
        PunishLogPo po = new PunishLogPo();
        po.setAddress(data.getAddress());
        po.setHeight(data.getHeight());
        po.setId(StringUtils.getNewUUID());
        po.setTime(tx.getTime());
        po.setType(PunishType.RED.getCode());
        punishLogDataService.save(po);
    }

    @Override
    public void onApproval(RedPunishTransaction tx, Block block) throws NulsException {

    }
}

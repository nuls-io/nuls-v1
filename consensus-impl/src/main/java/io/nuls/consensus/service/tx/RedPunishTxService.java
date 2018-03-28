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

import io.nuls.consensus.constant.PunishType;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.tx.serivce.TransactionService;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.PunishLogDataService;
import io.nuls.db.entity.PunishLogPo;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class RedPunishTxService implements TransactionService<RedPunishTransaction> {
    private PunishLogDataService punishLogDataService = NulsContext.getServiceBean(PunishLogDataService.class);

    @Override
    public void onRollback(RedPunishTransaction tx) throws NulsException {
        RedPunishData data = tx.getTxData();
        this.punishLogDataService.deleteByHeight(data.getHeight());
    }

    @Override
    public void onCommit(RedPunishTransaction tx) throws NulsException {
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
    public void onApproval(RedPunishTransaction tx) throws NulsException {

    }
}

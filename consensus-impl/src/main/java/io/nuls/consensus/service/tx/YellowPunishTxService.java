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
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
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
public class YellowPunishTxService implements TransactionService<YellowPunishTransaction> {

    private PunishLogDataService punishLogDataService = NulsContext.getServiceBean(PunishLogDataService.class);

    @Override
    public void onRollback(YellowPunishTransaction tx) throws NulsException {
        YellowPunishData data = tx.getTxData();
        this.punishLogDataService.deleteByHeight(data.getHeight());
    }

    @Override
    public void onCommit(YellowPunishTransaction tx) throws NulsException {
        YellowPunishData data = tx.getTxData();
        for (Address address : data.getAddressList()) {
            PunishLogPo po = new PunishLogPo();
            po.setAddress(address.getBase58());
            po.setHeight(data.getHeight());
            po.setId(StringUtils.getNewUUID());
            po.setTime(tx.getTime());
            po.setType(PunishType.YELLOW.getCode());
            punishLogDataService.save(po);
        }
    }

    @Override
    public void onApproval(YellowPunishTransaction tx) throws NulsException {
    }
}

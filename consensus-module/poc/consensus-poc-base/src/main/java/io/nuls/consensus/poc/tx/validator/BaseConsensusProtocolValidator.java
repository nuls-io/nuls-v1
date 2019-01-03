/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.consensus.poc.tx.validator;

import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.protocol.constant.PocConsensusProtocolConstant;
import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.NulsData;
import io.nuls.kernel.validate.NulsDataValidator;

import java.util.Arrays;
import java.util.List;

/**
 * @author Niels
 */
public abstract class BaseConsensusProtocolValidator<T extends NulsData> implements NulsDataValidator<T> {

    protected final boolean isDepositOk(Na deposit, CoinData coinData) {
        if(coinData == null || coinData.getTo().size() == 0) {
            return false;
        }
        Coin coin = coinData.getTo().get(0);
        if(!deposit.equals(coin.getNa())) {
            return false;
        }
        if(coin.getLockTime() != PocConsensusProtocolConstant.LOCK_OF_LOCK_TIME) {
            return false;
        }
        return true;
    }

    protected long getRedPunishCount(byte[] address ) {
        List<PunishLogPo> list = PocConsensusContext.getChainManager().getMasterChain().getChain().getRedPunishList();
        if (null == list || list.isEmpty()) {
            return 0;
        }
        long count = 0;
        for (PunishLogPo po : list) {
            if (Arrays.equals(address, po.getAddress())) {
                count++;
            }
        }
        return count;
    }
}

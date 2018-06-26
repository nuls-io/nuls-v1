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

package io.nuls.consensus.poc.tx.validator;

import io.nuls.consensus.poc.config.ConsensusConfig;
import io.nuls.consensus.poc.protocol.constant.PocConsensusErrorCode;
import io.nuls.consensus.poc.protocol.tx.YellowPunishTransaction;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.kernel.constant.SeverityLevelEnum;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.ValidateResult;

import java.util.List;

/**
 * @author Niels
 * @date 2018/5/14
 */
@Component
public class YellowPunishValidator extends BaseConsensusProtocolValidator<YellowPunishTransaction> {
    @Override
    public ValidateResult validate(YellowPunishTransaction data) {
        if (null == data || data.getTxData() == null || data.getTxData().getAddressList() == null || data.getTxData().getAddressList().isEmpty()) {
            return ValidateResult.getFailedResult(this.getClass().getName(), PocConsensusErrorCode.YELLOW_PUNISH_TX_WRONG);
        }
        List<byte[]> list = data.getTxData().getAddressList();
        for (byte[] address : list) {
            if (ConsensusConfig.getSeedNodeStringList().contains(AddressTool.getStringAddressByBytes(address))) {
                return ValidateResult.getFailedResult(this.getClass().getName(), PocConsensusErrorCode.ADDRESS_IS_CONSENSUS_SEED);
            }
        }
        if (data.getCoinData() != null) {
            ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), PocConsensusErrorCode.YELLOW_PUNISH_TX_WRONG);
            result.setLevel(SeverityLevelEnum.FLAGRANT_FOUL);
            return result;
        }
        return ValidateResult.getSuccessResult();
    }
}

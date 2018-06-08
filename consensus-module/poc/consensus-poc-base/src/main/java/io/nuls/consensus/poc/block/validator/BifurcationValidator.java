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

package io.nuls.consensus.poc.block.validator;

import io.nuls.consensus.poc.protocol.constant.PunishReasonEnum;
import io.nuls.consensus.poc.protocol.entity.RedPunishData;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
import io.nuls.consensus.service.ConsensusService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.protocol.model.SmallBlock;
import io.nuls.protocol.service.BlockService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: Niels Wang
 * @date: 2018/6/8
 */
@Component
public class BifurcationValidator implements NulsDataValidator<BlockHeader> {

    @Autowired
    private BlockService blockService;

    @Autowired
    private ConsensusService consensusService;

    @Override
    public ValidateResult validate(BlockHeader header) {
        ValidateResult result = ValidateResult.getSuccessResult();
        if (header.getHeight() > NulsContext.getInstance().getBestHeight()) {
            return result;
        }
        BlockHeader otherBlockHeader = blockService.getBlockHeader(header.getHeight()).getData();
        if (null != otherBlockHeader && !otherBlockHeader.getHash().equals(header.getHash()) && Arrays.equals(otherBlockHeader.getPackingAddress(), header.getPackingAddress())) {
            RedPunishTransaction redPunishTransaction = new RedPunishTransaction();
            RedPunishData redPunishData = new RedPunishData();
            redPunishData.setAddress(header.getPackingAddress());
            try {
                byte[] header1 = header.serialize();
                byte[] header2 = otherBlockHeader.serialize();
                redPunishData.setEvidence(ArraysTool.joinintTogether(header1, header2));
            } catch (Exception e) {
                ValidateResult.getFailedResult(this.getClass().getName(), e.getMessage());
            }
            redPunishData.setReasonCode(PunishReasonEnum.DOUBLE_SPEND.getCode());
            redPunishTransaction.setTxData(redPunishData);
            this.consensusService.newTx(redPunishTransaction);
            return ValidateResult.getFailedResult(this.getClass().getName(), "Bifurcation");
        }

        return result;
    }

}
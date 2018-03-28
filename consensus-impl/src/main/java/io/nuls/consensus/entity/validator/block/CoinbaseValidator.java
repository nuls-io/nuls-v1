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
package io.nuls.consensus.entity.validator.block;

import io.nuls.consensus.entity.block.BlockRoundData;
import io.nuls.consensus.manager.ConsensusManager;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2018/1/11
 */
public class CoinbaseValidator implements NulsDataValidator<Block> {

    private static final CoinbaseValidator INSTANCE = new CoinbaseValidator();

    private ConsensusManager consensusManager = ConsensusManager.getInstance();

    private CoinbaseValidator() {
    }

    public static CoinbaseValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(Block block) {
        if (null == block || block.getHeader() == null || null == block.getTxs() || block.getTxs().isEmpty()) {
            return ValidateResult.getFailedResult(ErrorCode.DATA_FIELD_CHECK_ERROR);
        }
        Transaction tx = block.getTxs().get(0);
        if (tx.getType() != TransactionConstant.TX_TYPE_COIN_BASE) {
            return ValidateResult.getFailedResult("Coinbase transaction order wrong!");
        }

        for (int i = 1; i < block.getTxs().size(); i++) {
            Transaction transaction = block.getTxs().get(i);
            if (transaction.getType() == TransactionConstant.TX_TYPE_COIN_BASE) {
                ValidateResult result = ValidateResult.getFailedResult("Coinbase transaction more than one!");
                result.setLevel(SeverityLevelEnum.FLAGRANT_FOUL);
                return result;
            }
        }

        BlockRoundData blockRound = null;
        try {
            blockRound = new BlockRoundData(block.getHeader().getExtend());
        } catch (NulsException e) {
            Log.error(e);
        }
        if (null == blockRound) {
            return ValidateResult.getFailedResult("Cann't get the round data!");
        }
        if(block.getHeader().getHeight()!=(NulsContext.getInstance().getBestHeight()+1)){
            return ValidateResult.getSuccessResult();
        }
        //todo 金额验证


        return ValidateResult.getSuccessResult();
    }
}

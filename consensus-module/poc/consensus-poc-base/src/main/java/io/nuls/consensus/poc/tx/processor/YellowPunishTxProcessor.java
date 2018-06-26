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

package io.nuls.consensus.poc.tx.processor;

import io.nuls.consensus.poc.model.BlockRoundData;
import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.entity.YellowPunishData;
import io.nuls.consensus.poc.protocol.tx.YellowPunishTransaction;
import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.consensus.poc.storage.service.PunishLogStorageService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.kernel.utils.VarInt;
import io.nuls.kernel.validate.ValidateResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2018/5/14
 */
@Component
public class YellowPunishTxProcessor implements TransactionProcessor<YellowPunishTransaction> {

    @Autowired
    private PunishLogStorageService punishLogStorageService;

    @Override
    public Result onRollback(YellowPunishTransaction tx, Object secondaryData) {
        YellowPunishData punishData = tx.getTxData();
        List<byte[]> deletedList = new ArrayList<>();
        int deleteIndex = 1;
        for (byte[] address : punishData.getAddressList()) {
            boolean result = punishLogStorageService.delete(this.getPoKey(address, PunishType.YELLOW.getCode(), tx.getBlockHeight(), deleteIndex++));
            if (!result) {
                BlockHeader header = (BlockHeader) secondaryData;
                BlockRoundData roundData = new BlockRoundData(header.getExtend());
                int index = 1;
                for (byte[] bytes : deletedList) {
                    PunishLogPo po = new PunishLogPo();
                    po.setAddress(bytes);
                    po.setHeight(tx.getBlockHeight());
                    po.setRoundIndex(roundData.getRoundIndex());
                    po.setTime(tx.getTime());
                    po.setIndex(index++);
                    po.setType(PunishType.YELLOW.getCode());
                    punishLogStorageService.save(po);
                }
                throw new NulsRuntimeException(TransactionErrorCode.ROLLBACK_TRANSACTION_FAILED);
            } else {
                deletedList.add(address);
            }
        }
        return Result.getSuccess();
    }

    @Override
    public Result onCommit(YellowPunishTransaction tx, Object secondaryData) {
        YellowPunishData punishData = tx.getTxData();
        BlockHeader header = (BlockHeader) secondaryData;
        BlockRoundData roundData = new BlockRoundData(header.getExtend());
        List<PunishLogPo> savedList = new ArrayList<>();
        int index = 1;
        for (byte[] address : punishData.getAddressList()) {
            PunishLogPo po = new PunishLogPo();
            po.setAddress(address);
            po.setHeight(tx.getBlockHeight());
            po.setRoundIndex(roundData.getRoundIndex());
            po.setTime(tx.getTime());
            po.setIndex(index++);
            po.setType(PunishType.YELLOW.getCode());
            boolean result = punishLogStorageService.save(po);
            if (!result) {
                for (PunishLogPo punishLogPo : savedList) {
                    punishLogStorageService.delete(getPoKey(punishLogPo.getAddress(), PunishType.YELLOW.getCode(), punishLogPo.getHeight(), punishLogPo.getIndex()));
                }
                throw new NulsRuntimeException(TransactionErrorCode.ROLLBACK_TRANSACTION_FAILED);
            } else {
                savedList.add(po);
            }
        }
        return Result.getSuccess();
    }

    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        return ValidateResult.getSuccessResult();
    }

    /**
     * 获取固定格式的key
     */
    private byte[] getPoKey(byte[] address, byte type, long blockHeight, int index) {
        return ArraysTool.concatenate(address, new byte[]{type}, SerializeUtils.uint64ToByteArray(blockHeight), new VarInt(index).encode());
    }
}

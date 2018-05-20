package io.nuls.consensus.poc.tx.processor;

import io.nuls.consensus.poc.model.BlockRoundData;
import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.entity.YellowPunishData;
import io.nuls.consensus.poc.protocol.tx.YellowPunishTransaction;
import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.consensus.poc.storage.service.PunishLogStorageService;
import io.nuls.core.tools.array.ArraysTool;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.processor.TransactionProcessor;
import io.nuls.kernel.utils.SerializeUtils;
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
    private PunishLogStorageService storageService;

    @Override
    public Result onRollback(YellowPunishTransaction tx, Object secondaryData) {
        YellowPunishData punishData = tx.getTxData();
        List<byte[]> deletedList = new ArrayList<>();
        for (byte[] address : punishData.getAddressList()) {
            boolean result = storageService.delete(this.getPoKey(address, (byte) PunishType.YELLOW.getCode(), tx.getBlockHeight()));
            if (!result) {
                BlockHeader header = (BlockHeader) secondaryData;
                BlockRoundData roundData = new BlockRoundData(header.getExtend());
                for (byte[] bytes : deletedList) {
                    PunishLogPo po = new PunishLogPo();
                    po.setAddress(bytes);
                    po.setHeight(tx.getBlockHeight());
                    po.setRoundIndex(roundData.getRoundIndex());
                    po.setTime(tx.getTime());
                    po.setType(PunishType.YELLOW.getCode());
                    this.storageService.save(po);
                }
                throw new NulsRuntimeException(KernelErrorCode.FAILED, "rollback tx failed!");
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
        List<byte[]> savedList = new ArrayList<>();
        for (byte[] address : punishData.getAddressList()) {
            PunishLogPo po = new PunishLogPo();
            po.setAddress(address);
            po.setHeight(tx.getBlockHeight());
            po.setRoundIndex(roundData.getRoundIndex());
            po.setTime(tx.getTime());
            po.setType(PunishType.YELLOW.getCode());
            boolean result = storageService.save(po);
            if (!result) {
                for (byte[] bytes : savedList) {
                    this.storageService.delete(getPoKey(bytes, (byte) PunishType.YELLOW.getCode(), header.getHeight()));
                }
                throw new NulsRuntimeException(KernelErrorCode.FAILED, "rollback tx failed!");
            } else {
                savedList.add(address);
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
    private byte[] getPoKey(byte[] address, byte type, long blockHeight) {
        return ArraysTool.joinintTogether(address, new byte[]{type}, SerializeUtils.uint64ToByteArray(blockHeight));
    }
}

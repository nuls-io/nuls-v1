package io.nuls.consensus.poc.tx.processor;

import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.model.BlockRoundData;
import io.nuls.consensus.poc.protocol.constant.PunishType;
import io.nuls.consensus.poc.protocol.entity.RedPunishData;
import io.nuls.consensus.poc.protocol.tx.RedPunishTransaction;
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
public class RedPunishTxProcessor implements TransactionProcessor<RedPunishTransaction> {

    @Autowired
    private PunishLogStorageService storageService;

    @Override
    public Result onRollback(RedPunishTransaction tx, Object secondaryData) {
        RedPunishData punishData = tx.getTxData();
        List<byte[]> deletedList = new ArrayList<>();
        byte[] address = punishData.getAddress();
        boolean result = storageService.delete(this.getPoKey(address, PunishType.RED.getCode(), tx.getBlockHeight()));
        if (!result) {
            BlockHeader header = (BlockHeader) secondaryData;
            BlockRoundData roundData = new BlockRoundData(header.getExtend());
            for (byte[] bytes : deletedList) {
                PunishLogPo po = new PunishLogPo();
                po.setAddress(bytes);
                po.setHeight(tx.getBlockHeight());
                po.setRoundIndex(roundData.getRoundIndex());
                po.setTime(tx.getTime());
                po.setType(PunishType.RED.getCode());
                this.storageService.save(po);
            }
            throw new NulsRuntimeException(KernelErrorCode.FAILED, "rollback tx failed!");
        } else {
            deletedList.add(address);
        }
        return Result.getSuccess();
    }

    @Override
    public Result onCommit(RedPunishTransaction tx, Object secondaryData) {
        RedPunishData punishData = tx.getTxData();
        BlockHeader header = (BlockHeader) secondaryData;
        BlockRoundData roundData = new BlockRoundData(header.getExtend());
        List<byte[]> savedList = new ArrayList<>();
        PunishLogPo po = new PunishLogPo();
        po.setAddress(punishData.getAddress());
        po.setHeight(tx.getBlockHeight());
        po.setRoundIndex(roundData.getRoundIndex());
        po.setTime(tx.getTime());
        po.setType(PunishType.RED.getCode());
        boolean result = storageService.save(po);
        if (!result) {
            for (byte[] bytes : savedList) {
                this.storageService.delete(getPoKey(bytes, PunishType.RED.getCode(), header.getHeight()));
            }
            throw new NulsRuntimeException(KernelErrorCode.FAILED, "rollback tx failed!");
        } else {
            savedList.add(punishData.getAddress());
        }

        PocConsensusContext.getChainManager().getMasterChain().getChain().getAgentList();


        return Result.getSuccess();
    }

    /**
     * 获取固定格式的key
     */
    private byte[] getPoKey(byte[] address, byte type, long blockHeight) {
        return ArraysTool.joinintTogether(address, new byte[]{type}, SerializeUtils.uint64ToByteArray(blockHeight));
    }

    @Override
    public ValidateResult conflictDetect(List<Transaction> txList) {
        return ValidateResult.getSuccessResult();
    }
}

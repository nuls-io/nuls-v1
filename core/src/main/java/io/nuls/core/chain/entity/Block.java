package io.nuls.core.chain.entity;

import io.nuls.core.chain.manager.BlockValidatorManager;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.event.EventManager;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.NulsDataValidator;

import java.io.IOException;
import java.util.List;

/**
 * @author win10
 * @date 2017/10/30
 */
public class Block extends BaseNulsData {

    private BlockHeader header;
    /**
     * the count of the agents the current round
     */
    private int countOfRound;
    private long roundStartTime;
    private int orderInRound;

    private List<Transaction> txs;

    public Block() {
        initValidators();
    }

    private void initValidators() {
        List<NulsDataValidator> list = BlockValidatorManager.getValidators();
        for (NulsDataValidator<Block> validator : list) {
            this.registerValidator(validator);
        }
    }

    @Override
    public int size() {
        int size = header.size();
        size += VarInt.sizeOf(countOfRound);
        size += VarInt.sizeOf(roundStartTime);
        size += VarInt.sizeOf(orderInRound);
        for (Transaction tx : txs) {
            size += tx.size();
        }
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        header.serializeToStream(stream);
        stream.writeVarInt(countOfRound);
        stream.writeVarInt(roundStartTime);
        stream.writeVarInt(orderInRound);
        for (Transaction tx : txs) {
            stream.write(tx.serialize());
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        header = new BlockHeader();
        header.parse(byteBuffer);
        countOfRound = (int) byteBuffer.readVarInt();
        roundStartTime = byteBuffer.readVarInt();
        orderInRound = (int) byteBuffer.readVarInt();
        try {
            txs = TransactionManager.getInstances(byteBuffer);
        } catch (Exception e) {
            throw new NulsRuntimeException(ErrorCode.PARSE_OBJECT_ERROR);
        }
    }

    public List<Transaction> getTxs() {
        return txs;
    }

    public void setTxs(List<Transaction> txs) {
        this.txs = txs;
    }

    public BlockHeader getHeader() {
        return header;
    }

    public void setHeader(BlockHeader header) {
        this.header = header;
    }

    public int getCountOfRound() {
        return countOfRound;
    }

    public void setCountOfRound(int countOfRound) {
        this.countOfRound = countOfRound;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public int getOrderInRound() {
        return orderInRound;
    }

    public void setOrderInRound(int orderInRound) {
        this.orderInRound = orderInRound;
    }
}

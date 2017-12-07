package io.nuls.core.chain.entity;

import io.nuls.core.chain.manager.BlockValidatorManager;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.validate.NulsDataValidator;

import java.io.IOException;
import java.util.List;

/**
 * @author vivi
 * @date 2017/10/30
 */
public class BlockHeader extends BaseNulsData {

    protected NulsDigestData hash;
    protected NulsDigestData preHash;

    protected NulsDigestData merkleHash;

    protected long time;

    protected long height;

    protected long txCount;
    /**
     * the count of the agents the current round
     */
    protected int countOfRound;
    protected long roundStartTime;
    protected int orderInRound;

    private NulsSignData sign;


    public BlockHeader(long height, long time) {
        this.height = height;
        this.time = time;
        initValidators();
    }

    private void initValidators() {
        List<NulsDataValidator> list = BlockValidatorManager.getValidators();
        for (NulsDataValidator<Block> validator : list) {
            this.registerValidator(validator);
        }
    }

    public BlockHeader(long height, long time, NulsDigestData preHash) {
        this(height, time);
        this.preHash = preHash;
    }

    @Override
    public int size() {
        //todo
        return 0;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        //todo

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        //todo
    }

    public NulsDigestData getHash() throws IOException {
        if (hash == null) {
            //todo
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            try {
//                Utils.uint32ToByteStreamLE(version.getVersion(), stream);
//                stream.write(preHash.getBytes());
////                stream.write(merkleHash.getBytes());
//                Utils.int64ToByteStreamLE(time, stream);
//                Utils.uint32ToByteStreamLE(height, stream);
//                stream.write(new VarInt(periodCount).encode());
//                stream.write(new VarInt(timePeriod).encode());
//                Utils.uint32ToByteStreamLE(periodStartTime, stream);
//                //交易数量
//                stream.write(new VarInt(txCount).encode());
//                hash = Sha256Hash.twiceOf(stream.toByteArray());
//            } finally {
//                stream.close();
//            }
        }
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

    public NulsDigestData getPreHash() {
        return preHash;
    }

    public void setPreHash(NulsDigestData preHash) {
        this.preHash = preHash;
    }

    public NulsDigestData getMerkleHash() {
        return merkleHash;
    }

    public void setMerkleHash(NulsDigestData merkleHash) {
        this.merkleHash = merkleHash;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getTxCount() {
        return txCount;
    }

    public void setTxCount(long txCount) {
        this.txCount = txCount;
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

    public NulsSignData getSign() {
        return sign;
    }

    public void setSign(NulsSignData sign) {
        this.sign = sign;
    }
}

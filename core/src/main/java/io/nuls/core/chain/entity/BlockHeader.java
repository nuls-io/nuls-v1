package io.nuls.core.chain.entity;

import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.crypto.script.Script;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vivi
 * @date 2017/10/30
 */
public class BlockHeader extends BaseNulsData {

    public static final short OWN_MAIN_VERSION = 1;
    public static final short OWN_SUB_VERSION = 0001;

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

    public BlockHeader() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
    }

    public BlockHeader(long height, long time) {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.height = height;
        this.time = time;
    }

    public BlockHeader(long height, long time, NulsDigestData preHash) {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.height = height;
        this.time = time;
        this.preHash = preHash;
    }

    @Override
    protected int dataSize() {
        //todo
        return 0;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        //todo

    }

    @Override
    protected void parseObject(NulsByteBuffer byteBuffer) {
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

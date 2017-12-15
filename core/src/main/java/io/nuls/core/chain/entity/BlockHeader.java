package io.nuls.core.chain.entity;

import io.nuls.core.chain.manager.BlockHeaderValidatorManager;
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

    private NulsDigestData hash;
    private NulsDigestData preHash;
    private NulsDigestData merkleHash;

    private long time;

    private long height;

    private long txCount;

    private List<byte[]> txHashList;

    private NulsSignData sign;

    public BlockHeader() {
        initValidators();
    }

    private void initValidators() {
        List<NulsDataValidator> list = BlockHeaderValidatorManager.getValidators();
        for (NulsDataValidator validator : list) {
            this.registerValidator(validator);
        }
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

    public NulsDigestData getHash() {
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

    public NulsSignData getSign() {
        return sign;
    }

    public void setSign(NulsSignData sign) {
        this.sign = sign;
    }

    public List<byte[]> getTxHashList() {
        return txHashList;
    }

    public void setTxHashList(List<byte[]> txHashList) {
        this.txHashList = txHashList;
    }
}

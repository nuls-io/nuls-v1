package io.nuls.rpc.entity;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.db.entity.BlockHeaderPo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vive
 */
public class BlockDto {

    private String hash;

    private String preHash;

    private String merkleHash;

    private Long time;

    private Long height;

    private Long txCount;

    private String packingAddress;

    private String scriptSign;

    private Long roundIndex;

    private Integer consensusMemberCount;

    private Long roundStartTime;

    private Integer packingIndexOfRound;

    private Long reward;

    private Long fee;

    private Long confirmCount;

    private int size;

    private List<TransactionDto> txList;

    public BlockDto(Block block, long reward, long fee) throws IOException {
        this(block.getHeader(), reward, fee);

        this.txList = new ArrayList<>();
        for (Transaction tx : block.getTxs()) {
            this.txList.add(new TransactionDto(tx));
        }
    }

    public BlockDto(BlockHeader header, long reward, long fee) throws IOException {
        long bestBlockHeight = NulsContext.getInstance().getBestBlock().getHeader().getHeight();
        this.hash = header.getHash().getDigestHex();
        this.preHash = header.getPreHash().getDigestHex();
        this.merkleHash = header.getMerkleHash().getDigestHex();
        this.time = header.getTime();
        this.height = header.getHeight();
        this.txCount = header.getTxCount();
        this.packingAddress = header.getPackingAddress();
        this.scriptSign = Hex.encode(header.getScriptSig().serialize());
        this.reward = reward;
        this.fee = fee;
        this.confirmCount = bestBlockHeight - this.height;
        this.size = header.getSize();
        NulsByteBuffer byteBuffer = new NulsByteBuffer(header.getExtend());
        try {
            this.roundIndex = byteBuffer.readVarInt();
        } catch (NulsException e) {
            Log.error(e);
        }
        try {
            this.consensusMemberCount = (int) byteBuffer.readVarInt();
        } catch (NulsException e) {
            Log.error(e);
        }
        try {
            this.roundStartTime = byteBuffer.readVarInt();
        } catch (NulsException e) {
            Log.error(e);
        }
        try {
            this.packingIndexOfRound = (int) byteBuffer.readVarInt();
        } catch (NulsException e) {
            Log.error(e);
        }
    }

    public BlockDto(BlockHeaderPo header, long reward, long fee) {
        long bestBlockHeight = NulsContext.getInstance().getBestBlock().getHeader().getHeight();
        this.hash = header.getHash();
        this.preHash = header.getPreHash();
        this.merkleHash = header.getMerkleHash();
        this.time = header.getCreateTime();
        this.height = header.getHeight();
        this.txCount = header.getTxCount();
        this.packingAddress = header.getConsensusAddress();
        this.scriptSign = Hex.encode(header.getScriptSig());
        this.reward = reward;
        this.fee = fee;
        this.size = header.getSize();
        this.confirmCount = bestBlockHeight - this.height;
        NulsByteBuffer byteBuffer = new NulsByteBuffer(header.getExtend());
        try {
            this.roundIndex = byteBuffer.readVarInt();
        } catch (NulsException e) {
            Log.error(e);
        }
        try {
            this.consensusMemberCount = (int) byteBuffer.readVarInt();
        } catch (NulsException e) {
            Log.error(e);
        }
        try {
            this.roundStartTime = byteBuffer.readVarInt();
        } catch (NulsException e) {
            Log.error(e);
        }
        try {
            this.packingIndexOfRound = (int) byteBuffer.readVarInt();
        } catch (NulsException e) {
            Log.error(e);
        }
    }


    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreHash() {
        return preHash;
    }

    public void setPreHash(String preHash) {
        this.preHash = preHash;
    }

    public String getMerkleHash() {
        return merkleHash;
    }

    public void setMerkleHash(String merkleHash) {
        this.merkleHash = merkleHash;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public Long getTxCount() {
        return txCount;
    }

    public void setTxCount(Long txCount) {
        this.txCount = txCount;
    }

    public String getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(String packingAddress) {
        this.packingAddress = packingAddress;
    }

    public String getScriptSig() {
        return scriptSign;
    }

    public void setScriptSig(String scriptSig) {
        this.scriptSign = scriptSig;
    }

    public Long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(Long roundIndex) {
        this.roundIndex = roundIndex;
    }

    public Integer getConsensusMemberCount() {
        return consensusMemberCount;
    }

    public void setConsensusMemberCount(Integer consensusMemberCount) {
        this.consensusMemberCount = consensusMemberCount;
    }

    public Long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(Long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public Integer getPackingIndexOfRound() {
        return packingIndexOfRound;
    }

    public void setPackingIndexOfRound(Integer packingIndexOfRound) {
        this.packingIndexOfRound = packingIndexOfRound;
    }

    public List<TransactionDto> getTxList() {
        return txList;
    }

    public void setTxList(List<TransactionDto> txList) {
        this.txList = txList;
    }

    public Long getReward() {
        return reward;
    }

    public void setReward(Long reward) {
        this.reward = reward;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public Long getConfirmCount() {
        return confirmCount;
    }

    public void setConfirmCount(Long confirmCount) {
        this.confirmCount = confirmCount;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}

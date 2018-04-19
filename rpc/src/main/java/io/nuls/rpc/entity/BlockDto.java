package io.nuls.rpc.entity;

import io.nuls.account.entity.Address;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.log.Log;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.BlockHeader;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.utils.io.NulsByteBuffer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vive
 */
@ApiModel(value = "blockJSON 区块信息(包含区块头信息, 交易信息), 只返回对应的部分数据")
public class BlockDto {

    @ApiModelProperty(name = "hash", value = "区块的hash值")
    private String hash;

    @ApiModelProperty(name = "preHash", value = "上一个区块的hash值")
    private String preHash;

    @ApiModelProperty(name = "merkleHash", value = "梅克尔hash")
    private String merkleHash;

    @ApiModelProperty(name = "time", value = "区块生成时间")
    private Long time;

    @ApiModelProperty(name = "height", value = "区块高度")
    private Long height;

    @ApiModelProperty(name = "txCount", value = "区块打包交易数量")
    private Long txCount;

    @ApiModelProperty(name = "packingAddress", value = "打包地址")
    private String packingAddress;

    @ApiModelProperty(name = "scriptSign", value = "签名Hex.encode(byte[])")
    private String scriptSign;

    @ApiModelProperty(name = "roundIndex", value = "共识轮次")
    private Long roundIndex;

    @ApiModelProperty(name = "consensusMemberCount", value = "参与共识成员数量")
    private Integer consensusMemberCount;

    @ApiModelProperty(name = "roundStartTime", value = "当前共识轮开始时间")
    private Long roundStartTime;

    @ApiModelProperty(name = "packingIndexOfRound", value = "当前轮次打包出块的名次")
    private Integer packingIndexOfRound;

    @ApiModelProperty(name = "reward", value = "共识奖励")
    private Long reward;

    @ApiModelProperty(name = "fee", value = "获取的打包手续费")
    private Long fee;

    @ApiModelProperty(name = "confirmCount", value = "确认次数")
    private Long confirmCount;

    @ApiModelProperty(name = "size", value = "大小")
    private int size;

    @ApiModelProperty(name = "txList", value = "transactionsJSON")
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
        this.packingAddress = Address.fromHashs(header.getPackingAddress()).getBase58();
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

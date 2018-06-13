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

package io.nuls.protocol.sdk.model;

import io.nuls.sdk.utils.StringUtils;
import java.util.List;
import java.util.Map;

/**
 * @author: Niels Wang
 * @date: 2018/5/9
 */
public class BlockDto {

    /**
     * 区块的hash值
     */
    private String hash;
    /**
     * 上一个区块的hash值
     */
    private String preHash;
    /**
     * 梅克尔hash
     */
    private String merkleHash;
    /**
     * 区块生成时间
     */
    private Long time;
    /**
     * 区块高度
     */
    private Long height;
    /**
     * 区块打包交易数量
     */
    private Long txCount;
    /**
     * 打包地址
     */
    private String packingAddress;
    /**
     * 签名Hex.encode(byte[])
     */
    private String scriptSig;
    /**
     * 共识轮次
     */
    private Long roundIndex;
    /**
     * 参与共识成员数量
     */
    private Integer consensusMemberCount;
    /**
     * 当前共识轮开始时间
     */
    private Long roundStartTime;
    /**
     * 当前轮次打包出块的名次
     */
    private Integer packingIndexOfRound;
    /**
     * 确认次数
     */
    private Long confirmCount;

    /**
     *共识奖励
     */
    private Long reward;

    /**
     *获取的打包手续费
     */
    private Long fee;

    /**
     * 大小
     */
    private int size;

    /**
     * transactionsJSON
     */
    private List<TransactionDto> txList;

    public BlockDto(Map<String, Object> map) {
        this.hash = (String) map.get("hash");
        this.preHash = (String) map.get("preHash");
        this.merkleHash = (String) map.get("merkleHash");
        this.time = StringUtils.parseLong(map.get("time"));
        this.height = StringUtils.parseLong(map.get("height"));
        this.txCount = StringUtils.parseLong(map.get("height"));
        this.packingAddress = (String) map.get("packingAddress");
        this.scriptSig = (String) map.get("scriptSig");
        this.roundIndex = StringUtils.parseLong(map.get("roundIndex"));
        this.consensusMemberCount = (Integer) map.get("consensusMemberCount");
        this.roundStartTime = StringUtils.parseLong(map.get("roundStartTime"));
        this.packingIndexOfRound = (Integer) map.get("packingIndexOfRound");
        this.confirmCount = StringUtils.parseLong(map.get("confirmCount"));
        this.reward = StringUtils.parseLong(map.get("reward"));
        this.fee = StringUtils.parseLong(map.get("fee"));
        this.size = (int) map.get("consensusMemberCount");
        this.txList = (List<TransactionDto>)map.get("txList");
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
        return scriptSig;
    }

    public void setScriptSig(String scriptSig) {
        this.scriptSig = scriptSig;
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

    public Long getConfirmCount() {
        return confirmCount;
    }

    public void setConfirmCount(Long confirmCount) {
        this.confirmCount = confirmCount;
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<TransactionDto> getTxList() {
        return txList;
    }

    public void setTxList(List<TransactionDto> txList) {
        this.txList = txList;
    }
}
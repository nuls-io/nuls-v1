/*
 *
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

package io.nuls.rpc.sdk.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private String sign;

    private Long roundIndex;
    private Integer consensusMemberCount;
    private Long roundStartTime;
    private Integer packingIndexOfRound;
    private ArrayList<TransactionDto> txList;

    public BlockDto(Map<String, Object> map, boolean all) {
        this.hash = (String) map.get("hash");
        this.preHash = (String) map.get("preHash");
        this.merkleHash = (String) map.get("merkleHash");
        this.time = Long.parseLong(""+ map.get("time"));
        this.height = Long.parseLong(""+  map.get("height"));
        this.txCount = Long.parseLong(""+ map.get("txCount"));
        this.packingAddress = (String) map.get("packingAddress");
        this.sign = (String) map.get("sign");
        roundIndex = Long.parseLong(""+  map.get("roundIndex"));
        consensusMemberCount = (Integer) map.get("consensusMemberCount");
        roundStartTime = Long.parseLong(""+  map.get("roundStartTime"));
        packingIndexOfRound = (Integer) map.get("packingIndexOfRound");
        if (all) {
            this.txList = new ArrayList<>();
            for (Map<String, Object> tx : (List<Map<String, Object>>) map.get("txList")) {
                this.txList.add(new TransactionDto(tx));
            }
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

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
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
}

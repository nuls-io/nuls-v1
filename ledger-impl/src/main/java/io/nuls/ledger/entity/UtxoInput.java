/**
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
 */
package io.nuls.ledger.entity;

import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.ledger.service.impl.LedgerCacheService;
import io.nuls.ledger.util.UtxoTransferTool;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.BaseNulsData;
import io.nuls.protocol.model.NulsDigestData;
import io.nuls.protocol.utils.io.NulsByteBuffer;
import io.nuls.protocol.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author win10
 * @date 2017/10/30
 */
public class UtxoInput extends BaseNulsData {

    private NulsDigestData txHash;

    private int index;

    private NulsDigestData fromHash;

    private int fromIndex;

    //private byte[] scriptSig;

    private UtxoOutput from;

    // key = fromHash + "-" + fromIndex, a key that will not be serialized, only used for caching
    private String key;


    public UtxoInput() {

    }

    public UtxoInput(NulsDigestData txHash) {
        this();
        this.txHash = txHash;
    }

    public UtxoInput(NulsDigestData txHash, UtxoOutput output) {
        this();
        this.txHash = txHash;
        this.from = output;
    }

    @Override
    public int size() {
        int size = 0;
        size += VarInt.sizeOf(index);
        size += Utils.sizeOfNulsData(fromHash);
        size += VarInt.sizeOf(fromIndex);
        //size += Utils.sizeOfBytes(scriptSig);
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(index);
        stream.writeNulsData(fromHash);
        stream.writeVarInt(fromIndex);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        index = (int) byteBuffer.readVarInt();
        fromHash = byteBuffer.readNulsData(new NulsDigestData());
        fromIndex = (int) byteBuffer.readVarInt();

        LedgerCacheService ledgerCacheService = LedgerCacheService.getInstance();
        UtxoOutput output = ledgerCacheService.getUtxo(this.getKey());
        if (output == null) {
            UtxoOutputDataService utxoOutputDataService = NulsContext.getServiceBean(UtxoOutputDataService.class);
            Map<String, Object> map = new HashMap<>();
            map.put("txHash", this.fromHash.getDigestHex());
            map.put("outIndex", this.fromIndex);
            UtxoOutputPo outputPo = utxoOutputDataService.get(map);
            if (outputPo != null) {
                output = UtxoTransferTool.toOutput(outputPo);
            }
        }
        from = output;
    }

    public NulsDigestData getTxHash() {
        return txHash;
    }

    public void setTxHash(NulsDigestData txHash) {
        this.txHash = txHash;
    }

    public UtxoOutput getFrom() {
        if (from == null && fromHash != null) {
            from = LedgerCacheService.getInstance().getUtxo(this.getKey());
        }
        return from;
    }

    public void setFrom(UtxoOutput from) {
        this.from = from;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
    }

    public String getKey() {
        if (StringUtils.isBlank(key)) {
            key = fromHash.getDigestHex() + "-" + fromIndex;
        }
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public NulsDigestData getFromHash() {
        return fromHash;
    }

    public void setFromHash(NulsDigestData fromHash) {
        this.fromHash = fromHash;
    }
}

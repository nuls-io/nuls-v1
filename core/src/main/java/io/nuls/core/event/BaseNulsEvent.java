package io.nuls.core.event;

import io.nuls.core.bus.BaseBusData;
import io.nuls.core.bus.BusDataHeader;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class BaseNulsEvent<T extends BaseNulsData> extends BaseBusData<T> {
    private NulsDigestData hash;
    private NulsSignData sign;

    public BaseNulsEvent(short moduleId, short eventType, byte[] extend) {
        super(moduleId, eventType, extend);
    }

    public BaseNulsEvent(short moduleId, short eventType) {
        this(moduleId, eventType, null);
    }

    @Override
    public final int size() {
        int size = super.size();
        size += hash.size();
        size += sign.size();
        return size;
    }

    @Override
    public final void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        super.serializeToStream(stream);
        this.hash.serializeToStream(stream);
        this.sign.serializeToStream(stream);
    }

    @Override
    public final void parse(NulsByteBuffer byteBuffer) {
        super.parse(byteBuffer);
        this.hash = new NulsDigestData();
        this.hash.parse(byteBuffer);
        this.sign = new NulsSignData();
        this.sign.parse(byteBuffer);

    }

    public NulsDigestData getHash() {
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

    public NulsSignData getSign() {
        return sign;
    }

    public void setSign(NulsSignData sign) {
        this.sign = sign;
    }
}

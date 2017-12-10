package io.nuls.network.message;

import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author vivi
 * @date 2017/12/10.
 */
public class ReplyEvent extends BaseNulsEvent<BasicTypeData<byte[]>> {

    public ReplyEvent() {
        super(NulsConstant.MODULE_ID_NETWORK, (short) 1);
    }

    @Override
    protected BasicTypeData<byte[]> parseEventBody(NulsByteBuffer byteBuffer) {
        BasicTypeData<byte[]> typeData = new BasicTypeData(byteBuffer);
        return typeData;
    }
}

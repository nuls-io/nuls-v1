package io.nuls.network.message;

import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.notice.BaseNulsNotice;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author vivi
 * @date 2017/12/10.
 */
public class ReplyNotice extends BaseNulsNotice<BasicTypeData<byte[]>> {

    public ReplyNotice() {
        super(NulsConstant.MODULE_ID_NETWORK, (short) 1);
    }

    @Override
    protected BasicTypeData<byte[]> parseEventBody(NulsByteBuffer byteBuffer) {
        BasicTypeData<byte[]> typeData = new BasicTypeData(byteBuffer);
        return typeData;
    }

    @Override
    public Object copy() {
        ReplyNotice event = new ReplyNotice();
        BasicTypeData<byte[]> typeData = new BasicTypeData(this.getEventBody().getVal());
        event.setEventBody(typeData);
        return event;
    }
}

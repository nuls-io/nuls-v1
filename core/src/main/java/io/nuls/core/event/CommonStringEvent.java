package io.nuls.core.event;

import io.nuls.core.chain.entity.basic.NulsStringData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.str.StringUtils;

/**
 * @author Niels
 * @date 2018/1/22
 */
public class CommonStringEvent extends BaseEvent<NulsStringData> {

    public CommonStringEvent() {
        super(NulsConstant.MODULE_ID_MICROKERNEL, (short) 1);
    }

    @Override
    protected NulsStringData parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {

        return byteBuffer.readNulsData(new NulsStringData());
    }

    @Override
    public NoticeData getNotice() {
        return null;
    }

    public void setMessage(String message) {
        if (StringUtils.isBlank(message)) {
            return;
        }
        NulsStringData val = new NulsStringData(message);
        this.setEventBody(val);
    }

    public String getMessage() {
        NulsStringData val = this.getEventBody();
        if (null == val) {
            return null;
        }
        return val.getVal();
    }
}

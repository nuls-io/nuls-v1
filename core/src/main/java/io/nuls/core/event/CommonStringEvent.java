package io.nuls.core.event;

import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.str.StringUtils;

/**
 * @author Niels
 * @date 2018/1/22
 */
public class CommonStringEvent extends BaseEvent<BasicTypeData<String>> {

    public CommonStringEvent() {
        super(NulsConstant.MODULE_ID_MICROKERNEL, (short) 1);
    }

    @Override
    protected BasicTypeData<String> parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {

        return byteBuffer.readNulsData(new BasicTypeData<String>());
    }

    @Override
    public NoticeData getNotice() {
        return null;
    }

    public void setMessage(String message) {
        if (StringUtils.isBlank(message)) {
            return;
        }
        BasicTypeData<String> val = new BasicTypeData<>(message);
        this.setEventBody(val);
    }

    public String getMessage() {
        BasicTypeData<String> val = this.getEventBody();
        if (null == val) {
            return null;
        }
        return val.getVal();
    }
}

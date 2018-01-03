package io.nuls.account.entity.event;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.entity.tx.AliasTransaction;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

/**
 * @author vivi
 * @date 2017/12/18.
 */
public class AliasEvent extends BaseNetworkEvent<AliasTransaction> {
    public AliasEvent() {
        super(NulsConstant.MODULE_ID_ACCOUNT, AccountConstant.EVENT_TYPE_ALIAS);
    }

    @Override
    public Object copy() {
        try {
            return this.clone();
        } catch (CloneNotSupportedException e) {
            Log.error(e);
            return null;
        }
    }

    @Override
    protected AliasTransaction parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new AliasTransaction());
    }
}

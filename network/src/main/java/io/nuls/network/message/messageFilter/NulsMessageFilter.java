package io.nuls.network.message.messageFilter;

import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;

public interface NulsMessageFilter {

    NulsMessageHeader filterHeader(byte[] bytes);

    NulsMessage filterMessage(byte[] bytes);

}

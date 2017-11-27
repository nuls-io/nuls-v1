package io.nuls.network.message.messageFilter;

import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public interface NulsMessageFilter {

    NulsMessageHeader filterHeader(byte[] bytes);

    NulsMessage filterMessage(byte[] bytes);

    public void addMagicNum(Integer magicNum);

    public void removeMagicNum(Integer magicNum);

}

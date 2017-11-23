package io.nuls.network.filter.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.message.messageFilter.NulsMessageFilter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class DefaultMessageFilter implements NulsMessageFilter {


    private Set<Long> magicSet = new LinkedHashSet<>();

    private static final DefaultMessageFilter MESSAGE_FILTER = new DefaultMessageFilter();

    private DefaultMessageFilter() {

    }

    public static DefaultMessageFilter getInstance() {
        return MESSAGE_FILTER;
    }

    @Override
    public NulsMessageHeader filterHeader(byte[] bytes) {
        NulsMessageHeader messageHeader = new NulsMessageHeader();
        messageHeader.parse(new NulsByteBuffer(bytes));
        if (!magicSet.contains(messageHeader.getMagicNumber())) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        return messageHeader;
    }

    @Override
    public NulsMessage filterMessage(byte[] bytes) {
        return null;
    }

    public void addMagicNum(long magicNum) {
        magicSet.add(magicNum);
    }

    public void removeMagicNum(long magicNum) {
        magicSet.remove(magicNum);
    }

}

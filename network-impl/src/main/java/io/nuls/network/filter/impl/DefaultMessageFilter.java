package io.nuls.network.filter.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.core.utils.io.ByteBuffer;
import io.nuls.network.message.messageFilter.NulsMessageFilter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by vivi on 2017/11/20.
 */
public class DefaultMessageFilter implements NulsMessageFilter {


    private Set<Long> magicSet = new LinkedHashSet<>();

    private static final DefaultMessageFilter messageFilter = new DefaultMessageFilter();

    public static DefaultMessageFilter getInstance() {
        return messageFilter;
    }

    @Override
    public NulsMessageHeader filterHeader(byte[] bytes) {
        NulsMessageHeader messageHeader = new NulsMessageHeader();
        messageHeader.parse(new ByteBuffer(bytes));
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

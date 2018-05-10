package io.nuls.network.message.filter.impl;

import io.nuls.network.message.filter.NulsMessageFilter;
import io.nuls.protocol.message.base.BaseMessage;
import io.nuls.protocol.message.base.MessageHeader;

import java.util.HashSet;
import java.util.Set;

public class MagicNumberFilter implements NulsMessageFilter{

    private Set<Integer> magicSet = new HashSet<>();

    private static MagicNumberFilter instance = new MagicNumberFilter();

    private MagicNumberFilter() {

    }

    public static MagicNumberFilter getInstance() {
        return instance;
    }

    @Override
    public boolean filter(BaseMessage message) {
        MessageHeader header = message.getHeader();
        return magicSet.contains(header.getMagicNumber());
    }

    public void addMagicNum(Integer magicNum) {
        magicSet.add(magicNum);
    }

    public void removeMagicNum(Integer magicNum) {
        magicSet.remove(magicNum);
    }
}

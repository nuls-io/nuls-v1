package io.nuls.network.filter.impl;

import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.core.utils.log.Log;
import io.nuls.network.message.filter.NulsMessageFilter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class DefaultMessageFilter implements NulsMessageFilter {

    private Set<Integer> magicSet = new LinkedHashSet<>();

    private static DefaultMessageFilter instance = new DefaultMessageFilter();

    private DefaultMessageFilter() {

    }

    public static DefaultMessageFilter getInstance() {
        return instance;
    }

    @Override
    public boolean filter(NulsMessage message) {
        try {
            NulsMessageHeader header = message.getHeader();
            if (!magicSet.contains(header.getMagicNumber())) {
                return false;
            }
            message.verify();
            return true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    public void addMagicNum(Integer magicNum) {
        magicSet.add(magicNum);
    }

    public void removeMagicNum(Integer magicNum) {
        magicSet.remove(magicNum);
    }


}

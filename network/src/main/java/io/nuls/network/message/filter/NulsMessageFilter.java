package io.nuls.network.message.filter;

import io.nuls.core.mesasge.NulsMessage;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public interface NulsMessageFilter {

    boolean filter(NulsMessage message);
}

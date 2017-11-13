package io.nuls.core.event;

import io.nuls.core.chain.entity.NulsData;

/**
 * Created by Niels on 2017/11/7.
 * nuls.io
 */
public abstract class NulsEvent extends NulsData {

    public NulsEvent(NulsEventHeader header) {
        this.header = header;
    }

    private NulsEventHeader header;

    public NulsEventHeader getHeader() {
        return header;
    }

    public void setHeader(NulsEventHeader header) {
        this.header = header;
    }
}

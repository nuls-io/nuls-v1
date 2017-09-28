package io.nuls;


import io.nuls.message.NulsMessage;

public interface IBroadcaster {
    void Boradcast(NulsMessage msg);
}

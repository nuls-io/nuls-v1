package io.nuls;


import io.nuls.message.InchainMessage;

public interface IBroadcaster {
    void Boradcast(InchainMessage msg);
}

package io.nuls.network.intf;

public interface IPeersManager {
    void start();
    void shutdown();
    void restart();
    String info();
}

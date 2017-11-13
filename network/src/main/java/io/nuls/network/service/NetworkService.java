package io.nuls.network.service;

import io.nuls.network.module.NetworkModule;

/**
 * Created by v.chou on 2017/10/25.
 */
public abstract class NetworkService {

    protected NetworkModule networkModule;

    public abstract void start();

    public abstract void shutdown();
}

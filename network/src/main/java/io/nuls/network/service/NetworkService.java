package io.nuls.network.service;

/**
 * Created by win10 on 2017/10/25.
 */
public abstract class NetworkService {
    long getBestBlockHeight(){
        return 0;
    }

    String getBestBlockHash(){
        return "";
    }

}

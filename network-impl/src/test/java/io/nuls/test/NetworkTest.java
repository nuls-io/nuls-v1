package io.nuls.test;

import io.nuls.network.module.impl.NetworkModuleImpl;

/**
 * Created by win10 on 2017/11/10.
 */
public class NetworkTest {


    public static void main(String[] args) {
        try{
            NetworkModuleImpl module = new NetworkModuleImpl();
            module.start();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}

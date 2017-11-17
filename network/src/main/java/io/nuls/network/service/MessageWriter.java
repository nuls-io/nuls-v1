package io.nuls.network.service;

import java.io.IOException;

/**
 * Created by vivi on 2017/11/16.
 */
public interface MessageWriter {

    void write(byte[] message) throws IOException;

    void closeConnection();
}

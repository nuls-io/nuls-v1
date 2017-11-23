package io.nuls.network.service;

import java.io.IOException;

/**
 * @author vivi
 * @date 2017/11/21
 */
public interface MessageWriter {

    void write(byte[] message) throws IOException;

    void closeConnection();
}

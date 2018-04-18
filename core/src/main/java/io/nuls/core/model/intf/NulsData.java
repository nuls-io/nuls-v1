package io.nuls.core.model.intf;

import io.nuls.core.exception.NulsException;

import java.io.IOException;

/**
 * @author: Niels Wang
 * @date: 2018/4/17
 */
public interface NulsData {

    int size();

    /**
     * serialize important field
     */
    byte[] serialize() throws IOException;

    void parse(byte[] bytes) throws NulsException;
}

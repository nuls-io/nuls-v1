package io.nuls.db;

import io.nuls.exception.NulsException;

/**
 * Created by win10 on 2017/9/30.
 */
public class DBException extends NulsException{
    public DBException(String message) {
        super(message);
    }
}

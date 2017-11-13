package io.nuls.account.entity;

import io.nuls.core.chain.entity.KeyValue;
import io.nuls.core.utils.json.JSONUtils;
import io.nuls.core.utils.log.Log;

import java.io.UnsupportedEncodingException;

public class AccountKeyValue extends KeyValue {


    public AccountKeyValue(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public AccountKeyValue(String code, String name, byte[] value) {
        this.code = code;
        this.name = name;
        this.value = value;
    }

    public AccountKeyValue(String code, String name, String value) {
        this.code = code;
        this.name = name;
        try {
            this.value = value.getBytes(CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public AccountKeyValue(byte[] content) {
        super(content);
    }

    public String toString() {
        try {
            return JSONUtils.obj2json(this);
        } catch (Exception e) {
            Log.error(e);
            return super.toString();
        }
    }

}

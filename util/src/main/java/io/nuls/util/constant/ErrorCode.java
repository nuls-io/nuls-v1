package io.nuls.util.constant;

import io.nuls.util.cfg.I18nUtils;

/**
 * Created by Niels on 2017/9/27.
 * nuls.io
 */
public enum ErrorCode {

    SUCCESS(0, 10000),
    FAILED(1, 10001),
    FILENOTFOUND(2, 10002),
    NULLPARAMETER(3, 10003),

    LANGUAGECANNOTSETTONULL(100,10100),
    UNKOWN(88, 99999);

    private final int msg;
    private final int code;

    private ErrorCode(int code, int msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getMsg() {
        return I18nUtils.get(msg);
    }

    public int getCode() {
        return code;
    }
}

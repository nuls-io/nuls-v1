package io.nuls.core.crypto.script;

/**
 * Created by win10 on 2017/10/30.
 */
public class ScriptException extends RuntimeException{

    public ScriptException(String msg) {
        super(msg);
    }

    public ScriptException(String msg, Exception e) {
        super(msg, e);
    }
}

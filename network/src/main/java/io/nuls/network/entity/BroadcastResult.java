package io.nuls.network.entity;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class BroadcastResult {
    public boolean success = false;
    private String msg = "";

    public boolean isSuccess() {
        return success;
    }

    public BroadcastResult(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "";
    }

}

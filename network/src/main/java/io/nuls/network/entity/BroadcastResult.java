package io.nuls.network.entity;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class BroadcastResult {
    public boolean isSuccess = false;
    private String msg = "";

    public boolean isSuccess() {
        return isSuccess;
    }

    public BroadcastResult(boolean isSuccess, String msg) {
        this.isSuccess = isSuccess;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "";
    }

}

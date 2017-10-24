package io.nuls.network.entity;


public class BroadcastResult{
    public boolean isSuccess = false;
    private String msg = "";

    public boolean isSuccess(){
        return isSuccess;
    }

    public BroadcastResult(boolean isSuccess,String msg){
        this.isSuccess = isSuccess;
        this.msg = msg;
    }

    public String toString(){
        return "";
    }

}

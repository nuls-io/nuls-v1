package io.nuls.rpc.entity;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.utils.json.JSONUtils;
import io.nuls.core.utils.log.Log;

/**
 *
 * @author Niels
 * @date 2017/9/27
 */
public class RpcResult {

    private String code;
    private String msg;
    private Object data;

    public RpcResult() {
    }

    public RpcResult(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public RpcResult(String code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public RpcResult(ErrorCode ec) {
        this.code = ec.getCode()+"";
        this.msg = ec.getMsg();
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Object getData() {
        return data;
    }

    public RpcResult setCode(String code) {
        this.code = code;return this;
    }

    public RpcResult setMsg(String msg) {
        this.msg = msg;return this;
    }

    public RpcResult setData(Object data) {
        this.data = data;
        return this;
    }

    public static RpcResult getSuccess(){
        return new RpcResult(ErrorCode.SUCCESS);
    }

    public static RpcResult getFailed(){
        return new RpcResult(ErrorCode.FAILED);
    }
    public static RpcResult getFailed(ErrorCode errorCode){
        return new RpcResult(errorCode);
    }
    @Override
    public String toString(){
        try {
            return JSONUtils.obj2json(this);
        } catch (Exception e) {
            Log.error(e);
        }
        return null;
    }
}

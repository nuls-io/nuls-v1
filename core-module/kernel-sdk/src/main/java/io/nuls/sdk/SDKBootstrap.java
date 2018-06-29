package io.nuls.sdk;

import io.nuls.sdk.constant.RpcConstant;
import io.nuls.sdk.utils.RestFulUtils;
import io.nuls.sdk.utils.StringUtils;

/**
 * @author: Charlie
 */
public class SDKBootstrap {

    public static void main(String[] args) {
        sdkStart();
        sdkStart("127.0.0.1", "8001");
    }

    public static void sdkStart() {
        sdkStart(null, null);
    }

    public static void sdkStart(String ip, String port) {
        if (StringUtils.isBlank(ip) || StringUtils.isBlank(port)) {
            RestFulUtils.getInstance().setServerUri("http://" + RpcConstant.DEFAULT_IP + ":" + RpcConstant.DEFAULT_PORT + RpcConstant.PREFIX);
        } else {
            RestFulUtils.getInstance().setServerUri("http://" + ip + ":" + port + RpcConstant.PREFIX);
        }
    }
}

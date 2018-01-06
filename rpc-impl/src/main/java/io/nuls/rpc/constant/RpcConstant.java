package io.nuls.rpc.constant;

/**
 *
 * @author Niels
 * @date 2017/10/9
 */
public interface RpcConstant {
    //version
    int RPC_MODULE_VERSION = 1111;
    //Minimum version supported
    int MINIMUM_VERSION_SUPPORTED = 0;

    String PACKAGES = "io.nuls.rpc.resources.impl";
    int DEFAULT_PORT = 8001;
    String DEFAULT_IP = "0.0.0.0";
    String DEFAULT_URL = "nuls";


    String CFG_RPC_SECTION = "rpc";
    String CFG_RPC_SERVER_IP = "server.ip";
    String CFG_RPC_SERVER_PORT ="server.port" ;
    String CFG_RPC_SERVER_URL = "server.url";
    String CFG_RPC_REQUEST_WHITE_SHEET="request.white.sheet";

    String WHITE_SHEET_SPLIT = ",";
}

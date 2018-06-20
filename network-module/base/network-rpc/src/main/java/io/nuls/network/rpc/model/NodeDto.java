package io.nuls.network.rpc.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "nodeInfoJSON")
public class NodeDto {

    @ApiModelProperty(name = "ip", value = "节点ip")
    private String ip;
    @ApiModelProperty(name = "port", value = "节点端口")
    private int port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

package io.nuls.network.test;

public class Node {

    private String id;

    private String ip;

    private int port;

    public String getId() {
        return ip + ":" + port;
    }

    public void setId(String id) {
        this.id = id;
    }

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

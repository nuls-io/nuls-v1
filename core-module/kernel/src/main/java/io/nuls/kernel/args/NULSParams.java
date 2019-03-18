package io.nuls.kernel.args;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Niels
 */
public enum NULSParams {
    BOOTSTRAP;

    private Map<String, String> paramsMap = new HashMap<>();

    private String rpcIp;

    private Integer rpcPort;

    private String dataDir;

    public void init(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String param = args[i];
            if (null != param && param.startsWith("--")) {
                String value = null;
                if (args.length > (i + 1)) {
                    value = args[i + 1];
                    i += 1;
                }
                paramsMap.put(param.substring(2), value);
            }
        }
        rpcIp = paramsMap.get("serverIp");
        String port = paramsMap.get("port");
        if (null != port) {
            this.rpcPort = Integer.parseInt(port);
        }
        this.dataDir = paramsMap.get("dataDir");
    }

    public String getRpcIp() {
        return rpcIp;
    }

    public void setRpcIp(String rpcIp) {
        this.rpcIp = rpcIp;
    }

    public Integer getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(Integer rpcPort) {
        this.rpcPort = rpcPort;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getProperty(String key) {
        return paramsMap.get(key);
    }
}

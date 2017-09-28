package io.nuls.rpcserver.impl.services;

import io.nuls.rpcserver.resources.NulsResourceConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import io.nuls.rpcserver.intf.RpcServerService;
import io.nuls.util.log.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Niels on 2017/9/25.
 * nuls.io
 */
@Service
public class RpcServerServiceImpl implements RpcServerService {


    @Value("${server.ip}")
    private String serverIp;
    @Value("${server.port}")
    private String serverPort;

    private HttpServer httpServer;


    @Override
    public void init() {
        URI serverURI = UriBuilder.fromUri("http://" + serverIp + "/").port(Integer.parseInt(serverPort)).build();
        final Map<String, Object> initParams = new HashMap<>();
//        initParams.put("jersey.config.server.provider.packages", packages);
        initParams.put("load-on-startup", "1");
        NulsResourceConfig rc = new NulsResourceConfig();
        rc.addProperties(initParams);
        httpServer = GrizzlyHttpServerFactory.createHttpServer(serverURI, rc);
        Log.info("http restFul server is started!");
    }

    @Override
    public void shutdown() {
        httpServer.shutdown();
    }

    @Override
    public boolean isStarted() {
        return this.httpServer.isStarted();
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

}

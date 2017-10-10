package io.nuls.rpcserver.impl.services;

import io.nuls.global.NulsContext;
import io.nuls.rpcserver.constant.RpcConstant;
import io.nuls.rpcserver.intf.IRpcServerService;
import io.nuls.rpcserver.resources.NulsResourceConfig;
import io.nuls.util.log.Log;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Niels on 2017/9/25.
 * nuls.io
 */
public class RpcServerServiceImpl implements IRpcServerService {

    private RpcServerServiceImpl() {
        NulsContext.getInstance().regService(this);
    }

    private static final RpcServerServiceImpl service = new RpcServerServiceImpl();

    public static IRpcServerService getInstance() {
        return service;
    }

    private HttpServer httpServer;

    @Override
    public void startServer(String ip, int port, String moduleUrl) {
        URI serverURI = UriBuilder.fromUri("http://" + ip + "/" + moduleUrl).port(port).build();
        final Map<String, Object> initParams = new HashMap<>();
        initParams.put("jersey.config.server.provider.packages", RpcConstant.PACKAGES);
        initParams.put("load-on-startup", "1");
        NulsResourceConfig rc = new NulsResourceConfig();
        rc.addProperties(initParams);
        httpServer = GrizzlyHttpServerFactory.createHttpServer(serverURI, rc);
        Log.info("http restFul server is started!url is " + serverURI.toString());
    }

    @Override
    public void shutdown() {
        httpServer.shutdown();
    }

    @Override
    public boolean isStarted() {
        if(null==this.httpServer){
            return false;
        }
        return this.httpServer.isStarted();
    }

}

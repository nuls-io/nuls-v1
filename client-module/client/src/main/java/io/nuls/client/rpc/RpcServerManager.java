/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.client.rpc;

import io.nuls.client.rpc.config.NulsResourceConfig;
import io.nuls.core.tools.log.Log;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Niels Wang
 * @date: 2018/5/10
 */
public class RpcServerManager {

    private static final RpcServerManager INSTANCE = new RpcServerManager();

    private HttpServer httpServer;

    private RpcServerManager() {
    }

    public static RpcServerManager getInstance() {
        return INSTANCE;
    }

    public void startServer(String ip, int port) {
        URI serverURI = UriBuilder.fromUri("http://" + ip).port(port).build();
        final Map<String, Object> initParams = new HashMap<>();
//        initParams.put("jersey.config.server.provider.packages", RpcConstant.PACKAGES);
        initParams.put("load-on-startup", "1");
        NulsResourceConfig rc = new NulsResourceConfig();
        rc.addProperties(initParams);
        httpServer = GrizzlyHttpServerFactory.createHttpServer(serverURI, rc);
        try {
            httpServer.start();
            ClassLoader loader = this.getClass().getClassLoader();
            CLStaticHttpHandler docsHandler = new CLStaticHttpHandler(loader, "swagger-ui/");
            docsHandler.setFileCacheEnabled(false);
            ServerConfiguration cfg = httpServer.getServerConfiguration();
            cfg.addHttpHandler(docsHandler, "/docs/");
        } catch (IOException e) {
            Log.error(e);
        }
        Log.info("http restFul server is started!url is " + serverURI.toString());
    }

    public void shutdown() {
        httpServer.shutdown();
    }

    public boolean isStarted() {
        if (null == this.httpServer) {
            return false;
        }
        return this.httpServer.isStarted();
    }
}

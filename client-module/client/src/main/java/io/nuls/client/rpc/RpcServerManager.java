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

import io.nuls.core.tools.log.Log;
import org.glassfish.grizzly.http.server.*;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.utils.Charsets;
import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.ServletRegistration;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * @author: Niels Wang
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
        // Create test web application context.
        WebappContext webappContext = new WebappContext("NULS-RPC-SERVER", "/api");

        ServletRegistration servletRegistration = webappContext.addServlet("jersey-servlet", ServletContainer.class);
        servletRegistration.setInitParameter("javax.ws.rs.Application", "io.nuls.client.rpc.config.NulsResourceConfig");
        servletRegistration.addMapping("/api/*");

        httpServer = new HttpServer();
        NetworkListener listener = new NetworkListener("grizzly2", ip, port);
        TCPNIOTransport transport = listener.getTransport();
        ThreadPoolConfig workerPool = ThreadPoolConfig.defaultConfig()
                .setCorePoolSize(4)
                .setMaxPoolSize(4)
                .setQueueLimit(1000)
                .setThreadFactory((new ThreadFactoryBuilder()).setNameFormat("grizzly-http-server-%d").build());
        transport.configureBlocking(false);
        transport.setSelectorRunnersCount(2);
        transport.setWorkerThreadPoolConfig(workerPool);
        transport.setIOStrategy(WorkerThreadIOStrategy.getInstance());
        transport.setTcpNoDelay(true);
        listener.setSecure(false);
        httpServer.addListener(listener);

        ServerConfiguration config = httpServer.getServerConfiguration();
        config.setDefaultQueryEncoding(Charsets.UTF8_CHARSET);

        webappContext.deploy(httpServer);

        try {
            ClassLoader loader = this.getClass().getClassLoader();

            addSwagerUi(loader);
            addClientUi(loader);

            httpServer.start();
            Log.info("http restFul server is started!url is " + serverURI.toString());
        } catch (IOException e) {
            Log.error(e);
            httpServer.shutdownNow();
        }
    }

    private void addClientUi(ClassLoader loader) {
        CLStaticHttpHandler docsHandler = new CLStaticHttpHandler(loader, "client-web/");
        docsHandler.setFileCacheEnabled(true);
        ServerConfiguration cfg = httpServer.getServerConfiguration();
        cfg.addHttpHandler(docsHandler, "/");
    }

    private void addSwagerUi(ClassLoader loader) {
        CLStaticHttpHandler docsHandler = new CLStaticHttpHandler(loader, "swagger-ui/");
        docsHandler.setFileCacheEnabled(false);
        ServerConfiguration cfg = httpServer.getServerConfiguration();
        cfg.addHttpHandler(docsHandler, "/docs/");
    }

    public void shutdown() {
        Map<HttpHandler, HttpHandlerRegistration[]> mapping = httpServer.getServerConfiguration().getHttpHandlersWithMapping();
        for (HttpHandler handler : mapping.keySet()) {
            handler.destroy();
        }
        httpServer.shutdown();
    }

    public boolean isStarted() {
        if (null == this.httpServer) {
            return false;
        }
        return this.httpServer.isStarted();
    }
}

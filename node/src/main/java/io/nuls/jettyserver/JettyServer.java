/**
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
 */
package io.nuls.jettyserver;

import io.nuls.core.utils.log.Log;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Created by daviyang35 on 2017/11/14.
 */
public class JettyServer {

    private static final String DESCRIPTOR_PATH = "/WEB-INF/web.xml";

    public static void init() {

        Server server = new Server(9999);
        // when JVM quit,shutdown jetty server
        server.setStopAtShutdown(true);

        String rootPath = null;
        try {
            rootPath = JettyServer.class.getResource("/").toURI().getPath();
        } catch (URISyntaxException e) {
            Log.error(e);
        }


        WebAppContext context = null;
        File targetFile = new File(rootPath + "index.html");
        if (targetFile.exists()) {
            Log.warn("use rootPath:" + rootPath);
            context = new WebAppContext(rootPath, "/");
        } else {
            targetFile = new File(rootPath + "../webapp/WEB-INF/web.xml");
            if (targetFile.exists()) {
                Log.warn("use rootPath:" + rootPath + "../webapp/");
                context = new WebAppContext(rootPath + "../webapp/", "/");
            }
        }

        assert context != null : "Jetty webapp directory can't found!";

        // for security reason,block network request
        context.setVirtualHosts(new String[]{"127.0.0.1"});
        context.setDescriptor(DESCRIPTOR_PATH);
        context.setParentLoaderPriority(true);
        context.setConfigurationDiscovered(true);
        server.setHandler(context);

        server.addLifeCycleListener(new LifeCycle.Listener() {
            @Override
            public void lifeCycleStarting(LifeCycle lifeCycle) {
                Log.info("Jetty Server Starting");
            }

            @Override
            public void lifeCycleStarted(LifeCycle lifeCycle) {
                Log.info("Jetty Server Started");
            }

            @Override
            public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {
                Log.info("Jetty Server Failure");
            }

            @Override
            public void lifeCycleStopping(LifeCycle lifeCycle) {
                Log.info("Jetty Server Stopping");
            }

            @Override
            public void lifeCycleStopped(LifeCycle lifeCycle) {
                Log.info("Jetty Server Stopped");
            }
        });

        try {
            server.start();
        } catch (Exception e) {
            Log.error("start Jetty Server Failure");
            Log.error(e);
        }
    }
}

package io.nuls.rpcserver.resources;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.nuls.global.NulsContext;
import io.nuls.rpcserver.aop.RpcServerFilter;
import io.nuls.util.log.Log;
import javafx.scene.shape.Path;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Created by Niels on 2017/9/28.
 * nuls.io
 */
public class NulsResourceConfig extends ResourceConfig {

    public NulsResourceConfig() {
        register(RpcServerFilter.class);
        register(JacksonJsonProvider.class);
        initResources(NulsContext.getApplicationContext());
    }

    private void initResources(ApplicationContext context) {
        Map<String, Object> map = context.getBeansWithAnnotation(javax.ws.rs.Path.class);
        for (Object obj : map.values()) {
            register(obj);
            Log.debug("loading:" + obj.getClass().getName());
        }
//        if (context.getParent() != null) {
//            initResources(context.getParent());
//        }
    }
}

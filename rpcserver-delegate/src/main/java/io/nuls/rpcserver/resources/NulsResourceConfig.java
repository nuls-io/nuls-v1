package io.nuls.rpcserver.resources;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.nuls.global.NulsContext;
import io.nuls.rpcserver.aop.RpcServerFilter;
import io.nuls.util.log.Log;
import org.glassfish.jersey.server.ResourceConfig;

import java.lang.annotation.Annotation;

/**
 * Created by Niels on 2017/9/28.
 * nuls.io
 */
public class NulsResourceConfig extends ResourceConfig {

    public NulsResourceConfig(){
        register(RpcServerFilter.class);
        register(JacksonJsonProvider.class);
        String names[] = NulsContext.getApplicationContext().getBeanDefinitionNames();
        for (String name : names)
        {
            Object obj = NulsContext.getApplicationContext().getBean(name);
            Annotation[] ann = obj.getClass().getDeclaredAnnotations();
            for (Annotation a : ann)
            {
                if (a.annotationType().getName().equals(javax.ws.rs.Path.class.getName()))
                {
                    register(obj);
                    Log.debug("loading:" + obj.getClass().getName());
                    break;
                }
            }
        }
        register(NulsContext.getApplicationContext().getBean(TestResouce.class));
    }
}

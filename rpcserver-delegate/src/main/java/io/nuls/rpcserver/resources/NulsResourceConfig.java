package io.nuls.rpcserver.resources;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.nuls.global.NulsContext;
import io.nuls.rpcserver.aop.RpcServerFilter;
import io.nuls.util.log.Log;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.Map;

/**
 * Created by Niels on 2017/9/28.
 * nuls.io
 */
public class NulsResourceConfig extends ResourceConfig {

    public NulsResourceConfig() {
        register(RpcServerFilter.class);
        register(JacksonJsonProvider.class);
    }

}

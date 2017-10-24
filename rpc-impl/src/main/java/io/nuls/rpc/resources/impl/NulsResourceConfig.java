package io.nuls.rpc.resources.impl;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.nuls.rpc.aop.RpcServerFilter;
import org.glassfish.jersey.server.ResourceConfig;

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

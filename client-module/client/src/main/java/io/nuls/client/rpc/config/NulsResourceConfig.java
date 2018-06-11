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
package io.nuls.client.rpc.config;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.nuls.client.rpc.filter.RpcServerFilter;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.lite.core.SpringLiteContext;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.Path;
import java.util.Collection;

/**
 * @author Niels
 * @date 2017/9/28
 */
public class NulsResourceConfig extends ResourceConfig {

    public NulsResourceConfig() {
        register(io.swagger.jaxrs.listing.ApiListingResource.class);
        register(io.swagger.jaxrs.listing.AcceptHeaderApiListingResource.class);
        register(NulsSwaggerSerializers.class);
        register(MultiPartFeature.class);
        register(RpcServerFilter.class);
        register(JacksonJsonProvider.class);

        Collection<Object> list = SpringLiteContext.getAllBeanList();
        for (Object object : list) {
            if (object.getClass().getAnnotation(Path.class) != null) {
                Log.debug("register restFul resource:{}", object.getClass());
                register(object);
            }
        }
    }

}

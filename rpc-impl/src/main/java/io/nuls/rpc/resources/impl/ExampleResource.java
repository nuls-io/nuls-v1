/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.rpc.resources.impl;

import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.rpc.entity.RpcResult;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Niels
 * @date 2017/9/25
 *
 */
@Path("/")
public class ExampleResource {

    public ExampleResource() {
        Log.debug("haha");
    }

    @GET
    @Produces("application/json")
    public RpcResult getMessage0() {
        return RpcResult.getSuccess().setData("hello world");
    }

    @GET
    @Path("/hello/{key1}")
    @Produces("application/json")
    public String getMessage(@PathParam("key1") String key1          ,@QueryParam("key") String key) {
        return RpcResult.getSuccess().setData("hello world222222222222222222,"+key1+","+key).toString();
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getMessage2(@QueryParam("key") String key) {
        AssertUtil.canNotEmpty(key,"测试抛出异常");
        return RpcResult.getSuccess().setData("hello world3333333333333333333333");
    }
}

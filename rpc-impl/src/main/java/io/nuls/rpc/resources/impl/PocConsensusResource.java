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

import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.rpc.entity.RpcResult;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/consensus")
public class PocConsensusResource {
    private NulsContext context = NulsContext.getInstance();
    private ConsensusService consensusService = context.getService(ConsensusService.class);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getInfo(@QueryParam("address") String address) {
        AssertUtil.canNotEmpty(address, ErrorCode.NULL_PARAMETER);
        RpcResult result = RpcResult.getSuccess();
        ConsensusStatusInfo status = consensusService.getConsensusInfo(address);
        result.setData(status);
        return result;
    }


    @GET
    @Path("/condition")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getCondition() {
        return RpcResult.getSuccess();
    }


    @POST
    @Path("/in")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult in(@FormParam("address") String address, @FormParam("password") String password) {
        return RpcResult.getSuccess();
    }


    @POST
    @Path("/out")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult out(@FormParam("address") String address, @FormParam("password") String password) {
        return RpcResult.getSuccess();
    }

}

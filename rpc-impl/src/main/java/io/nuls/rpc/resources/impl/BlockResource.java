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

import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.rpc.entity.RpcResult;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/block")
public class BlockResource  {

    private BlockService blockService = NulsContext.getInstance().getService(BlockService.class);

    @GET
    @Path("/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    
    public RpcResult loadBlock(@PathParam("hash") String hash) {
        RpcResult result = RpcResult.getSuccess();
        result.setData(blockService.getBlock(hash));
        return result;
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    
    public RpcResult getBlockCount() {
        RpcResult result = RpcResult.getSuccess();
        result.setData(blockService.getLocalHeight() + 1);
        return result;
    }

    @GET
    @Path("/bestheight")
    @Produces(MediaType.APPLICATION_JSON)
    
    public RpcResult getBestBlockHeiht() {
        RpcResult result = RpcResult.getSuccess();
        result.setData(blockService.getLocalHeight());
        return result;
    }

    @GET
    @Path("/besthash")
    @Produces(MediaType.APPLICATION_JSON)
    
    public RpcResult getBestBlockHash() {
        RpcResult result = RpcResult.getSuccess();
        result.setData(blockService.getLocalBestBlock().getHeader().getHash());
        return result;
    }

    @GET
    @Path("/height/{height}/hash")
    @Produces(MediaType.APPLICATION_JSON)
    
    public RpcResult getHashByHeight(@PathParam("height") Integer height) {
        RpcResult result = RpcResult.getSuccess();
        Block block = blockService.getBlock(height);
        if (block != null) {
            result.setData(block.getHeader().getHash());
        }
        return result;
    }

    @GET
    @Path("/height/{height}/header")
    @Produces(MediaType.APPLICATION_JSON)
    
    public RpcResult getHeaderByHeight(@PathParam("height") Integer height) {
        RpcResult result = RpcResult.getSuccess();
        Block block = blockService.getBlock(height);
        if (block != null) {
            result.setData(block.getHeader());
        }
        return result;
    }

    @GET
    @Path("/{hash}/header")
    @Produces(MediaType.APPLICATION_JSON)
    
    public RpcResult getHeader(@PathParam("hash") String hash) {
        RpcResult result = RpcResult.getSuccess();
        Block block = blockService.getBlock(hash);
        if (block != null) {
            result.setData(block.getHeader());
        }
        return result;
    }

    @GET
    @Path("/height/{height}")
    @Produces(MediaType.APPLICATION_JSON)
    
    public RpcResult getBlock(@PathParam("height") Integer height) {
        RpcResult result = RpcResult.getSuccess();
        Block block = blockService.getBlock(height);
        result.setData(block);
        return result;
    }
}

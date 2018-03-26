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
package io.nuls.rpc.resources.impl;

import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.rpc.entity.BlockDto;
import io.nuls.rpc.entity.RpcResult;
import io.swagger.annotations.Api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/block")
@Api(value = "/browse", description = "Block")
public class BlockResource {

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);

    public BlockResource() {
    }

    @GET
    @Path("/header/height/{height}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getHeaderByHeight(@PathParam("height") Integer height) throws NulsException, IOException {
        RpcResult result = RpcResult.getSuccess();
        BlockHeader blockHeader = blockService.getBlockHeader(height);
        if (blockHeader == null) {
            return RpcResult.getFailed(ErrorCode.DATA_NOT_FOUND);
        }
        long reward = ledgerService.getBlockReward(blockHeader.getHeight());
        long fee = ledgerService.getBlockFee(blockHeader.getHeight());
        result.setData(new BlockDto(blockHeader, reward, fee));
        return result;
    }

    @GET
    @Path("/header/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getHeader(@PathParam("hash") String hash) throws NulsException, IOException {
        RpcResult result = RpcResult.getSuccess();
        BlockHeader blockHeader = blockService.getBlockHeader(hash);
        if (blockHeader == null) {
            return RpcResult.getFailed(ErrorCode.DATA_NOT_FOUND);
        }
        long reward = ledgerService.getBlockReward(blockHeader.getHeight());
        long fee = ledgerService.getBlockFee(blockHeader.getHeight());
        result.setData(new BlockDto(blockHeader, reward, fee));
        return result;
    }

    @GET
    @Path("/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult loadBlock(@PathParam("hash") String hash) {
        RpcResult result;
        if (!StringUtils.validHash(hash)) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        Block block = blockService.getBlock(hash);
        if (block == null) {
            result = RpcResult.getFailed(ErrorCode.DATA_NOT_FOUND);
        } else {
            long reward = ledgerService.getBlockReward(block.getHeader().getHeight());
            long fee = ledgerService.getBlockFee(block.getHeader().getHeight());
            result = RpcResult.getSuccess();
            try {
                result.setData(new BlockDto(block, reward, fee));
            } catch (IOException e) {
                //todo
                e.printStackTrace();
            }
        }
        return result;
    }

    @GET
    @Path("/height/{height}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getBlock(@PathParam("height") Long height) {
        RpcResult result;
        if (height < 0) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }

        Block block = blockService.getBlock(height);
        if (block == null) {
            result = RpcResult.getFailed(ErrorCode.DATA_NOT_FOUND);
        } else {
            result = RpcResult.getSuccess();
            long reward = ledgerService.getBlockReward(block.getHeader().getHeight());
            long fee = ledgerService.getBlockFee(block.getHeader().getHeight());
            try {
                result.setData(new BlockDto(block, reward, fee));
            } catch (IOException e) {
                //todo
                e.printStackTrace();
            }
        }
        return result;
    }

    @GET
    @Path("/newest")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getBestBlockHash() throws IOException {
        RpcResult result = RpcResult.getSuccess();
        result.setData(new BlockDto(blockService.getLocalBestBlock().getHeader(), 0, 0));
        return result;
    }

    @GET
    @Path("/list/address")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getBlocksByAddress(@QueryParam("address") String address,
                                        @QueryParam("type") int type,
                                        @QueryParam("pageNumber") int pageNumber,
                                        @QueryParam("pageSize") int pageSize) {

        if (type < 1 || type > 2 || pageNumber < 0 || pageSize < 0 || !StringUtils.validAddress(address)) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if (pageNumber == 0) {
            pageNumber = 1;
        }
        if (pageSize == 0) {
            pageSize = 10;
        }
        Page<BlockHeaderPo> page = blockService.getBlockHeaderList(address, type, pageNumber, pageSize);
        Page<BlockDto> dtoPage = new Page<>(page);
        List<BlockDto> dtoList = new ArrayList<>();

        long reward = 0;
        for (BlockHeaderPo header : page.getList()) {
            reward = ledgerService.getBlockReward(header.getHeight());
            dtoList.add(new BlockDto(header, reward, 0));
        }
        dtoPage.setList(dtoList);
        return RpcResult.getSuccess().setData(dtoPage);
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getBlocks(@QueryParam("pageNumber") int pageNumber,
                               @QueryParam("pageSize") int pageSize) {
        if (pageNumber < 0 || pageSize < 0) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        if (pageNumber == 0) {
            pageNumber = 1;
        }
        if (pageSize == 0) {
            pageSize = 10;
        }
        Page<BlockHeaderPo> page = blockService.getBlockHeaderList(pageNumber, pageSize);
        Page<BlockDto> dtoPage = new Page<>(page);
        List<BlockDto> dtoList = new ArrayList<>();

        long reward = 0;
        for (BlockHeaderPo header : page.getList()) {
            reward = ledgerService.getBlockReward(header.getHeight());
            dtoList.add(new BlockDto(header, reward, 0));
        }
        dtoPage.setList(dtoList);
        return RpcResult.getSuccess().setData(dtoPage);
    }
}

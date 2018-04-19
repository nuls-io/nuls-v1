/**
 * MIT License
 **
 * Copyright (c) 2017-2018 nuls.io
 **
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 **
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 **
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.rpc.resources.impl;

import io.nuls.account.entity.Address;
import io.nuls.consensus.poc.protocol.service.BlockService;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.dto.Page;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.BlockHeader;
import io.nuls.protocol.model.Transaction;
import io.nuls.rpc.entity.BlockDto;
import io.nuls.rpc.entity.RpcResult;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    @ApiOperation(value = "根据区块高度查询区块头 [3.2.2]", notes = "result.data: blockHeaderJson 返回对应的区块头信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = BlockDto.class)
    })
    public RpcResult getHeaderByHeight(@ApiParam(name="height", value="区块高度", required = true)
                                           @PathParam("height") Integer height) throws NulsException, IOException {
        if (height < 0 || height > Integer.MAX_VALUE) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }

        RpcResult result = RpcResult.getSuccess();
        BlockHeader blockHeader = blockService.getBlockHeader(height);
        if (blockHeader == null) {
            return RpcResult.getFailed(ErrorCode.DATA_NOT_FOUND);
        }
        long reward = ledgerService.getBlockReward(blockHeader.getHeight());
        Long fee = ledgerService.getBlockFee(blockHeader.getHeight());
        if (null == fee) {
            fee = getFeeOfBlock(blockHeader);
        }
        result.setData(new BlockDto(blockHeader, reward, fee));
        return result;
    }

    private long getFeeOfBlock(BlockHeader header) {
        return getFeeOfBlock(blockService.getBlock(header.getHash().getDigestHex()));
    }

    private long getFeeOfBlock(Block block) {
        long fee = 0L;
        for (Transaction tx : block.getTxs()) {
            fee += tx.getFee().getValue();
        }
        return fee;
    }


    @GET
    @Path("/header/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据区块hash查询区块头 [3.2.1]", notes = "result.data: blockHeaderJson 返回对应的区块头信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = BlockDto.class)
    })
    public RpcResult getHeader(@ApiParam(name="hash", value="区块hash", required = true)
                                   @PathParam("hash") String hash) throws NulsException, IOException {
        hash = StringUtils.formatStringPara(hash);
        if (!StringUtils.validHash(hash)) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }

        RpcResult result = RpcResult.getSuccess();
        BlockHeader blockHeader = blockService.getBlockHeader(hash);
        if (blockHeader == null) {
            return RpcResult.getFailed(ErrorCode.DATA_NOT_FOUND);
        }
        long reward = ledgerService.getBlockReward(blockHeader.getHeight());
        Long fee = ledgerService.getBlockFee(blockHeader.getHeight());
        if (null == fee) {
            fee = getFeeOfBlock(blockHeader);
        }
        result.setData(new BlockDto(blockHeader, reward, fee));
        return result;
    }

    @GET
    @Path("/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("根据区块hash查询区块，包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用 [3.2.3]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = BlockDto.class)
    })
    public RpcResult loadBlock(@ApiParam(name="hash", value="区块hash", required = true)
                                   @PathParam("hash") String hash) {
        RpcResult result;
        if (!StringUtils.validHash(hash)) {
            return RpcResult.getFailed(ErrorCode.PARAMETER_ERROR);
        }
        Block block = blockService.getBlock(hash);
        if (block == null) {
            result = RpcResult.getFailed(ErrorCode.DATA_NOT_FOUND);
        } else {
            long reward = ledgerService.getBlockReward(block.getHeader().getHeight());
            Long fee = ledgerService.getBlockFee(block.getHeader().getHeight());
            if (null == fee) {
                fee = getFeeOfBlock(block);
            }
            result = RpcResult.getSuccess();
            try {
                result.setData(new BlockDto(block, reward, fee));
            } catch (IOException e) {
                //todo
                Log.error(e);
            }
        }
        return result;
    }

    @GET
    @Path("/height/{height}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("根据区块高度查询区块，包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用 [3.2.4]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = BlockDto.class)
    })
    public RpcResult getBlock(@ApiParam(name="height", value="区块高度", required = true)
                                  @PathParam("height") Long height) {
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
            Long fee = ledgerService.getBlockFee(block.getHeader().getHeight());
            if (null == fee) {
                fee = getFeeOfBlock(block);
            }
            try {
                result.setData(new BlockDto(block, reward, fee));
            } catch (IOException e) {
                //todo
                Log.error(e);
            }
        }
        return result;
    }

    @GET
    @Path("/newest")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询最新区块头信息 [3.2.5]", notes = "result.data: blockHeaderJson 返回对应的区块头信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = BlockDto.class)
    })
    public RpcResult getBestBlockHash() throws IOException {
        RpcResult result = RpcResult.getSuccess();
        result.setData(new BlockDto(NulsContext.getInstance().getBestBlock().getHeader(), 0, 0));
        return result;
    }

    @GET
    @Path("/list/address")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询共识节点的出块信息，块高度倒序排列 [3.2.6]",
            notes = "result.data: Page<blockHeaderJson> 返回对应的区块头信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = BlockDto.class)
    })
    public RpcResult getBlocksByAddress(@ApiParam(name="address", value="节点账户地址", required = true)
                                            @QueryParam("address") String address,
                                        @ApiParam(name="type", value="1:委托节点, 2打包节点", required = true)
                                            @QueryParam("type") int type,
                                        @ApiParam(name="pageNumber", value="页码")
                                            @QueryParam("pageNumber") int pageNumber,
                                        @ApiParam(name="pageSize", value="每页条数")
                                            @QueryParam("pageSize") int pageSize) {

        if (type < 1 || type > 2 || pageNumber < 0 || pageSize < 0 || !Address.validAddress(address)) {
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
    @ApiOperation(value = "查询区块头列表信息，块高度倒序排列 [3.2.7]",
            notes = "result.data: Page<blockHeaderJson> 返回对应的区块头信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = BlockDto.class)
    })
    public RpcResult getBlocks(@ApiParam(name="pageNumber", value="页码")
                                   @QueryParam("pageNumber") int pageNumber,
                               @ApiParam(name="pageSize", value="每页条数")
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

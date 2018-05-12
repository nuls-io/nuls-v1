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

package io.nuls.protocol.rpc.resources;

import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.protocol.rpc.model.BlockDto;
import io.nuls.protocol.rpc.model.BlockHeaderDto;
import io.nuls.protocol.service.BlockService;
import io.swagger.annotations.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * @author: Niels Wang
 * @date: 2018/5/8
 */
@Path("/block")
@Api(value = "/browse", description = "Block")
@Component
public class BlockResource {

    @Autowired
    private BlockService blockService;

    @GET
    @Path("/header/height/{height}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据区块高度查询区块头", notes = "result.data: blockHeaderJson 返回对应的区块头信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = BlockDto.class)
    })
    public Result<BlockHeaderDto> getHeaderByHeight(@ApiParam(name = "height", value = "区块高度", required = true)
                                                    @PathParam("height") Integer height) {
        Result<Block> blockResult = blockService.getBlock(height);
        if (blockResult.isFailed()) {
            return Result.getFailed(KernelErrorCode.DATA_NOT_FOUND);
        }
        BlockHeaderDto dto = new BlockHeaderDto(blockResult.getData());
        return Result.getSuccess().setData(dto);
    }


    @GET
    @Path("/header/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据区块hash查询区块头", notes = "result.data: blockHeaderJson 返回对应的区块头信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = BlockDto.class)
    })
    public Result<BlockHeaderDto> getHeader(@ApiParam(name = "hash", value = "区块hash", required = true)
                                            @PathParam("hash") String hash) {
        hash = StringUtils.formatStringPara(hash);
        if (!StringUtils.validHash(hash)) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Result result = Result.getSuccess();
        Block block = null;
        try {
            block = blockService.getBlock(NulsDigestData.fromDigestHex(hash)).getData();
        } catch (NulsException e) {
            Log.error(e);
        }
        if (block == null) {
            return Result.getFailed(KernelErrorCode.DATA_NOT_FOUND);
        }
        result.setData(new BlockHeaderDto(block));
        return result;
    }

    @GET
    @Path("/hash/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("根据区块hash查询区块，包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = BlockDto.class)
    })
    public Result<BlockDto> loadBlock(@ApiParam(name = "hash", value = "区块hash", required = true)
                                      @PathParam("hash") String hash) {
        Result result;
        if (!StringUtils.validHash(hash)) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }
        Block block = null;
        try {
            block = blockService.getBlock(NulsDigestData.fromDigestHex(hash)).getData();
        } catch (NulsException e) {
            Log.error(e);
        }
        if (block == null) {
            result = Result.getFailed(KernelErrorCode.DATA_NOT_FOUND);
        } else {
            result = Result.getSuccess();
            result.setData(new BlockDto(block));

        }
        return result;
    }

    @GET
    @Path("/height/{height}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("根据区块高度查询区块，包含区块打包的所有交易信息，此接口返回数据量较多，谨慎调用")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = BlockDto.class)
    })
    public Result<BlockDto> getBlock(@ApiParam(name = "height", value = "区块高度", required = true)
                                     @PathParam("height") Long height) {
        Result result = Result.getSuccess();
        if (height < 0) {
            return Result.getFailed(KernelErrorCode.PARAMETER_ERROR);
        }

        Block block = blockService.getBlock(height).getData();
        if (block == null) {
            result = Result.getFailed(KernelErrorCode.DATA_NOT_FOUND);
        } else {
            result.setData(new BlockDto(block));
        }
        return result;
    }

    @GET
    @Path("/newest")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询最新区块头信息", notes = "result.data: blockHeaderJson 返回对应的区块头信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = BlockDto.class)
    })
    public Result getBestBlockHash() throws IOException {
        Result result = Result.getSuccess();
        result.setData(new BlockHeaderDto(NulsContext.getInstance().getBestBlock()));
        return result;
    }
}

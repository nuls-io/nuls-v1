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

import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Result;
import io.nuls.protocol.rpc.model.BlockDto;
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
public class BlockResource {
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);

    @GET
    @Path("/header/height/{height}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "根据区块高度查询区块头 [3.2.2]", notes = "result.data: blockHeaderJson 返回对应的区块头信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = BlockDto.class)
    })
    public Result getHeaderByHeight(@ApiParam(name="height", value="区块高度", required = true)
                                       @PathParam("height") Integer height) throws NulsException, IOException {

        return null;
    }
}

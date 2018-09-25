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

package io.nuls.network.rpc.resource;

import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.network.cache.NodeCacheManager;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.rpc.model.NetworkInfoDto;
import io.nuls.network.rpc.model.NodeDto;
import io.nuls.network.service.NetworkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.*;

@Path("/network")
@Api(value = "/network", description = "network")
@Component
public class NetworkResource {

    @Autowired
    private NetworkService networkService;

    private NodeCacheManager nodeCacheManager = NodeCacheManager.getInstance();

    @GET
    @Path("/info/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询网络最新信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = NetworkInfoDto.class)
    })
    public RpcClientResult getNetworkInfo() {
        NetworkInfoDto info = new NetworkInfoDto(NulsContext.getInstance().getBestBlock().getHeader().getHeight(),
                NulsContext.getInstance().getNetBestBlockHeight(), TimeService.getNetTimeOffset());

        NodeGroup inGroup = networkService.getNodeGroup(NetworkConstant.NETWORK_NODE_IN_GROUP);
        NodeGroup outGroup = networkService.getNodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
        int count = 0;
        for (Node node : inGroup.getNodes().values()) {
            if (node.getStatus() == Node.HANDSHAKE) {
                count += 1;
            }
        }
        info.setInCount(count);

        count = 0;
        for (Node node : outGroup.getNodes().values()) {
            if (node.getStatus() == Node.HANDSHAKE) {
                count += 1;
            }
        }
        info.setOutCount(count);
        info.setMastUpGrade(NulsContext.mastUpGrade);
        Result result = Result.getSuccess();
        result.setData(info);
        return result.toRpcClientResult();
    }

    @GET
    @Path("/nodes")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("查询节点IP [3.7.2]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = String[].class)
    })
    public RpcClientResult getNode() {
        Set<String> ipSet = nodeCacheManager.getIpSet();
        if (ipSet == null || ipSet.isEmpty()) {
            ipSet = new HashSet<>();
            List<Node> nodeList = networkService.getCanConnectNodes();
            for (Node node : nodeList) {
                ipSet.add(node.getIp());
            }
            nodeCacheManager.cacheIpSet(ipSet);
        }
        Result result = Result.getSuccess();
        Map<String, Set<String>> map = new HashMap<>();
        map.put("list", ipSet);
        result.setData(map);
        return result.toRpcClientResult();
    }

    @GET
    @Path("/peers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("查询节点[3.7.2]")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = NodeDto.class)
    })
    public RpcClientResult getPeers() {
        List<Node> nodeList = networkService.getCanConnectNodes();
        Result result = Result.getSuccess();
        List<NodeDto> dtoList = new ArrayList<>();
        for (Node node : nodeList) {
            NodeDto dto = new NodeDto();
            dto.setIp(node.getIp());
            dto.setPort(node.getPort());
            dtoList.add(dto);
        }
        Map<String, List<NodeDto>> map = new HashMap<>();
        map.put("list", dtoList);
        result.setData(map);
        return result.toRpcClientResult();
    }
}

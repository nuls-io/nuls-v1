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

import io.nuls.core.context.NulsContext;
import io.nuls.core.event.CommonStringEvent;
import io.nuls.core.utils.date.TimeService;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.service.NetworkService;
import io.nuls.rpc.entity.InfoDto;
import io.nuls.rpc.entity.RpcResult;
import io.swagger.annotations.Api;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2017/10/24
 */
@Path("/network")

@Api(value = "/browse", description = "Network")
public class NetworkMessageResource {

    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);

    public NetworkMessageResource() {
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult broadcast(String message) {
        CommonStringEvent event = new CommonStringEvent();
        event.setMessage(message);
        eventBroadcaster.broadcastAndCacheAysn(event, false);
        return RpcResult.getSuccess();
    }


    public RpcResult send(String message, String nodeId) {
        CommonStringEvent event = new CommonStringEvent();
        event.setMessage(message);
        eventBroadcaster.sendToNodeAysn(event, nodeId);
        return RpcResult.getSuccess();
    }


    public RpcResult broadcast(String message, String groupId) {
        CommonStringEvent event = new CommonStringEvent();
        event.setMessage(message);
        eventBroadcaster.sendToGroupAysn(event, groupId);
        return RpcResult.getSuccess();
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getInfo() {
        RpcResult result = RpcResult.getSuccess();
        InfoDto info = new InfoDto(NulsContext.getInstance().getBestBlock().getHeader().getHeight(),
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
        result.setData(info);
        return result;
    }

    @GET
    @Path("/nodes")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getNode() {
        Set<String> ipSet = networkService.getNodesIp();
        RpcResult result = RpcResult.getSuccess().setData(ipSet);
        return result;
    }
}

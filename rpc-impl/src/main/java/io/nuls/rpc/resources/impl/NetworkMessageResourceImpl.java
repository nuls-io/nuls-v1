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

import io.nuls.core.context.NulsContext;
import io.nuls.core.event.CommonStringEvent;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.network.service.NetworkService;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.NetworkMessageResource;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Niels
 * @date 2017/10/24
 *
 */
@Path("/broadcast")
public class NetworkMessageResourceImpl implements NetworkMessageResource {

    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);

    @Override
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult broadcast(String message) {
        CommonStringEvent event = new CommonStringEvent();
        event.setMessage(message);
        eventBroadcaster.broadcastAndCacheAysn(event, false);
        return RpcResult.getSuccess();
    }

    @Override
    public RpcResult send(String message, String nodeId) {
        CommonStringEvent event = new CommonStringEvent();
        event.setMessage(message);
        eventBroadcaster.sendToNodeAysn(event, nodeId);
        return RpcResult.getSuccess();
    }

    @Override
    public RpcResult broadcast(String message, String groupId) {
        CommonStringEvent event = new CommonStringEvent();
        event.setMessage(message);
        eventBroadcaster.sendToGroupAysn(event, groupId);
        return RpcResult.getSuccess();
    }
}

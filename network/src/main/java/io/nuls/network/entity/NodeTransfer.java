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
package io.nuls.network.entity;

import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.utils.date.TimeService;
import io.nuls.db.entity.NodePo;

/**
 * @author vivi
 * @date 2017/11/30.
 */
public class NodeTransfer {


    public static void toNode(Node node, NodePo po) {
        node.setFailCount(po.getFailCount());
        node.setIp(po.getIp());
        node.setPort(po.getPort());
        node.setLastTime(po.getLastTime());
        node.setMagicNumber(po.getMagicNum());
        node.setFailCount(po.getFailCount());
        node.setVersion(new NulsVersion(po.getVersion()));
        node.setHash(po.getId());
    }


    public static NodePo toPojo(Node node) {
        NodePo po = new NodePo();
        po.setFailCount(node.getFailCount());
        po.setIp(node.getIp());
        po.setPort(node.getPort());
        po.setLastTime(node.getLastTime());
        po.setMagicNum(node.getMagicNumber());
        po.setVersion(node.getVersion().getVersion());
        po.setLastFailTime(node.getLastFailTime());
        if (po.getLastTime() == null) {
            po.setLastTime(TimeService.currentTimeMillis());
        }
        if(po.getFailCount() == null) {
            po.setFailCount(0);
        }
        po.setId(node.getHash());
        return po;
    }
}

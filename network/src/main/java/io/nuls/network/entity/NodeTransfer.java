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
        node.setFailCount(null);
        node.setIp(po.getIp());
        node.setPort(po.getPort());
        node.setLastTime(po.getLastTime());
        node.setMagicNumber(po.getMagicNum());
        node.setFailCount(po.getFailCount());
        node.setVersion(new NulsVersion(po.getVersion()));
        node.setHash(node.getIp() + node.getPort());
    }


    public static NodePo toPojo(Node node) {

        NodePo po = new NodePo();
        po.setFailCount(node.getFailCount());
        po.setIp(node.getIp());
        po.setPort(node.getPort());
        po.setLastTime(node.getLastTime());
        po.setMagicNum(node.getMagicNumber());
        po.setVersion(node.getVersion().getVersion());
        if (po.getLastTime() == null) {
            po.setLastTime(TimeService.currentTimeMillis());
        }
        if(po.getFailCount() == null) {
            po.setFailCount(0);
        }
        return po;
    }
}

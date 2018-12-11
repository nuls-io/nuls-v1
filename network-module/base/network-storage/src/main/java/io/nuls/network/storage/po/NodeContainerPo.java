package io.nuls.network.storage.po;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NodeContainerPo implements Serializable {

    private List<NodePo> disConnectNodes;

    private List<NodePo> canConnectNodes;

    private List<NodePo> failNodes;

    private List<NodePo> uncheckNodes;

    public NodeContainerPo(){
        disConnectNodes = new ArrayList<>();
        canConnectNodes = new ArrayList<>();
        failNodes = new ArrayList<>();
        uncheckNodes = new ArrayList<>();
    }

    public List<NodePo> getDisConnectNodes() {
        return disConnectNodes;
    }

    public void setDisConnectNodes(List<NodePo> disConnectNodes) {
        this.disConnectNodes = disConnectNodes;
    }

    public List<NodePo> getCanConnectNodes() {
        return canConnectNodes;
    }

    public void setCanConnectNodes(List<NodePo> canConnectNodes) {
        this.canConnectNodes = canConnectNodes;
    }

    public List<NodePo> getFailNodes() {
        return failNodes;
    }

    public void setFailNodes(List<NodePo> failNodes) {
        this.failNodes = failNodes;
    }

    public List<NodePo> getUncheckNodes() {
        return uncheckNodes;
    }

    public void setUncheckNodes(List<NodePo> uncheckNodes) {
        this.uncheckNodes = uncheckNodes;
    }
}

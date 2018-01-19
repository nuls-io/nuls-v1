package io.nuls.network.entity;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.str.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author Facjas
 * date 2018/1/19.
 */
public class NodeArea {
    private String areaName;
    private Map<String, NodeGroup> nodegroups = new ConcurrentHashMap<>();


    public NodeArea(String areaName){
        this.areaName = areaName;
        nodegroups = new ConcurrentHashMap<>();
    }
    public NodeArea(String areaName, Map<String,NodeGroup> nodegroups){
        this(areaName);
        this.nodegroups = nodegroups;
        for(NodeGroup ng : nodegroups.values()){
            addGroup(ng);
            ng.addtoArea(this);
        }
    }

    public String getAreaName(){
        return areaName;
    }

    public void setAreaName(String areaName){
        if(!StringUtils.isNull(areaName)) {
            this.areaName = areaName;
        }
    }

    public NodeGroup getNodeGroup(String groupName){
        if(!StringUtils.isNull(groupName)) {
            return nodegroups.get(groupName);
        }
        return null;
    }

    public boolean hasGroup(String groupName){
        if(!StringUtils.isNull(groupName)) {
            return nodegroups.containsKey(groupName);
        }
        return false;
    }

    public void addGroup(String groupName, NodeGroup nodeGroup){
        if(!StringUtils.isNull(groupName) && nodeGroup !=null){
            if(nodegroups.containsKey(groupName)){
                throw new NulsRuntimeException(ErrorCode.NODE_AREA_ALREADY_EXISTS);
            }
            nodegroups.put(groupName,nodeGroup);
            nodeGroup.addtoArea(this);
        }
    }

    public void addGroup(NodeGroup nodeGroup){
        addGroup(nodeGroup.getName(),nodeGroup);
    }

    public void removeGroup(String groupName){
        if(!StringUtils.isNull(groupName)){
            if(!nodegroups.containsKey(groupName)){
                return;
            }
            NodeGroup nodeGroup = nodegroups.get(groupName);
            nodeGroup.removeFromArea(this);
            nodegroups.remove(groupName);
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{areaName:'").append(this.getAreaName()).append("'}");
        return sb.toString();
    }
}

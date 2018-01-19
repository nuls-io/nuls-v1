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

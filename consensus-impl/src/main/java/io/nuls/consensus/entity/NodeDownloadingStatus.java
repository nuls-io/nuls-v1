package io.nuls.consensus.entity;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Niels
 * @date 2018/1/16
 */
public class NodeDownloadingStatus {

    private String nodeId;
    private long start;
    private long end;
    private Set<Long> downloadedSet = new HashSet<>();
    private long updateTime;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public Set<Long> getDownloadedSet() {
        return downloadedSet;
    }

    public void setDownloadedSet(Set<Long> downloadedSet) {
        this.downloadedSet = downloadedSet;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public boolean containsHeight(long height) {
        return height >= start && height <= end;
    }

    public void downloaded(long height) {
        downloadedSet.add(height);
    }

    public boolean finished() {
        return downloadedSet.size()==(end-start+1);
    }
}

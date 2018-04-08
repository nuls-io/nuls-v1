package io.nuls.consensus.download;

import io.nuls.network.entity.Node;

import java.util.List;

/**
 * Created by ln on 2018/4/8.
 */
public class NetworkNewestBlockInfos {

    private long netBestHeight;
    private String netBestHash;
    private long localBestHeight;
    private String localBestHash;

    private List<Node> nodes;

    public NetworkNewestBlockInfos() {
    }

    public NetworkNewestBlockInfos(long netBestHeight, String netBestHash, List<Node> nodes) {
        this.netBestHeight = netBestHeight;
        this.netBestHash = netBestHash;
        this.nodes = nodes;
    }

    public void setNetBestHeight(long netBestHeight) {
        this.netBestHeight = netBestHeight;
    }

    public void setNetBestHash(String netBestHash) {
        this.netBestHash = netBestHash;
    }

    public void setLocalBestHeight(long localBestHeight) {
        this.localBestHeight = localBestHeight;
    }

    public void setLocalBestHash(String localBestHash) {
        this.localBestHash = localBestHash;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public long getNetBestHeight() {
        return netBestHeight;
    }

    public String getNetBestHash() {
        return netBestHash;
    }

    public long getLocalBestHeight() {
        return localBestHeight;
    }

    public String getLocalBestHash() {
        return localBestHash;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "NetworkNewestBlockInfos{" +
                "netBestHeight=" + netBestHeight +
                ", netBestHash='" + netBestHash + '\'' +
                ", localBestHeight=" + localBestHeight +
                ", localBestHash='" + localBestHash + '\'' +
                ", nodes=" + nodes +
                '}';
    }
}

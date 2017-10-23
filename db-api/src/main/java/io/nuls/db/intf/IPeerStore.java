package io.nuls.db.intf;

import io.nuls.db.entity.Peer;

import java.util.List;

/**
 * Created by win10 on 2017/10/19.
 */
public interface IPeerStore extends IStore<Peer,Long>{

    /**
     * 获取主动连接的节点信息
     * @return
     */
    List<Peer> getInPeers();

    /**
     * 获取节点组信息
     * @param group
     * @return
     */
    List<Peer> getPeersByGroup(String group);


    /**
     * 根据ip获取节点信息
     * @param ip
     * @return
     */
    List<Peer> getByIp(String ip);

    /**
     * 根据ip和端口号获取节点信息
     * @param ip
     * @param port
     * @return
     */
    Peer getPeer(String ip, int port);
}

package io.nuls.network.constant;

import io.nuls.core.mesasge.NulsMessageHeader;

/**
 * @author vivi
 * @date 2017.11.10
 */
public interface NetworkConstant {

    int MESSAGE_MAX_SIZE = NulsMessageHeader.MESSAGE_HEADER_SIZE + 2<<21;

    /**
     * --------[network] -------
     */
    String NETWORK_PROPERTIES = "network.properties";
    String NETWORK_SECTION = "Network";
    String NETWORK_TYPE = "net.type";
    String NETWORK_SERVER_PORT = "network.server.port";
    String NETWORK_EXTER_PORT = "network.external.port";
    String NETWORK_MAGIC = "network.magic";

    String NETWORK_NODE_MAX_IN = "net.node.max.in";
    String NETWORK_NODE_MAX_OUT = "net.node.max.out";

    String NETWORK_NODE_IN_GROUP = "inNodes";
    String NETWORK_NODE_OUT_GROUP = "outNodes";
    String NETWORK_NODE_CONSENSUS_GROUP = "consensus_Group";
    String NETWORK_NODE_DEFAULT_GROUP = "all";
    int NETWORK_BROAD_SUCCESS_MIN_COUNT = 1;
    int NETWORK_BROAD_MAX_TRY_COUNT = 3;

    //network message type

    short NETWORK_GET_VERSION_EVENT = 01;
    short NETWORK_VERSION_EVENT = 02;
    short NETWORK_PING_EVENT = 03;
    short NETWORK_PONG_EVENT = 04;
    short NETWORK_BYE_EVENT = 05;
    short NETWORK_GET_NODE_EVENT = 06;
    short NETWORK_NODE_EVENT = 07;
}

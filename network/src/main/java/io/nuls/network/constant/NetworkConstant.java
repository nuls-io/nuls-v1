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
    String NETWORK_PORT_TEST = "test.port";
    String NETWORK_MAGIC_TEST = "test.magic";
    String NETWORK_PORT_DEV = "dev.port";
    String NETWORK_MAGIC_DEV = "dev.magic";

    String NETWORK_NODE_MAX_IN = "net.node.max.in";
    String NETWORK_NODE_MAX_OUT = "net.node.max.out";

    String NETWORK_NODE_IN_GROUP = "inNodes";
    String NETWORK_NODE_OUT_GROUP = "outNodes";
    String NETWORK_NODE_CONSENSUS_GROUP = "consensus_Group";
    int NETWORK_NODE_OUT_MIN_COUNT = 1;
    int NETWORK_BROAD_MAX_TRY_COUNT = 3;

    //network message type

    short NETWORK_GET_VERSION_MESSAGE = 01;
    short NETWORK_VERSION_MESSAGE = 02;
    short NETWORK_PING_MESSAGE = 03;
    short NETWORK_PONG_MESAAGE = 04;
    short NETWORK_BYE_MESSAGE = 05;
    short NETWORK_GET_NODE_MESSAGE = 06;
    short NETWORK_NODE_MESSAGE = 07;
}

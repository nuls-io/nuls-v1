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

    String NETWORK_PEER_MAX_IN = "net.peer.max.in";
    String NETWORK_PEER_MAX_OUT = "net.peer.max.out";

    String NETWORK_PEER_IN_GROUP = "inPeers";
    String NETWORK_PEER_OUT_GROUP = "outPeers";
    String NETWORK_PEER_CONSENSUS_GROUP = "consensus_Group";


    //network message type

    short NETWORK_GET_VERSION_MESSAGE = 01;
    short NETWORK_VERSION_MESSAGE = 02;
    short NETWORK_PING_MESSAGE = 03;
    short NETWORK_PONG_MESAAGE = 04;
    short NETWORK_BYE_MESSAGE = 05;
    short NETWORK_GET_PEER_MESSAGE = 06;
    short NETWORK_PEER_MESSAGE = 07;
}

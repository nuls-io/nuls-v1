package io.nuls.network.constant;

/**
 * @author vivi
 * @date 2017.11.10
 */
public interface NetworkConstant {
    //todo version
    int NETWORK_MODULE_VERSION = 1111;
    //Minimum version supported
    int MINIMUM_VERSION_SUPPORTED = 0;


    /**--------[network] -------*/
    String Network_Properties = "network.properties";
    String Network_Section = "Network";
    String Network_Type = "net.type";
    String Network_Port = "network.port";
    String Network_Magic = "network.magic";
    String Network_Port_Test = "test.port";
    String Network_Magic_Test = "test.magic";
    String Network_Port_Dev = "dev.port";
    String Network_Magic_Dev = "dev.magic";

    String Network_Peer_Max_In = "net.peer.max.in";
    String Network_Peer_Max_Out = "net.peer.max.out";

    String Network_Peer_In_Group = "inPeers";
    String Network_Peer_Out_Group = "outPeers";
    String Network_Peer_Consensus_Group = "consensus_Group";


    //network message type

    short Network_Version_Message = 01;
    short Network_Ping_Message = 02;
    short Network_Pong_Mesaage = 03;
    short Network_Bye_Message = 04;
}

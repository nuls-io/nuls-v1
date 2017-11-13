package io.nuls.network.constant;

/**
 * Created by Niels on 2017/11/7.
 * nuls.io
 */
public interface NetworkConstant {
    //todo version
    int NETWORK_MODULE_VERSION = 1111;
    //Minimum version supported
    int MINIMUM_VERSION_SUPPORTED = 0;


    /**--------[network] -------*/
    String Network_Section = "Network";
    String Network_Type = "net.type";
    String Network_Port_Main = "main.port";
    String Network_Magic_Main = "main.magic";
    String Network_Port_Test = "test.port";
    String Network_Magic_Test = "test.magic";
    String Network_Port_Dev = "dev.port";
    String Network_Magic_Dev = "dev.magic";

    String Network_Peer_Max_In = "net.peer.max.in";
    String Network_Peer_Max_Out = "net.peer.max.out";

    String Network_Peer_In_Group = "inPeers";
    String Network_Peer_Out_Group = "outPeers";
    String Network_Peer_Consensus_Group = "consensus_Group";
}

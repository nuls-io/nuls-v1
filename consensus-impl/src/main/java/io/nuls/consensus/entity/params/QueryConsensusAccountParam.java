package io.nuls.consensus.entity.params;

/**
 * @author Niels
 * @date 2017/12/16
 */
public class QueryConsensusAccountParam {
    private String address;
    private String agentAddress;

    public QueryConsensusAccountParam( ){
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }
}

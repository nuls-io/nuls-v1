package io.nuls.consensus.entity.meeting;

import io.nuls.account.entity.Address;
import io.nuls.core.crypto.Sha256Hash;

/**
 * @author Niels
 * @date 2017/12/25
 */
public class PocMeetingMember implements Comparable<PocMeetingMember> {
    private long roundStartTime;
    private int roundIndex;
    private String address;
    private String packerAddress;
    private long packTime;


    private String sortValue;

    @Override
    public int compareTo(PocMeetingMember o2) {
        if (this.getSortValue() == null) {
            String hashHex = new Address(this.getAddress()).hashHex();
            this.setSortValue(Sha256Hash.twiceOf((roundStartTime + hashHex).getBytes()).toString());
        }
        if (o2.getSortValue() == null) {
            String hashHex = new Address(o2.getAddress()).hashHex();
            o2.setSortValue(Sha256Hash.twiceOf((o2.getRoundStartTime() + hashHex).getBytes()).toString());
        }
        return this.getSortValue().compareTo(o2.getSortValue());
    }

    public String getSortValue() {
        return sortValue;
    }

    public void setSortValue(String sortValue) {
        this.sortValue = sortValue;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPackerAddress() {
        return packerAddress;
    }

    public void setPackerAddress(String packerAddress) {
        this.packerAddress = packerAddress;
    }

    public int getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(int roundIndex) {
        this.roundIndex = roundIndex;
    }

    public long getPackTime() {
        return packTime;
    }

    public void setPackTime(long packTime) {
        this.packTime = packTime;
    }
}

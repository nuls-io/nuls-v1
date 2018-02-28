package io.nuls.rpc.entity;

public class VersionDto {

    private String myVersion;

    private String newestVersion;

    public String getMyVersion() {
        return myVersion;
    }

    public void setMyVersion(String myVersion) {
        this.myVersion = myVersion;
    }

    public String getNewestVersion() {
        return newestVersion;
    }

    public void setNewestVersion(String newestVersion) {
        this.newestVersion = newestVersion;
    }
}

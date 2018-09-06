package io.nuls.protocol.storage.po;

/**
 * @author: Charlie
 * @date: 2018/8/17
 */
public class UpgradeInfoPo {

    private int version;

    private int upgradeCount;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getUpgradeCount() {
        return upgradeCount;
    }

    public void setUpgradeCount(int upgradeCount) {
        this.upgradeCount = upgradeCount;
    }
}

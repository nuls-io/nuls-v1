package io.nuls.rpc.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "versionJSON")
public class VersionDto {

    @ApiModelProperty(name = "myVersion", value = "当前版本号")
    private String myVersion;

    @ApiModelProperty(name = "newestVersion", value = "可更新的最新版本号")
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

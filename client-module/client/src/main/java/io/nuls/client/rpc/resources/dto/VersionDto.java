/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.client.rpc.resources.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "versionJSON")
public class VersionDto {

    @ApiModelProperty(name = "myVersion", value = "当前版本号")
    private String myVersion;

    @ApiModelProperty(name = "newestVersion", value = "可更新的最新版本号")
    private String newestVersion;

    @ApiModelProperty(name = "upgradeable", value = "是否可以进行升级")
    private boolean upgradable;

    @ApiModelProperty(name = "infromation", value = "新版本说明")
    private String infromation;

    @ApiModelProperty(name = "networkVersion", value = "运行网络版本")
    private Integer networkVersion;

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

    public boolean isUpgradable() {
        return upgradable;
    }

    public void setUpgradable(boolean upgradable) {
        this.upgradable = upgradable;
    }

    public String getInfromation() {
        return infromation;
    }

    public void setInfromation(String infromation) {
        this.infromation = infromation;
    }

    public Integer getNetworkVersion() {
        return networkVersion;
    }

    public void setNetworkVersion(Integer networkVersion) {
        this.networkVersion = networkVersion;
    }
}

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

package io.nuls.consensus.poc.rpc.model;

import io.nuls.core.tools.str.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.ws.rs.QueryParam;

/**
 * @author Niels
 * @date 2018/3/14
 */
@ApiModel(value = "获取创建共识(代理)节点表单数据")
public class GetCreateAgentFeeForm {

    @ApiModelProperty(name = "agentAddress", value = "申请账户的地址", required = true)
    @QueryParam("agentAddress")
    private String agentAddress;

    @ApiModelProperty(name = "packingAddress", value = "打包地址", required = true)
    @QueryParam("packingAddress")
    private String packingAddress;
    @ApiModelProperty(name = "rewardAddress", value = "结算地址", required = false)
    @QueryParam("rewardAddress")
    private String rewardAddress;

    @ApiModelProperty(name = "commissionRate", value = "佣金比例", required = true)
    @QueryParam("commissionRate")
    private double commissionRate;

    @ApiModelProperty(name = "deposit", value = "参与共识需要的总金额", required = true)
    @QueryParam("deposit")
    private long deposit;

    @ApiModelProperty(name = "remark", value = "节点备注", required = true)
    @QueryParam("remark")
    private String remark;

    @ApiModelProperty(name = "agentName", value = "节点名称", required = true)
    @QueryParam("agentName")
    private String agentName;

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = StringUtils.formatStringPara(agentAddress);
    }

    public String getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(String packingAddress) {
        this.packingAddress = StringUtils.formatStringPara(packingAddress);
    }

    public String getRewardAddress() {
        return rewardAddress;
    }

    public void setRewardAddress(String rewardAddress) {
        this.rewardAddress = rewardAddress;
    }

    public long getDeposit() {
        return deposit;
    }

    public void setDeposit(long deposit) {
        this.deposit = deposit;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = StringUtils.formatStringPara(remark);
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = StringUtils.formatStringPara(agentName);
    }
}

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

package io.nuls.account.rpc.model;

import io.nuls.account.model.Balance;
import io.nuls.contract.dto.ContractTokenInfo;
import io.nuls.kernel.model.Na;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigInteger;

@ApiModel(value = "assetJSON")
public class AssetDto {

    @ApiModelProperty(name = "asset", value = "资产名称")
    private String asset;

    @ApiModelProperty(name = "address", value = "资产地址")
    private String address;

    @ApiModelProperty(name = "balance", value = "余额")
    private BigInteger balance;

    @ApiModelProperty(name = "usable", value = "可用余额")
    private BigInteger usable;

    @ApiModelProperty(name = "locked", value = "锁定余额")
    private BigInteger locked;

    @ApiModelProperty(name = "decimals", value = "货币小数位精度")
    private long decimals;

    public AssetDto(String asset, Balance balance) {
        this.balance = BigInteger.valueOf(balance.getBalance().getValue());
        this.usable = BigInteger.valueOf(balance.getUsable().getValue());
        this.locked = BigInteger.valueOf(balance.getLocked().getValue());
        this.asset = asset;
        this.decimals = Na.SMALLEST_UNIT_EXPONENT;
    }

    public AssetDto(ContractTokenInfo tokenInfo) {
        this.balance = tokenInfo.getAmount();
        this.usable = this.balance;
        this.locked = BigInteger.ZERO;
        this.asset = tokenInfo.getSymbol();
        this.address = tokenInfo.getContractAddress();
        this.decimals = tokenInfo.getDecimals();
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public BigInteger getUsable() {
        return usable;
    }

    public void setUsable(BigInteger usable) {
        this.usable = usable;
    }

    public BigInteger getLocked() {
        return locked;
    }

    public void setLocked(BigInteger locked) {
        this.locked = locked;
    }

    public long getDecimals() {
        return decimals;
    }

    public void setDecimals(long decimals) {
        this.decimals = decimals;
    }
}

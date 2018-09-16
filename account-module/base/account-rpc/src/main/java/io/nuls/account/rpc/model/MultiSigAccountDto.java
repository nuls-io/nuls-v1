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

import io.nuls.account.model.MultiSigAccount;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.kernel.utils.AddressTool;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 */
@ApiModel(value = "multiSigAccountJSON")
public class MultiSigAccountDto {

    @ApiModelProperty(name = "address", value = "账户地址")
    private String address;

    @ApiModelProperty(name = "m", value = "最小签名数")
    private long m;

    @ApiModelProperty(name = "alias", value = "别名")
    private String alias;

    @ApiModelProperty(name = "pubkeys", value = "公钥列表")
    private List<String> pubkeys;

    public MultiSigAccountDto() {

    }

    public MultiSigAccountDto(MultiSigAccount account) {
        this.address = account.getAddress().getBase58();
        this.pubkeys = new ArrayList<>();
        for (byte[] bytes : account.getPubKeyList()) {
            pubkeys.add(Hex.encode(bytes));
        }
        this.m = account.getM();
        this.alias = account.getAlias();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getM() {
        return m;
    }

    public void setM(long m) {
        this.m = m;
    }

    public List<String> getPubkeys() {

        return pubkeys;
    }

    public void setPubkeys(List<String> pubkeys) {
        this.pubkeys = pubkeys;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}

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

package io.nuls.account.sdk.model.dto;

import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/5/15
 */
public class AccountKeyStoreDto {

    /**
     * 账户地址
     */
    private String address;

    /**
     * 加密后的私钥
     */
    private String encryptedPrivateKey;
    /**
     * 账户别名
     */
    private String alias;
    /**
     * 公钥 public key
     */
    private String pubKey;
    /**
     * 私钥 private key
     */
    private String prikey;

    public AccountKeyStoreDto() {

    }

    public AccountKeyStoreDto(Map<String, Object> map) {
        this.address = (String)map.get("address");
        this.encryptedPrivateKey = null == map.get("encryptedPrivateKey") ? null : (String)map.get("encryptedPrivateKey");
        this.alias = null == map.get("alias") ? null : (String)map.get("alias");
        this.pubKey = null == map.get("pubKey") ? null : (String)map.get("pubKey");
        this.prikey = null == map.get("prikey") ? null : (String)map.get("prikey");
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPrikey() {
        return prikey;
    }

    public void setPrikey(String prikey) {
        this.prikey = prikey;
    }
}

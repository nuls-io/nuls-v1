/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.account.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class Account extends BaseNulsData implements NulsCloneable {

    private String id;

    private String alias;

    private Address address;

    // is default acct
    private int status;

    private byte[] pubKey;

    private byte[] extend;

    private Long createTime;

    private byte[] priSeed;
    // Decrypted  prikey
    private byte[] priKey;
    //local field
    private ECKey ecKey;

    private List<Transaction> myTxs;

    public Account() {

    }

    public Account(NulsByteBuffer buffer) throws NulsException {
        super(buffer);
    }

    public ECKey getEcKey() {
        return ecKey;
    }

    public void setEcKey(ECKey ecKey) {
        this.ecKey = ecKey;
    }

    public byte[] getPriKey() {
        return priKey;
    }

    public void setPriKey(byte[] priKey) {
        this.priKey = priKey;
    }

    public boolean isEncrypted() {
        //it's encrypted when eckey is null
        if (ecKey == null) {
            return true;
        }
        try {
            ecKey.getPrivKey();
        } catch (Exception e) {
            return true;
        }
        //same pubkey means not Encrypted
        if (Arrays.equals(ecKey.getPubKey(), this.getPubKey())) {
            return false;
        } else {
            return true;
        }
    }


    public void resetKey() {
        resetKey(null);
    }

    public void resetKey(String password) {
        if (!isEncrypted()) {
            setEcKey(ECKey.fromPrivate(new BigInteger(getPriSeed())));
        } else {

        }
    }

    public boolean validatePassword(String password) {
        //todo
        return true;
    }

    /**
     * @param password
     */
    public void encrypt(String password) {
        //todo
    }

    public boolean decrypt(String password) {
        //todo
        return false;
    }

    @Override
    public int size() {
        int s = 0;
        s += 2;    //version size

        if (StringUtils.isNotBlank(alias)) {
            try {
                s += alias.getBytes(NulsContext.DEFAULT_ENCODING).length + 1;
            } catch (UnsupportedEncodingException e) {
                Log.error(e);
            }
        } else {
            s++;
        }
        try {
            s += address.getBase58().getBytes(NulsContext.DEFAULT_ENCODING).length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (null != priSeed) {
            s += priSeed.length + 1;
        } else {
            s++;
        }
        s += 1;//status

        s += pubKey.length + 1;
        if (null != extend) {
            s += extend.length + 1;
        } else {
            s++;
        }
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {

        stream.writeShort(version.getVersion());
        stream.writeString(alias);
        stream.writeString(address.getBase58());
        stream.writeBytesWithLength(priSeed);
        stream.writeBytesWithLength(pubKey);
        stream.write(new VarInt(status).encode());
        stream.writeBytesWithLength(extend);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        version = new NulsVersion(byteBuffer.readShort());
        alias = new String(byteBuffer.readByLengthByte());
        address = new Address(new String(byteBuffer.readByLengthByte()));
        id = new String(new String(byteBuffer.readByLengthByte()));
        priSeed = byteBuffer.readByLengthByte();
        pubKey = byteBuffer.readByLengthByte();
        status = byteBuffer.readInt32LE();
        extend = byteBuffer.readByLengthByte();
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public byte[] getPriSeed() {
        return priSeed;
    }

    public void setPriSeed(byte[] priSeed) {
        this.priSeed = priSeed;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    public void setPubKey(byte[] pubKey) {
        this.pubKey = pubKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public int getAccountChainId() {
        return this.getAddress().getChainId();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public Object copy() {
        Account account = new Account();
        account.setId(id);
        account.setAlias(alias);
        account.setAddress(address);
        account.setStatus(status);
        account.setPubKey(pubKey);
        account.setExtend(extend);
        account.setCreateTime(createTime);
        account.setPriSeed(priSeed);
        account.setPriKey(priKey);
        account.setEcKey(ecKey);
        return account;
    }

    public List<Transaction> getMyTxs() {
        return myTxs;
    }

    public void setMyTxs(List<Transaction> myTxs) {
        this.myTxs = myTxs;
    }
}

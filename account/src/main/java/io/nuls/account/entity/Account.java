package io.nuls.account.entity;

import io.nuls.account.util.AccountTool;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.EncryptedData;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class Account extends BaseNulsData implements NulsCloneable{

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

    public boolean validatePassword(String password){
        //todo
        return true;
    }

    /**
     *
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
        //s += VarInt.sizeOf(version);
        if (!StringUtils.isBlank(id)) {
            try {
                s += id.getBytes(NulsContext.DEFAULT_ENCODING).length + 1;
            } catch (UnsupportedEncodingException e) {
                Log.error(e);
            }
        } else {
            s++;
        }
        if (StringUtils.isNotBlank(alias)) {
            try {
                s += alias.getBytes(NulsContext.DEFAULT_ENCODING).length + 1;
            } catch (UnsupportedEncodingException e) {
                Log.error(e);
            }
        } else {
            s++;
        }
        if (null != address) {
            s += address.getHash160().length;
        }
        if (null != priSeed) {
            s += priSeed.length + 1;
        }
        s += 1;//status

        s += pubKey.length + 1;
        if (null != extend) {
            s += extend.length + 1;
        }
        return s;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        // stream.write(new VarInt(version).encode());
//        if(StringUtils.isNotBlank(id)){
//            stream.writeBytesWithLength(stream, id.getBytes(NulsContext.DEFAULT_ENCODING));
//        }else {
//            stream.write(0);
//        }
//        if(StringUtils.isNotBlank(alias)){
//            stream.writeBytesWithLength(stream, alias.getBytes(NulsContext.DEFAULT_ENCODING));
//        }else {
//            stream.write(0);
//        }
//        if (null != address && null != address.getHash160()) {
//            stream.write(address.getHash160());
//        }
//        stream.writeBytesWithLength(stream, priSeed);
//        stream.write(status);
//        stream.writeBytesWithLength(stream, sign);
//        stream.writeBytesWithLength(stream, pubKey);
//        stream.writeBytesWithLength(stream, extend);

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
//        id = new String(byteBuffer.readByLengthByte());
//        alias = new String(byteBuffer.readByLengthByte());
//        byte[] hash160 = byteBuffer.readBytes(Address.LENGTH);
//        this.address = new Address(hash160);
//        priSeed = byteBuffer.readByLengthByte();
//        status = byteBuffer.readByte();
//        sign = byteBuffer.readByLengthByte();
//        pubKey = byteBuffer.readByLengthByte();
//        extend = byteBuffer.readByLengthByte();
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
}

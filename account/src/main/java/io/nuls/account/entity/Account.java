package io.nuls.account.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 *
 * @author Niels
 * @date 2017/10/30
 */
public class Account extends BaseNulsData {

    private String id;

    private String alias;

    private Address address;

    private byte status;

    private byte[] sign;

    private byte[] pubKey;

    private byte[] extend;

    private Long createTime;
    private Long createHeight;
    private byte[] txHash;


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
            return false;
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

    @Override
    public int size() {
        int s = 0;
        s += VarInt.sizeOf(version);
        if (!StringUtils.isBlank(id)) {
            try {
                s += id.getBytes(NulsContext.DEFAULT_ENCODING).length + 1;
            } catch (UnsupportedEncodingException e) {
                Log.error(e);
            }
        }else{
            s ++;
        }
        if(StringUtils.isNotBlank(alias)){
            try {
                s += alias.getBytes(NulsContext.DEFAULT_ENCODING).length + 1;
            } catch (UnsupportedEncodingException e) {
                Log.error(e);
            }
        }else{
            s ++;
        }
        if (null != address) {
            s += address.getHash160().length;
        }
        if (null != priSeed) {
            s += priSeed.length + 1;
        }
        s += 1;//status
        if (null != sign) {
            s += sign.length + 1;
        }
        s += pubKey.length + 1;
        if(null!=extend){
            s += extend.length + 1;
        }
        return s;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        stream.write(new VarInt(version).encode());
        if(StringUtils.isNotBlank(id)){
            this.writeBytesWithLength(stream, id.getBytes(NulsContext.DEFAULT_ENCODING));
        }else {
            stream.write(0);
        }
        if(StringUtils.isNotBlank(alias)){
            this.writeBytesWithLength(stream, alias.getBytes(NulsContext.DEFAULT_ENCODING));
        }else {
            stream.write(0);
        }
        if (null != address && null != address.getHash160()) {
            stream.write(address.getHash160());
        }
        this.writeBytesWithLength(stream, priSeed);
        stream.write(status);
        this.writeBytesWithLength(stream, sign);
        this.writeBytesWithLength(stream, pubKey);
        this.writeBytesWithLength(stream, extend);

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        version = (int) byteBuffer.readUint32();
        id = new String(byteBuffer.readByLengthByte());
        alias = new String(byteBuffer.readByLengthByte());
        byte[] hash160 = byteBuffer.readBytes(Address.LENGTH);
        this.address = new Address(hash160);
        priSeed = byteBuffer.readByLengthByte();
        status = byteBuffer.readByte();
        sign = byteBuffer.readByLengthByte();
        pubKey = byteBuffer.readByLengthByte();
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

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

    public byte[] getSign() {
        return sign;
    }

    public void setSign(byte[] sign) {
        this.sign = sign;
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

    public Long getCreateHeight() {
        return createHeight;
    }

    public void setCreateHeight(Long createHeight) {
        this.createHeight = createHeight;
    }

    public byte[] getTxHash() {
        return txHash;
    }

    public void setTxHash(byte[] txHash) {
        this.txHash = txHash;
    }
}

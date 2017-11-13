package io.nuls.account.entity;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.VerificationException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.ByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by Niels on 2017/10/30.
 * nuls.io
 */
public class Account extends NulsData {

    private String id;

    private Address address;

    private byte[] priSeed;

    private byte status;

    private double credit;

    private byte[] sign;

    private byte[] pubKey;

    private byte[] extend;

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
                s += id.getBytes(NulsConstant.DEFAULT_ENCODING).length + 1;
            } catch (UnsupportedEncodingException e) {
                Log.error(e);
            }
        }
        if (null != address) {
            s += address.getHash160().length;
        }
        if (null != priSeed) {
            s += priSeed.length + 1;
        }
        s += 1;//status
        s += Utils.double2Bytes(credit).length;
        if (null != sign) {
            s += sign.length + 1;
        }
        s += pubKey.length + 1;
        s += extend.length + 1;
        return s;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        stream.write(new VarInt(version).encode());
        this.writeBytesWithLength(stream, id.getBytes(NulsConstant.DEFAULT_ENCODING));
        if (null != address&&null!=address.getHash160()) {
            stream.write(address.getHash160());
        }
        this.writeBytesWithLength(stream, priSeed);
        stream.write(status);
        this.writeBytesWithLength(stream, Utils.double2Bytes(credit));
        this.writeBytesWithLength(stream, sign);
        this.writeBytesWithLength(stream, pubKey);
        this.writeBytesWithLength(stream, extend);

    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        version = (int) byteBuffer.readUint32();
        id = new String(byteBuffer.readByLengthByte());
        byte[] hash160 = byteBuffer.readBytes(Address.LENGTH);
        this.address = new Address(hash160);
        priSeed = byteBuffer.readByLengthByte();
        status = byteBuffer.readBytes(1)[0];
        credit = Utils.bytes2Double(byteBuffer.readByLengthByte());
        sign = byteBuffer.readByLengthByte();
        pubKey = byteBuffer.readByLengthByte();
        extend = byteBuffer.readByLengthByte();
    }

    @Override
    public void verify() throws NulsException {
        ECKey key1 = ECKey.fromPublicOnly(pubKey);
        byte[] hash;
        try {
            hash = Sha256Hash.of(this.serialize()).getBytes();
        } catch (IOException e) {
            Log.error(e);
            throw new VerificationException("account verify fail");
        }
        if (!key1.verify(hash, sign)) {
            throw new VerificationException("account verify fail");
        }
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

    public double getCredit() {
        return credit;
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

    public void setCredit(double credit) {
        this.credit = credit;
    }
}

package io.nuls.account.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author vivi
 * @date 2017/12/21.
 */
public class Alias extends BaseNulsData {

    private String address;

    private String alias;

    public Alias(String address, String alias) {
        this.address = address;
        this.alias = alias;
    }

    public Alias(NulsByteBuffer buffer) throws NulsException {
        super(buffer);
    }

    @Override
    public int size() {
        int s = 0;
        try {
            s += address.getBytes(NulsContext.DEFAULT_ENCODING).length + 1;
            s += alias.getBytes(NulsContext.DEFAULT_ENCODING).length + 1;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(address);
        stream.writeString(alias);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        try {
            this.address = new String(byteBuffer.readByLengthByte(), NulsContext.DEFAULT_ENCODING);
            this.alias = new String(byteBuffer.readByLengthByte(), NulsContext.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
            throw new NulsException(ErrorCode.DATA_ERROR);
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}

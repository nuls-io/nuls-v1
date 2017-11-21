package io.nuls.core.chain.entity;

import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.DataValidatorChain;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.crypto.UnsafeByteArrayOutputStream;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsIOException;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Created by Niels on 2017/10/30.
 */
public abstract class NulsData implements Serializable {

    protected NulsDataType dataType;

    protected int version;

    private DataValidatorChain validatorChain = new DataValidatorChain();

    public NulsData() {
    }

    protected void registerValidator(NulsDataValidator<? extends NulsData> validator) {
        this.validatorChain.addValidator(validator);
    }

    /**
     * 计算序列化实体时需要的字节长度
     *
     * @return
     */
    public abstract int size();

    /**
     * First, serialize the version field
     *
     * @return
     */
    public byte[] serialize() throws IOException {
        this.verify();
        ByteArrayOutputStream bos = null;
        try {
            bos = new UnsafeByteArrayOutputStream(size());
            serializeToStream(bos);
            return bos.toByteArray();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    throw e;
                }

            }
        }
    }


    public abstract void serializeToStream(OutputStream stream) throws IOException;


    public abstract void parse(NulsByteBuffer byteBuffer);

    /**
     * @throws NulsException
     */
    public final void verify() throws NulsVerificationException {
        ValidateResult result = this.validatorChain.startDoValidator(this);
        if (!result.isSeccess()) {
            throw new NulsVerificationException(result.getMessage());
        }
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    protected void writeBytesWithLength(OutputStream stream, String value) throws IOException {
        if (StringUtils.isNotBlank(value)) {
            stream.write(0);
        } else {
            byte[] bytes = value.getBytes(NulsContext.DEFAULT_ENCODING);
            stream.write(bytes.length);
            stream.write(bytes);
        }
    }

    protected void writeBytesWithLength(OutputStream stream, byte[] bytes) throws IOException {
        if (null == bytes || bytes.length == 0) {
            stream.write(0);
        } else {
            stream.write(bytes.length);
            stream.write(bytes);
        }
    }

    public NulsDataType getDataType() {
        return dataType;
    }

    public void setDataType(NulsDataType dataType) {
        this.dataType = dataType;
    }
}

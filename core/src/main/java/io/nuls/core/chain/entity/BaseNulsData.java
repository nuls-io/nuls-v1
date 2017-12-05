package io.nuls.core.chain.entity;

import io.nuls.core.crypto.UnsafeByteArrayOutputStream;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.validate.DataValidatorChain;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author Niels
 * @date 2017/10/30
 */
public abstract class BaseNulsData implements Serializable {

    protected NulsDataType dataType;

    protected NulsVersion version;

    private DataValidatorChain validatorChain = new DataValidatorChain();

    public BaseNulsData() {

    }

    public BaseNulsData(NulsByteBuffer buffer) {
        this.parse(buffer);
    }

    public BaseNulsData(short mainVersion, short subVersion) {
        this.version = new NulsVersion(mainVersion, subVersion);
    }

    protected void registerValidator(NulsDataValidator<? extends BaseNulsData> validator) {
        this.validatorChain.addValidator(validator);
    }
    public final int size(){
        int size = 0;
        size += version.size();
        size += this.dataSize();
        return size;
    }
    /**
     * the length of the object itself
     * @return
     */
    protected abstract int dataSize();

    /**
     * First, serialize the version field
     *
     * @return
     */
    public final byte[] serialize() throws IOException {
        this.verify();
        ByteArrayOutputStream bos = null;
        try {
            bos = new UnsafeByteArrayOutputStream(size());
            NulsOutputStreamBuffer buffer = new NulsOutputStreamBuffer(bos);
            buffer.writeShort(version.getVersion());
            serializeToStream(buffer);
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

    /**
     * serialize important field
     * @param stream
     * @throws IOException
     */
    public abstract void serializeToStream(NulsOutputStreamBuffer stream) throws IOException;

    public final void parse(NulsByteBuffer byteBuffer){
        this.version = new NulsVersion(byteBuffer.readShort());
        this.parse(byteBuffer);
    }
    /**
     * bytes to object
     * @param byteBuffer
     */
    protected abstract void parseObject(NulsByteBuffer byteBuffer);

    /**
     * @throws NulsException
     */
    public final void verify() throws NulsVerificationException {
        ValidateResult result = this.validatorChain.startDoValidator(this);
        if (!result.isSeccess()) {
            throw new NulsVerificationException(result.getMessage());
        }
    }

    public NulsDataType getDataType() {
        return dataType;
    }

    public void setDataType(NulsDataType dataType) {
        this.dataType = dataType;
    }


    public NulsVersion getVersion() {
        return version;
    }

    public void setVersion(NulsVersion version) {
        this.version = version;
    }

    public void setVersionBy(short main, short sub) {
        version.setVersionBy(main, sub);
    }
}

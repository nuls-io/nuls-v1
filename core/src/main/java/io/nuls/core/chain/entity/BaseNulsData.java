package io.nuls.core.chain.entity;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.crypto.UnsafeByteArrayOutputStream;
import io.nuls.core.exception.NulsException;
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

    public BaseNulsData(NulsByteBuffer buffer) throws NulsException {
        this.parse(buffer);
    }

    public BaseNulsData(short mainVersion, short subVersion) {
        this.version = new NulsVersion(mainVersion, subVersion);
    }

    protected void registerValidator(NulsDataValidator<? extends BaseNulsData> validator) {
        this.validatorChain.addValidator(validator);
    }

    public abstract int size();

    /**
     * First, serialize the version field
     *
     * @return
     */
    public final byte[] serialize() throws IOException {
        ByteArrayOutputStream bos = null;
        try {
            bos = new UnsafeByteArrayOutputStream(size());
            NulsOutputStreamBuffer buffer = new NulsOutputStreamBuffer(bos);
            if (size() == 0) {
                bos.write(NulsConstant.PLACE_HOLDER);
            } else {
                serializeToStream(buffer);
            }
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

    public final void parse(byte[] bytes) throws NulsException {
        if (bytes == null || bytes.length == 0 || NulsConstant.PLACE_HOLDER == bytes[0]) {
            return;
        }
        this.parse(new NulsByteBuffer(bytes));
    }

    /**
     * serialize important field
     *
     * @param stream
     * @throws IOException
     */
    protected abstract void serializeToStream(NulsOutputStreamBuffer stream) throws IOException;

    protected abstract void parse(NulsByteBuffer byteBuffer) throws NulsException;

    /**
     * @throws NulsException
     */
    public final ValidateResult verify() {
        return this.validatorChain.startDoValidator(this);
    }

    public NulsDataType getDataType() {
        return dataType;
    }

    public void setDataType(NulsDataType dataType) {
        this.dataType = dataType;
    }


    public NulsVersion getVersion() {
        if (null == version) {
            version = new NulsVersion((short) 1, (short) 1);
        }
        return version;
    }

    public void setVersion(NulsVersion version) {
        this.version = version;
    }

    public void setVersionBy(short main, short sub) {
        version.setVersionBy(main, sub);
    }
}

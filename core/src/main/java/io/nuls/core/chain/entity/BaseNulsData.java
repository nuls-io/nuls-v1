package io.nuls.core.chain.entity;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.core.validate.DataValidatorChain;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.core.crypto.UnsafeByteArrayOutputStream;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @author Niels
 * @date 2017/10/30
 */
public abstract class BaseNulsData implements Serializable {

    protected NulsDataType dataType;

    /**
     * The version number is 2 bytes
     */
    protected short version;

    public static final short MAX_VERSION = 32767;

    public static final short MAX_MAIN_VERSION = 15;

    public static final short MAX_SUB_VERSION = 2047;

    private DataValidatorChain validatorChain = new DataValidatorChain();

    public BaseNulsData() {

    }

    /**
     * version = mainVersion << 11 + subVersion
     *
     * @param mainVersion
     * @param subVersion
     */
    public BaseNulsData(short mainVersion, short subVersion) {
        setVersionBy(mainVersion, subVersion);
    }

    protected void registerValidator(NulsDataValidator<? extends BaseNulsData> validator) {
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

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        if (version > MAX_VERSION) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        this.version = version;
    }

    public void setVersionBy(short main, short sub) {
        if (main <= 0 || main > MAX_MAIN_VERSION) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        if (main <= 0 || sub > MAX_SUB_VERSION) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
        main = (short) (main << 11);
        version = (short) (main + sub);
    }

    public short getMainVersion() {
        String str = Integer.toBinaryString(version);
        String mainStr = str.substring(0, str.length() - 11);
        return Short.valueOf(mainStr, 2);

    }

    public short getSubVersion() {
        String str = Integer.toBinaryString(version);
        String subStr = str.substring(str.length() - 11);
        return Short.valueOf(subStr, 2);
    }

    public String getStringVersion() {
        return getMainVersion() + "." + getSubVersion();
    }

}

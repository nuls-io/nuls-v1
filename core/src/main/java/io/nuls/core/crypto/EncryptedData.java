package io.nuls.core.crypto;

import java.util.Arrays;
import java.util.Objects;

/**
 * 加密后的数据
 * @author ln
 *
 */
public final class EncryptedData {
    private byte[] initialisationVector;		//iv
    private byte[] encryptedBytes;			//加密结果

    public EncryptedData(byte[] initialisationVector, byte[] encryptedBytes) {
        this.initialisationVector = Arrays.copyOf(initialisationVector, initialisationVector.length);
        this.encryptedBytes = Arrays.copyOf(encryptedBytes, encryptedBytes.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EncryptedData other = (EncryptedData) o;
        return Arrays.equals(encryptedBytes, other.encryptedBytes) && Arrays.equals(initialisationVector, other.initialisationVector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(encryptedBytes), Arrays.hashCode(initialisationVector));
    }

    @Override
    public String toString() {
        return "EncryptedData [initialisationVector=" + Arrays.toString(initialisationVector)
            + ", encryptedPrivateKey=" + Arrays.toString(encryptedBytes) + "]";
    }

	public byte[] getInitialisationVector() {
		return initialisationVector;
	}

	public byte[] getEncryptedBytes() {
		return encryptedBytes;
	}

	public void setInitialisationVector(byte[] initialisationVector) {
		this.initialisationVector = initialisationVector;
	}

	public void setEncryptedBytes(byte[] encryptedBytes) {
		this.encryptedBytes = encryptedBytes;
	}
}

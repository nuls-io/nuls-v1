package io.nuls.core.crypto;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECPoint;

/**
 * Created by facjas on 2017/11/20.
 */
public class SM2Result {
    public SM2Result() {
    }

    public BigInteger r;
    public BigInteger s;
    public BigInteger R;

    public byte[] sa;
    public byte[] sb;
    public byte[] s1;
    public byte[] s2;

    public ECPoint keyra;
    public ECPoint keyrb;
}

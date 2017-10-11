package io.nuls.crypto.sm;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECPoint;

public class SM2Result 
{
	public SM2Result() {
	}

	// ǩ��/��ǩ
	public BigInteger r;
	public BigInteger s;
	public BigInteger R;

	// ��Կ����
	public byte[] sa;
	public byte[] sb;
	public byte[] s1;
	public byte[] s2;

	public ECPoint keyra;
	public ECPoint keyrb;
}

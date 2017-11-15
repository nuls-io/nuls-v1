package io.nuls.core.crypto;

import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.DERSequenceGenerator;
import org.spongycastle.asn1.DLSequence;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.*;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointCombMultiplier;
import org.spongycastle.math.ec.FixedPointUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * 椭圆曲线加密
 *
 * @author ln
 */
public class ECKey {

    private static final Logger log = LoggerFactory.getLogger(ECKey.class);

    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    public static final ECDomainParameters CURVE;
    public static final BigInteger HALF_CURVE_ORDER;

    private static final SecureRandom secureRandom;    //随机种子

    static {
        if (Utils.isAndroidRuntime())
            new LinuxSecureRandom();

        FixedPointUtil.precompute(CURVE_PARAMS.getG(), 12);
        CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
                CURVE_PARAMS.getH());
        HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
        secureRandom = new SecureRandom();
    }

    protected final BigInteger priv;    // 私匙
    private final ECPoint pub;            //公匙

    protected EncryptedData encryptedPrivateKey;

    protected long creationTimeSeconds;

    public ECKey() {
        this(secureRandom);
    }

    public ECKey(SecureRandom secureRandom) {
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, secureRandom);
        generator.init(keygenParams);
        AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
        ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
        ECPublicKeyParameters pubParams = (ECPublicKeyParameters) keypair.getPublic();
        priv = privParams.getD();
        pub = pubParams.getQ();
        creationTimeSeconds = System.currentTimeMillis();
    }

    /**
     * 根据私匙和公匙创建
     *
     * @param priv
     * @param pub
     */
    private ECKey(BigInteger priv, ECPoint pub) {
        if (priv != null) {
            //私匙不应该是0和1
            Utils.checkState(!priv.equals(BigInteger.ZERO));
            Utils.checkState(!priv.equals(BigInteger.ONE));
        }
        this.priv = priv;
        this.pub = Utils.checkNotNull(pub);
        creationTimeSeconds = System.currentTimeMillis();
    }

    /**
     * 根据私匙创建密码器
     *
     * @param privKey
     * @return ECKey
     */
    public static ECKey fromPrivate(BigInteger privKey) {
        return fromPrivate(privKey, true);
    }

    /**
     * 根据私匙创建密码器，并选择是否压缩公匙
     *
     * @param privKey
     * @param compressed
     * @return ECKey
     */
    public static ECKey fromPrivate(BigInteger privKey, boolean compressed) {

        ECPoint point = publicPointFromPrivate(privKey);
        return new ECKey(privKey, getPointWithCompression(point, compressed));
    }

    /**
     * 只有公匙
     *
     * @param pubKey
     * @return ECKey
     */
    public static ECKey fromPublicOnly(byte[] pubKey) {
        return new ECKey(null, CURVE.getCurve().decodePoint(pubKey));
    }

    /**
     * 只有公匙
     *
     * @param pub
     * @return ECKey
     */
    public static ECKey fromPublicOnly(ECPoint pub) {
        return new ECKey(null, pub);
    }

    /**
     * 根据私匙计算公匙
     *
     * @param privKey
     * @return ECKey
     */
    public static ECPoint publicPointFromPrivate(BigInteger privKey) {
        if (privKey.bitLength() > CURVE.getN().bitLength()) {
            privKey = privKey.mod(CURVE.getN());
        }
        return new FixedPointCombMultiplier().multiply(CURVE.getG(), privKey);
    }

    /**
     * 通过加密的私钥创建ECKey
     *
     * @param encryptedPrivateKey
     * @param pubKey
     * @return ECKey
     */
    public static ECKey fromEncrypted(EncryptedData encryptedPrivateKey, byte[] pubKey) {
        ECKey key = fromPublicOnly(pubKey);
        AssertUtil.canNotEmpty(encryptedPrivateKey, "encryptedPrivateKey can not null!");
        key.encryptedPrivateKey = encryptedPrivateKey;
        return key;
    }

    /**
     * 获取公匙内容
     *
     * @return byte[]
     */
    public byte[] getPubKey(boolean compressed) {
        return pub.getEncoded(compressed);
    }

    /**
     * 获取公匙内容,默认的公匙是压缩的
     *
     * @return byte[]
     */
    public byte[] getPubKey() {
        return getPubKey(true);
    }

    /**
     * 获取私匙对应的随机数
     *
     * @return BigInteger
     */
    public BigInteger getPrivKey() {
        if (priv == null)
            throw new MissingPrivateKeyException();
        return priv;
    }

    /**
     * 获取私匙的内容
     *
     * @return byte[]
     */
    public byte[] getPrivKeyBytes() {
        return getPrivKey().toByteArray();
    }

    /**
     * 获取私匙转16进制后的字符串
     *
     * @return String
     */
    public String getPrivateKeyAsHex() {
        return Hex.encode(getPrivKeyBytes());
    }

    /**
     * 获取公匙转16进制后的字符串，压缩过的
     *
     * @return String
     */
    public String getPublicKeyAsHex() {
        return getPublicKeyAsHex(true);
    }

    /**
     * 获取公匙转16进制后的字符串
     *
     * @param compressed
     * @return String
     */
    public String getPublicKeyAsHex(boolean compressed) {
        return Hex.encode(getPubKey(compressed));
    }

    /*
     * 压缩公匙
     */
    @SuppressWarnings("deprecation")
    private static ECPoint getPointWithCompression(ECPoint point, boolean compressed) {
        if (point.isCompressed() == compressed)
            return point;
        point = point.normalize();
        BigInteger x = point.getAffineXCoord().toBigInteger();
        BigInteger y = point.getAffineYCoord().toBigInteger();
        return CURVE.getCurve().createPoint(x, y, compressed);
    }

    /**
     * 验证签名
     */
    public static boolean verify(byte[] data, ECDSASignature signature, byte[] pub) {
        ECDSASigner signer = new ECDSASigner();
        ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(pub), CURVE);
        signer.init(false, params);
        try {
            return signer.verifySignature(data, signature.r, signature.s);
        } catch (NullPointerException e) {
            log.error("Caught NPE inside bouncy castle", e);
            return false;
        }
    }

    /**
     * 验证签名
     */
    public static boolean verify(byte[] data, byte[] signature, byte[] pub) {
        return verify(data, ECDSASignature.decodeFromDER(signature), pub);
    }

    /**
     * 验证签名
     */
    public boolean verify(byte[] hash, byte[] signature) {
        return ECKey.verify(hash, signature, getPubKey());
    }

    public static class MissingPrivateKeyException extends RuntimeException {
        private static final long serialVersionUID = 2789844760773725676L;
    }

    public static class ECDSASignature {
        public final BigInteger r, s;

        public ECDSASignature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        public byte[] encodeToDER() {
            try {
                return derByteStream().toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);  // Cannot happen.
            }
        }

        public static ECDSASignature decodeFromDER(byte[] bytes) {
            ASN1InputStream decoder = null;
            try {
                decoder = new ASN1InputStream(bytes);
                DLSequence seq = (DLSequence) decoder.readObject();
                if (seq == null)
                    throw new RuntimeException("Reached past end of ASN.1 stream.");
                ASN1Integer r, s;
                try {
                    r = (ASN1Integer) seq.getObjectAt(0);
                    s = (ASN1Integer) seq.getObjectAt(1);
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException(e);
                }
                // OpenSSL deviates from the DER spec by interpreting these values as unsigned, though they should not be
                // Thus, we always use the positive versions. See: http://r6.ca/blog/20111119T211504Z.html
                return new ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
            } catch (IOException e) {
                Log.error(e);
                throw new RuntimeException(e);
            } finally {
                if (decoder != null)
                    try {
                        decoder.close();
                    } catch (IOException x) {
                    }
            }
        }

        protected ByteArrayOutputStream derByteStream() throws IOException {
            // Usually 70-72 bytes.
            ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
            DERSequenceGenerator seq = new DERSequenceGenerator(bos);
            seq.addObject(new ASN1Integer(r));
            seq.addObject(new ASN1Integer(s));
            seq.close();
            return bos;
        }

        public boolean isCanonical() {
            return s.compareTo(HALF_CURVE_ORDER) <= 0;
        }

        /**
         * Will automatically adjust the S component to be less than or equal to half the curve order, if necessary.
         * This is required because for every signature (r,s) the signature (r, -s (mod N)) is a valid signature of
         * the same message. However, we dislike the ability to modify the bits of a Bitcoin transaction after it's
         * been signed, as that violates various assumed invariants. Thus in future only one of those forms will be
         * considered legal and the other will be banned.
         */
        public ECDSASignature toCanonicalised() {
            if (!isCanonical()) {
                // The order of the curve is the number of valid points that exist on that curve. If S is in the upper
                // half of the number of valid points, then bring it back to the lower half. Otherwise, imagine that
                //    N = 10
                //    s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
                //    10 - 8 == 2, giving us always the latter solution, which is canonical.
                return new ECDSASignature(r, CURVE.getN().subtract(s));
            } else {
                return this;
            }
        }
    }

    /**
     * 签名
     *
     * @param hash
     * @return ECDSASignature
     */
    public ECDSASignature sign(Sha256Hash hash) {
        return sign(hash, null);
    }

    public ECDSASignature sign(Sha256Hash hash, KeyParameter aesKey) {
        return doSign(hash, priv);
    }

    protected ECDSASignature doSign(Sha256Hash input, BigInteger privateKeyForSigning) {
        Utils.checkNotNull(privateKeyForSigning);
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKeyForSigning, CURVE);
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(input.getBytes());
        return new ECDSASignature(components[0], components[1]).toCanonicalised();
    }

    /**
     * 是否包含私匙
     *
     * @return boolean
     */
    public boolean hasPrivKey() {
        return priv != null;
    }

    /**
     * 公钥是否压缩
     *
     * @return boolean
     */
    public boolean isCompressed() {
        return pub.isCompressed();
    }


    /**
     * 设置创建时间
     *
     * @param creationTimeSeconds
     */
    public void setCreationTimeSeconds(long creationTimeSeconds) {
        this.creationTimeSeconds = creationTimeSeconds;
    }

    public long getCreationTimeSeconds() {
        return creationTimeSeconds;
    }

    public EncryptedData getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(EncryptedData encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }
}

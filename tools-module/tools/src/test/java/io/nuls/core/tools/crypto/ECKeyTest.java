package io.nuls.core.tools.crypto;

import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Base64;

import java.security.SignatureException;

public class ECKeyTest {

    @Test
    public void signedMessageToKeyTest() throws SignatureException {
        ECKey key = new ECKey();
        System.out.println(key.getPrivateKeyAsHex());
        System.out.println(key.getPublicKeyAsHex());
        String message = "Nuls Signed Message:\nHello,I'am test case！@##$%998877";
        String signatureBase64 = key.signMessage(message,null);
        System.out.println(signatureBase64);
        System.out.println(Base64.decode(signatureBase64).length);
        ECKey recoveryECKey = ECKey.signedMessageToKey(message, signatureBase64);
        Assert.assertEquals(recoveryECKey.getPublicKeyAsHex(),key.getPublicKeyAsHex());
    }

}

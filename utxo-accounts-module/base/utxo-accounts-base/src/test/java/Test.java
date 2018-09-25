import io.nuls.core.tools.log.Log;
import io.nuls.kernel.exception.NulsException;

import io.nuls.kernel.script.TransactionSignature;
import io.nuls.kernel.utils.AddressTool;

import java.util.Arrays;

public class Test {
    public static void main(String []args){
        String str="2102ac3f8f73d6a0f23d9ecd797fb14f96e82a032bb972f1ee39b4bf443d07a5d55e004630440220668f5538cdbbd90e3dfc1ea1197ddb03cd8628dedfc77afad795140f3867560302202453d0991dc254ae2a5f40f915ef22e0c0e9ba315516d22eaa2e002fc9239147";
        TransactionSignature signature = new TransactionSignature();
        try {
            signature.parse(str.getBytes(),0);
        } catch (NulsException e) {
            Log.error(e);
        }

        signature.getP2PHKSignatures().forEach(p -> {
            System.out.println(Arrays.toString(p.getPublicKey()));
        });
    }
}

package io.nuls.contract.entity.txdata;

import io.nuls.contract.util.ContractUtil;
import io.nuls.db.service.DBService;
import io.nuls.db.service.impl.LevelDBServiceImpl;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.ledger.util.LedgerUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class CallContractDataTest {

    private DBService dbService;

    private String area = "temps";

    private CallContractData data;

    @Before
    public void setUp() throws Exception {
        System.setProperty("protostuff.runtime.allow_null_array_element", "true");
        //System.setProperty("protostuff.runtime.auto_load_polymorphic_classes", "false");
        dbService = new LevelDBServiceImpl();
        dbService.createArea(area);
        initCallData();
    }

    private void initCallData() {
        String sender = "Nsdz8mKKFMehRDVRZFyXNuuenugUYM7M";
        String contract = "NseKznzD5fTe7LyrSa365Rko8UKmCE17";
        String[] args1 = new String[] {"a", "b", null, "d"};
        //String[][] args = new String[][]{{"NseKznzD5fTe7LyrSa365Rko8UKmCE17"}, {"single"}, null, {"0"}};
        //String[][] args = new String[][]{{"NseKznzD5fTe7LyrSa365Rko8UKmCE17"}, null, null, {"0"}};
        String[][] args = ContractUtil.twoDimensionalArray(args1);
        data = new CallContractData();
        data.setSender(AddressTool.getAddress(sender));
        data.setContractAddress(AddressTool.getAddress(contract));
        data.setValue(0);
        data.setGasLimit(2342);
        data.setPrice(24);
        data.setMethodName("callContract");
        data.setMethodDesc("return void");
        data.setArgsCount((byte) args.length);
        data.setArgs(args);
    }


    @Test
    public void test() {
        try {
            System.out.println(data.size());
            byte[] dataSe = data.serialize();
            CallContractData dataConvert = new CallContractData();
            dataConvert.parse(dataSe, 0);

            byte[] serialize = dataConvert.serialize();
            CallContractData dataConvert1 = new CallContractData();
            dataConvert1.parse(serialize, 0);


            System.out.println(dataConvert.toString());

            String[][] strings = dataConvert.getArgs();

            for(String[] bbb : strings)
                System.out.println(Arrays.toString(bbb));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void test1() {
        //dbService
        try {
            byte[] key = LedgerUtil.asBytes("aasdasdasdasda");
            dbService.putModel(area, key, data);
            CallContractData dataConvert = dbService.getModel(area, key, CallContractData.class);

            System.out.println(dataConvert.toString());

            String[][] strings = dataConvert.getArgs();

            for(String[] bbb : strings)
                System.out.println(Arrays.toString(bbb));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    //@Override
    //public String toString() {
    //    return "CallContractData{" +
    //            "sender=" + AddressTool.getStringAddressByBytes(sender) +
    //            ",\n contractAddress=" + AddressTool.getStringAddressByBytes(contractAddress) +
    //            ",\n value=" + value +
    //            ",\n gasLimit=" + gasLimit +
    //            ",\n price=" + price +
    //            ",\n methodName='" + methodName + '\'' +
    //            ",\n methodDesc='" + methodDesc + '\'' +
    //            ",\n argsCount=" + argsCount +
    //            //", args=" + Arrays.toString(args) +
    //            '}';
    //}
}
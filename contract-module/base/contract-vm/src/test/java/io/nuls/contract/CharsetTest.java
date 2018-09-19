package io.nuls.contract;

import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeAddress;
import io.nuls.contract.vm.program.ProgramCall;
import io.nuls.contract.vm.program.ProgramCreate;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.contract.vm.program.impl.ProgramExecutorImpl;
import io.nuls.db.service.DBService;
import io.nuls.db.service.impl.LevelDBServiceImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CharsetTest {

    private VMContext vmContext;
    private DBService dbService;
    private ProgramExecutor programExecutor;

    private static final String ADDRESS = "Nse5j2pZLWHH9iF4GW9Cja2sFyVXQtDX";
    private static final String SENDER = "Nse5gVscugsS8C1S1svh6CMcpoVWewpa";
    private static final String BUYER = "NsdwCuCKs2AXFfUT7PxXXJPm2XxybX6H";

    @Before
    public void setUp() {
        dbService = new LevelDBServiceImpl();
        programExecutor = new ProgramExecutorImpl(vmContext, dbService);
    }

    @Test
    public void testCreate() throws IOException {
        InputStream in = new FileInputStream(ContractTest.class.getResource("/charset_contract").getFile());
        //InputStream in = new FileInputStream(ContractTest.class.getResource("/").getFile() + "../contract.jar");
        byte[] contractCode = IOUtils.toByteArray(in);

        ProgramCreate programCreate = new ProgramCreate();
        programCreate.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCreate.setSender(NativeAddress.toBytes(SENDER));
        programCreate.setPrice(1);
        programCreate.setGasLimit(1000000);
        programCreate.setNumber(1);
        programCreate.setContractCode(contractCode);
        //programCreate.args();
        System.out.println(programCreate);

        byte[] prevStateRoot = Hex.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.create(programCreate);
        track.commit();

        System.out.println(programResult);
        System.out.println("stateRoot: " + Hex.toHexString(track.getRoot()));
        System.out.println();
    }

    @Test
    public void testCall() throws IOException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("setData");
        programCall.setMethodDesc("");
        programCall.args("中文测试，看看");
        System.out.println(programCall);

        byte[] prevStateRoot = Hex.decode("d38136d91101bdf28e3ca58555186e7969e7672cbddf7fbd8490cdc1ce3a8a21");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + Hex.toHexString(track.getRoot()));
        System.out.println();
        //c334818f7247f1cf9c46246938845abd5005a9ed2b2f55957ea1694e1310d523

        programCall.setMethodName("getData");
        programCall.setMethodDesc("");
        programCall.setArgs(new String[]{});
        System.out.println(programCall);

        track = programExecutor.begin(track.getRoot());
        programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + Hex.toHexString(track.getRoot()));
        System.out.println();

        programCall.setMethodName("getCharset");
        programCall.setMethodDesc("");
        programCall.setArgs(new String[]{});
        System.out.println(programCall);

        track = programExecutor.begin(track.getRoot());
        programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + Hex.toHexString(track.getRoot()));
        System.out.println();
    }

}

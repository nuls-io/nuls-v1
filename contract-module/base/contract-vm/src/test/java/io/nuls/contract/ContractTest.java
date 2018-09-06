package io.nuls.contract;

import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeAddress;
import io.nuls.contract.vm.program.*;
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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ContractTest {

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
        InputStream in = new FileInputStream(ContractTest.class.getResource("/token_contract").getFile());
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
    public void testCallMuch() throws IOException {
        byte[] prevStateRoot = Hex.decode("cbf81c8098255b91ed834ee6e354c115718bc6cc31032373f8482a3982961fd2");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            ProgramCall programCall = new ProgramCall();
            programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
            programCall.setSender(NativeAddress.toBytes(SENDER));
            programCall.setPrice(1);
            programCall.setGasLimit(1000000);
            programCall.setNumber(1);
            programCall.setMethodName("mint");
            programCall.setMethodDesc("");
            programCall.args(BUYER, "1000");
            System.out.println(programCall);

            ProgramExecutor track = programExecutor.begin(prevStateRoot);
            ProgramResult programResult = track.call(programCall);
            track.commit();

            prevStateRoot = track.getRoot();
            System.out.println(programResult);
            System.out.println("pierre - stateRoot: " + Hex.toHexString(track.getRoot()));
            System.out.println();
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    @Test
    public void testCall() throws IOException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("mint");
        programCall.setMethodDesc("");
        programCall.args(BUYER, "1000");
        System.out.println(programCall);

        byte[] prevStateRoot = Hex.decode("cbf81c8098255b91ed834ee6e354c115718bc6cc31032373f8482a3982961fd2");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + Hex.toHexString(track.getRoot()));
        System.out.println();

        programCall.setMethodName("balanceOf");
        programCall.setMethodDesc("");
        programCall.args(BUYER);
        System.out.println(programCall);

        track = programExecutor.begin(track.getRoot());
        programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + Hex.toHexString(track.getRoot()));
        System.out.println();
    }

    @Test
    public void testAddBalance() throws IOException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("_payable");
        programCall.setMethodDesc("()V");
        programCall.setValue(new BigInteger("100"));
        System.out.println(programCall);

        byte[] prevStateRoot = Hex.decode("cbf81c8098255b91ed834ee6e354c115718bc6cc31032373f8482a3982961fd2");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + Hex.toHexString(track.getRoot()));
        System.out.println();
    }

    @Test
    public void testStop() throws IOException {
        byte[] prevStateRoot = Hex.decode("cbf81c8098255b91ed834ee6e354c115718bc6cc31032373f8482a3982961fd2");
        byte[] address = NativeAddress.toBytes(ADDRESS);
        byte[] sender = NativeAddress.toBytes(SENDER);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.stop(address, sender);
        track.commit();

        System.out.println(programResult);
        System.out.println("stateRoot: " + Hex.toHexString(track.getRoot()));
        System.out.println();
    }

    @Test
    public void testStatus() throws IOException {
        byte[] prevStateRoot = Hex.decode("cbf81c8098255b91ed834ee6e354c115718bc6cc31032373f8482a3982961fd2");
        byte[] address = NativeAddress.toBytes(ADDRESS);
        byte[] sender = NativeAddress.toBytes(SENDER);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramStatus programStatus = track.status(address);

        System.out.println(programStatus);
        System.out.println("stateRoot: " + Hex.toHexString(track.getRoot()));
        System.out.println();
    }

    @Test
    public void testTransactions() {
        List<ProgramCall> transactions = new ArrayList<>();

        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("balanceOf");
        programCall.setMethodDesc("");
        programCall.args(ADDRESS);
        System.out.println(programCall);
        transactions.add(programCall);

        ProgramCall programCall1 = new ProgramCall();
        programCall1.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall1.setSender(NativeAddress.toBytes(SENDER));
        programCall1.setPrice(0);
        programCall1.setGasLimit(1000000);
        programCall1.setNumber(1);
        programCall1.setMethodName("balanceOf");
        programCall1.setMethodDesc("");
        programCall1.args(ADDRESS);
        System.out.println(programCall1);
        transactions.add(programCall1);

        byte[] prevStateRoot = Hex.decode("cbf81c8098255b91ed834ee6e354c115718bc6cc31032373f8482a3982961fd2");

        for (ProgramCall transaction : transactions) {
            ProgramExecutor track = programExecutor.begin(prevStateRoot);
            ProgramResult programResult = track.call(transaction);
            track.commit();

            prevStateRoot = track.getRoot();

            System.out.println("stateRoot: " + Hex.toHexString(track.getRoot()));
            System.out.println();
        }

    }

    @Test
    public void testMethod() throws IOException {
        byte[] prevStateRoot = Hex.decode("cbf81c8098255b91ed834ee6e354c115718bc6cc31032373f8482a3982961fd2");
        byte[] address = NativeAddress.toBytes(ADDRESS);
        byte[] sender = NativeAddress.toBytes(SENDER);

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        List<ProgramMethod> methods = track.method(address);
        //track.commit();

        for (ProgramMethod method : methods) {
            System.out.println(method);
        }
    }

    @Test
    public void testJarMethod() throws IOException {
        InputStream in = new FileInputStream(ContractTest.class.getResource("/token_contract").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);

        List<ProgramMethod> methods = programExecutor.jarMethod(contractCode);

        for (ProgramMethod method : methods) {
            System.out.println(method);
        }
    }

    @Test
    public void testTransfer() throws IOException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("transfer");
        programCall.setMethodDesc("");
        programCall.args(BUYER, "-1000");
        System.out.println(programCall);

        byte[] prevStateRoot = Hex.decode("cbf81c8098255b91ed834ee6e354c115718bc6cc31032373f8482a3982961fd2");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + Hex.toHexString(track.getRoot()));
        System.out.println();

        programCall.setMethodName("balanceOf");
        programCall.setMethodDesc("");
        programCall.args(BUYER);
        System.out.println(programCall);

        track = programExecutor.begin(track.getRoot());
        programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + Hex.toHexString(track.getRoot()));
        System.out.println();
    }

    @Test
    public void testGetter() throws IOException {
        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        //programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(1000000);
        programCall.setNumber(1);
        programCall.setMethodName("getName");
        programCall.setMethodDesc("");
        programCall.setValue(new BigInteger("0"));
        System.out.println(programCall);

        byte[] prevStateRoot = Hex.decode("cbf81c8098255b91ed834ee6e354c115718bc6cc31032373f8482a3982961fd2");

        ProgramExecutor track = programExecutor.begin(prevStateRoot);
        ProgramResult programResult = track.call(programCall);
        track.commit();

        System.out.println(programResult);
        System.out.println("pierre - stateRoot: " + Hex.toHexString(track.getRoot()));
        System.out.println();
    }

}

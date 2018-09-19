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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class VoteTest {

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
    public void testBatchBlock() throws IOException {
        byte[] prevStateRoot = Hex.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421");
        List transactions = createTransactions();
        prevStateRoot = testBatch(prevStateRoot, transactions);
        System.out.println("第1个块");
        for (int i = 0; i < 100; i++) {
            transactions = transactions();
            prevStateRoot = testBatch(prevStateRoot, transactions);
            System.out.println("第" + (i + 2) + "个块");
        }
    }

    @Test
    public void testOneBlock() throws IOException {
        byte[] prevStateRoot = Hex.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421");
        List transactions = createTransactions();
        prevStateRoot = testOne(prevStateRoot, transactions);
        System.out.println("第1个块");
        for (int i = 0; i < 100; i++) {
            transactions = transactions();
            prevStateRoot = testOne(prevStateRoot, transactions);
            System.out.println("第" + (i + 2) + "个块");
        }
    }

    public byte[] testBatch(byte[] prevStateRoot, List transactions) throws IOException {

        long start = System.currentTimeMillis();

        ProgramExecutor track = programExecutor.begin(prevStateRoot);

        for (int i = 0; i < transactions.size(); i++) {
            long transactionStart = System.currentTimeMillis();
            Object transaction = transactions.get(i);

            if (transaction instanceof ProgramCreate) {

                ProgramCreate create = (ProgramCreate) transaction;
                ProgramExecutor txTrack = track.startTracking();
                ProgramResult programResult = txTrack.create(create);
                txTrack.commit();

                System.out.println(programResult);
            } else if (transaction instanceof ProgramCall) {

                ProgramCall call = (ProgramCall) transaction;
                ProgramExecutor txTrack = track.startTracking();
                ProgramResult programResult = txTrack.call(call);
                txTrack.commit();

                System.out.println(programResult);
            }
            System.out.println("交易" + i + ", 耗时：" + (System.currentTimeMillis() - transactionStart) + "ms");
        }

        long time = System.currentTimeMillis() - start;
        System.out.println("完成" + transactions.size() + "笔交易， 耗时：" + time + "ms，平均：" + (time / transactions.size()) + "ms");

        System.out.println("提交");
        long commitStart = System.currentTimeMillis();
        track.commit();
        System.out.println("提交耗时：" + (System.currentTimeMillis() - commitStart) + "ms");

        byte[] root = track.getRoot();

        System.out.println("stateRoot: " + Hex.toHexString(root));
        System.out.println("总耗时：" + (System.currentTimeMillis() - start) + "ms");
        return root;
    }

    public byte[] testOne(byte[] prevStateRoot, List transactions) throws IOException {

        long start = System.currentTimeMillis();

        //ProgramExecutor track = programExecutor.begin(prevStateRoot);

        for (int i = 0; i < transactions.size(); i++) {
            long transactionStart = System.currentTimeMillis();
            Object transaction = transactions.get(i);

            if (transaction instanceof ProgramCreate) {

                ProgramCreate create = (ProgramCreate) transaction;
                //ProgramExecutor txTrack = track.startTracking();
                ProgramExecutor track = programExecutor.begin(prevStateRoot);
                ProgramResult programResult = track.create(create);
                track.commit();

                System.out.println(programResult);
                prevStateRoot = track.getRoot();
                System.out.println("stateRoot: " + Hex.toHexString(prevStateRoot));
            } else if (transaction instanceof ProgramCall) {

                ProgramCall call = (ProgramCall) transaction;
                //ProgramExecutor txTrack = track.startTracking();
                ProgramExecutor track = programExecutor.begin(prevStateRoot);
                ProgramResult programResult = track.call(call);
                track.commit();

                System.out.println(programResult);
                prevStateRoot = track.getRoot();
                System.out.println("stateRoot: " + Hex.toHexString(prevStateRoot));
            }
            System.out.println("交易" + i + ", 耗时：" + (System.currentTimeMillis() - transactionStart) + "ms");
        }

        long time = System.currentTimeMillis() - start;
        System.out.println("完成" + transactions.size() + "笔交易， 耗时：" + time + "ms，平均：" + (time / transactions.size()) + "ms");

//        System.out.println("提交");
//        long commitStart = System.currentTimeMillis();
//        track.commit();
//        System.out.println("提交耗时：" + (System.currentTimeMillis() - commitStart) + "ms");

        //System.out.println("stateRoot: " + Hex.toHexString(track.getRoot()));
        //System.out.println("总耗时：" + (System.currentTimeMillis() - start) + "ms");
        return prevStateRoot;
    }

    public List createTransactions() throws IOException {
        List transactions = new ArrayList();

        ProgramCreate programCreate = new ProgramCreate();
        programCreate.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCreate.setSender(NativeAddress.toBytes(SENDER));
        programCreate.setPrice(1);
        programCreate.setGasLimit(1000000);
        programCreate.setNumber(1);
        InputStream in = new FileInputStream(ContractTest.class.getResource("/vote_contract").getFile());
        //InputStream in = new FileInputStream("C:\\workspace\\nuls-vote\\out\\artifacts\\contract\\contract.jar");
        byte[] contractCode = IOUtils.toByteArray(in);
        programCreate.setContractCode(contractCode);
        programCreate.args("10000");

        transactions.add(programCreate);

        return transactions;
    }

    public List transactions() throws IOException {
        List transactions = new ArrayList();

        for (int i = 0; i < 10; i++) {
            ProgramCall programCall = new ProgramCall();
            programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
            programCall.setSender(NativeAddress.toBytes(SENDER));
            programCall.setPrice(1);
            programCall.setGasLimit(200000);
            programCall.setNumber(1);
            programCall.setMethodName("create");
            programCall.setMethodDesc("");
            programCall.setValue(new BigInteger("10000"));
            String[][] args = new String[][]{
                    {"投票"}, {"这是一个投票"}, {"选项1", "选项2", "选项3"},
                    {"1535012808000"}, {"1545197500000"}, {"true"}, {"3"}, {"true"}
            };
            programCall.setArgs(args);

            transactions.add(programCall);
        }

        ProgramCall programCall = new ProgramCall();
        programCall.setContractAddress(NativeAddress.toBytes(ADDRESS));
        programCall.setSender(NativeAddress.toBytes(SENDER));
        programCall.setPrice(1);
        programCall.setGasLimit(200000);
        programCall.setNumber(1);
        programCall.setMethodName("redemption");
        programCall.setMethodDesc("");
        programCall.setValue(new BigInteger("0"));
        String[][] args = new String[][]{
                {"1"}
        };
        programCall.setArgs(args);

        transactions.add(programCall);

        return transactions;
    }

}

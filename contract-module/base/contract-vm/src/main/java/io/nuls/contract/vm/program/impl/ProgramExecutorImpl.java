/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.contract.vm.program.impl;

import io.nuls.contract.entity.BlockHeaderDto;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.VM;
import io.nuls.contract.vm.VMFactory;
import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.ClassCodeLoader;
import io.nuls.contract.vm.code.ClassCodes;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.exception.ErrorException;
import io.nuls.contract.vm.exception.RevertException;
import io.nuls.contract.vm.program.*;
import io.nuls.contract.vm.util.Constants;
import io.nuls.contract.vm.util.JsonUtils;
import io.nuls.db.service.DBService;
import org.apache.commons.lang3.StringUtils;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.DefaultConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Repository;
import org.ethereum.datasource.Source;
import org.ethereum.datasource.leveldb.LevelDbDataSource;
import org.ethereum.db.RepositoryRoot;
import org.ethereum.db.StateSource;
import org.ethereum.util.FastByteComparisons;
import org.ethereum.vm.DataWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class ProgramExecutorImpl implements ProgramExecutor {

    private static final Logger log = LoggerFactory.getLogger(ProgramExecutorImpl.class);

    private final ProgramExecutorImpl parent;

    private final VMContext vmContext;

    private final Source<byte[], byte[]> source;

    private final Repository repository;

    private final byte[] prevStateRoot;

    private final long beginTime;

    private long blockNumber;

    private long currentTime;

    private boolean revert;

    private Thread thread;

    static {
        ClassCodeLoader.init();
        VMFactory.init();
        JsonUtils.init();
    }

    public ProgramExecutorImpl(VMContext vmContext, DBService dbService) {
        this(vmContext, stateSource(dbService), null, null, null);
    }

    private ProgramExecutorImpl(VMContext vmContext, Source<byte[], byte[]> source, Repository repository, byte[] prevStateRoot, Thread thread) {
        this.parent = this;
        this.vmContext = vmContext;
        this.source = source;
        this.repository = repository;
        this.prevStateRoot = prevStateRoot;
        this.beginTime = this.currentTime = System.currentTimeMillis();
        this.thread = thread;
    }

    @Override
    public ProgramExecutor begin(byte[] prevStateRoot) {
        if (log.isDebugEnabled()) {
            log.debug("begin vm root: {}", Hex.toHexString(prevStateRoot));
        }
        Repository repository = new RepositoryRoot(source, prevStateRoot);
        return new ProgramExecutorImpl(vmContext, source, repository, prevStateRoot, Thread.currentThread());
    }

    @Override
    public ProgramExecutor startTracking() {
        checkThread();
        if (log.isDebugEnabled()) {
            log.debug("startTracking");
        }
        Repository track = repository.startTracking();
        return new ProgramExecutorImpl(vmContext, source, track, null, thread);
    }

    @Override
    public void commit() {
        checkThread();
        if (!revert) {
            repository.commit();
            if (prevStateRoot == null) {
                if (parent.blockNumber == 0) {
                    parent.blockNumber = blockNumber;
                }
                if (parent.blockNumber != blockNumber) {
                    throw new RuntimeException("must use the same block number");
                }
            } else {
                if (vmContext != null) {
                    BlockHeaderDto blockHeaderDto;
                    try {
                        blockHeaderDto = vmContext.getBlockHeader(blockNumber);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    byte[] parentHash = Hex.decode(blockHeaderDto.getPreHash());
                    byte[] hash = Hex.decode(blockHeaderDto.getHash());
                    Block block = new Block(parentHash, hash, blockNumber);
                    DefaultConfig.getDefault().blockStore().saveBlock(block, BigInteger.ONE, true);
                    DefaultConfig.getDefault().pruneManager().blockCommitted(block.getHeader());
                }
                CommonConfig.getDefault().dbFlushManager().flush();
            }
            logTime("commit");
        }
    }

    @Override
    public byte[] getRoot() {
        checkThread();
        byte[] root;
        if (!revert) {
            root = repository.getRoot();
        } else {
            root = this.prevStateRoot;
        }
        if (log.isDebugEnabled()) {
            log.debug("end vm root: {}, runtime: {}", Hex.toHexString(root), System.currentTimeMillis() - beginTime);
        }
        return root;
    }

    @Override
    public ProgramResult create(ProgramCreate programCreate) {
        checkThread();
        ProgramInvoke programInvoke = new ProgramInvoke();
        programInvoke.setContractAddress(programCreate.getContractAddress());
        programInvoke.setSender(programCreate.getSender());
        programInvoke.setPrice(programCreate.getPrice());
        programInvoke.setGasLimit(programCreate.getGasLimit());
        programInvoke.setValue(programCreate.getValue() != null ? programCreate.getValue() : BigInteger.ZERO);
        programInvoke.setNumber(programCreate.getNumber());
        programInvoke.setData(programCreate.getContractCode());
        programInvoke.setMethodName("<init>");
        programInvoke.setArgs(programCreate.getArgs() != null ? programCreate.getArgs() : new String[0][0]);
        programInvoke.setEstimateGas(programCreate.isEstimateGas());
        programInvoke.setCreate(true);
        programInvoke.setInternalCall(false);
        return execute(programInvoke);
    }

    @Override
    public ProgramResult call(ProgramCall programCall) {
        checkThread();
        ProgramInvoke programInvoke = new ProgramInvoke();
        programInvoke.setContractAddress(programCall.getContractAddress());
        programInvoke.setSender(programCall.getSender());
        programInvoke.setPrice(programCall.getPrice());
        programInvoke.setGasLimit(programCall.getGasLimit());
        programInvoke.setValue(programCall.getValue() != null ? programCall.getValue() : BigInteger.ZERO);
        programInvoke.setNumber(programCall.getNumber());
        programInvoke.setMethodName(programCall.getMethodName());
        programInvoke.setMethodDesc(programCall.getMethodDesc());
        programInvoke.setArgs(programCall.getArgs() != null ? programCall.getArgs() : new String[0][0]);
        programInvoke.setEstimateGas(programCall.isEstimateGas());
        programInvoke.setCreate(false);
        programInvoke.setInternalCall(programCall.isInternalCall());
        return execute(programInvoke);
    }

    private ProgramResult execute(ProgramInvoke programInvoke) {
        if (programInvoke.getPrice() < 1) {
            return revert("gas price must be greater than zero");
        }
        if (programInvoke.getGasLimit() < 1) {
            return revert("gas must be greater than zero");
        }
        if (programInvoke.getGasLimit() > VM.MAX_GAS) {
            return revert("gas must be less than " + VM.MAX_GAS);
        }
        if (programInvoke.getValue().compareTo(BigInteger.ZERO) < 0) {
            return revert("value can't be less than zero");
        }
        blockNumber = programInvoke.getNumber();

        logTime("start");

        try {
            Map<String, ClassCode> classCodes;
            if (programInvoke.isCreate()) {
                if (programInvoke.getData() == null) {
                    return revert("contract code can't be null");
                }
                classCodes = ClassCodeLoader.loadJarCache(programInvoke.getData());
                logTime("load new code");
                ProgramChecker.check(classCodes);
                logTime("check code");
                AccountState accountState = repository.getAccountState(programInvoke.getContractAddress());
                if (accountState != null) {
                    return revert("contract already exists");
                }
                accountState = repository.createAccount(programInvoke.getContractAddress(), programInvoke.getSender());
                logTime("new account state");
                repository.saveCode(programInvoke.getContractAddress(), programInvoke.getData());
                logTime("save code");
            } else {
                if ("<init>".equals(programInvoke.getMethodName())) {
                    return revert("can't invoke <init> method");
                }
                AccountState accountState = repository.getAccountState(programInvoke.getContractAddress());
                if (accountState == null) {
                    return revert("contract does not exist");
                }
                logTime("load account state");
                if (accountState.getNonce().compareTo(BigInteger.ZERO) <= 0) {
                    return revert("contract has stopped");
                }
                byte[] codes = repository.getCode(programInvoke.getContractAddress());
                classCodes = ClassCodeLoader.loadJarCache(codes);
                logTime("load code");
            }


            VM vm = VMFactory.createVM();
            logTime("load vm");

            vm.heap.loadClassCodes(classCodes);
            vm.methodArea.loadClassCodes(classCodes);

            logTime("load classes");

            ClassCode contractClassCode = getContractClassCode(classCodes);
            String methodDesc = ProgramDescriptors.parseDesc(programInvoke.getMethodDesc());
            MethodCode methodCode = vm.methodArea.loadMethod(contractClassCode.name, programInvoke.getMethodName(), methodDesc);

            if (methodCode == null) {
                return revert(String.format("can't find method %s.%s", programInvoke.getMethodName(), programInvoke.getMethodDesc()));
            }
            if (!methodCode.isPublic) {
                return revert("can only invoke public method");
            }
            if (!methodCode.hasPayableAnnotation() && programInvoke.getValue().compareTo(BigInteger.ZERO) > 0) {
                return revert("not a payable method");
            }
            if (methodCode.argsVariableType.size() != programInvoke.getArgs().length) {
                return revert(String.format("require %s parameters in method [%s%s]",
                        methodCode.argsVariableType.size(), methodCode.name, methodCode.normalDesc));
            }

            logTime("load method");

            BigInteger accountBalance = getAccountBalance(programInvoke.getContractAddress());
            BigInteger vmBalance = repository.getBalance(programInvoke.getContractAddress());
            if (!programInvoke.isInternalCall() && vmBalance.compareTo(accountBalance) != 0) {
                return revert(String.format("balance error: accountBalance=%s, vmBalance=%s", accountBalance, vmBalance));
            }

            logTime("load balance");

            ObjectRef objectRef;
            if (programInvoke.isCreate()) {
                objectRef = vm.heap.newContract(programInvoke.getContractAddress(), contractClassCode, repository);
            } else {
                objectRef = vm.heap.loadContract(programInvoke.getContractAddress(), contractClassCode, repository);
            }

            logTime("load contract ref");

            if (programInvoke.getValue().compareTo(BigInteger.ZERO) > 0) {
                repository.addBalance(programInvoke.getContractAddress(), programInvoke.getValue());
            }
            vm.setProgramExecutor(this);
            vm.setRepository(repository);
            vm.setGas(programInvoke.getGasLimit());
            vm.addGasUsed(programInvoke.getData() == null ? 0 : programInvoke.getData().length);

            logTime("load end");

            vm.run(objectRef, methodCode, vmContext, programInvoke);

            logTime("run");

            ProgramResult programResult = new ProgramResult();
            programResult.setGasUsed(vm.getGasUsed());

            Result vmResult = vm.getResult();
            Object resultValue = vmResult.getValue();
            if (vmResult.isError() || vmResult.isException()) {
                if (resultValue != null && resultValue instanceof ObjectRef) {
                    vm.setResult(new Result());
                    String error = vm.heap.runToString((ObjectRef) resultValue);
                    String stackTrace = vm.heap.stackTrace((ObjectRef) resultValue);
                    programResult.error(error);
                    programResult.setStackTrace(stackTrace);
                } else {
                    programResult.error(null);
                }

                logTime("contract exception");

                this.revert = true;

                programResult.setGasUsed(vm.getGasUsed());

                return programResult;
            }

            repository.increaseNonce(programInvoke.getContractAddress());
            programResult.setNonce(repository.getNonce(programInvoke.getContractAddress()));
            programResult.setTransfers(vm.getTransfers());
            programResult.setEvents(vm.getEvents());
            programResult.setBalance(repository.getBalance(programInvoke.getContractAddress()));

            if (resultValue != null) {
                if (resultValue instanceof ObjectRef) {
                    String result = vm.heap.runToString((ObjectRef) resultValue);
                    programResult.setResult(result);
                } else {
                    programResult.setResult(resultValue.toString());
                }
            }

            if (methodCode.isPublic && methodCode.hasViewAnnotation()) {
                this.revert = true;
                programResult.view();
                programResult.setGasUsed(vm.getGasUsed());
                return programResult;
            }

            logTime("contract return");

            Map<DataWord, DataWord> contractState = vm.heap.contractState();
            logTime("contract state");

            for (Map.Entry<DataWord, DataWord> entry : contractState.entrySet()) {
                DataWord key = entry.getKey();
                DataWord value = entry.getValue();
                repository.addStorageRow(programInvoke.getContractAddress(), key, value);
            }
            logTime("add contract state");

            programResult.setGasUsed(vm.getGasUsed());

            return programResult;
        } catch (ErrorException e) {
            this.revert = true;
            //log.error("", e);
            ProgramResult programResult = new ProgramResult();
            programResult.setGasUsed(e.getGasUsed());
            //programResult.setStackTrace(e.getStackTraceMessage());
            logTime("error");
            return programResult.error(e.getMessage());
        } catch (RevertException e) {
            //log.error("", e);
            return revert(e.getMessage());
            //return revert(e.getMessage(), e.getStackTraceMessage());
        } catch (Exception e) {
            log.error("", e);
            ProgramResult programResult = revert(e.getMessage());
            //programResult.setStackTrace(ExceptionUtils.getStackTrace(e));
            return programResult;
        }
    }

    private ProgramResult revert(String errorMessage) {
        return revert(errorMessage, null);
    }

    private ProgramResult revert(String errorMessage, String stackTrace) {
        this.revert = true;
        ProgramResult programResult = new ProgramResult();
        programResult.setStackTrace(stackTrace);
        logTime("revert");
        return programResult.revert(errorMessage);
    }

    @Override
    public ProgramResult stop(byte[] address, byte[] sender) {
        checkThread();
        AccountState accountState = repository.getAccountState(address);
        if (accountState == null) {
            return revert("can't find contract");
        }
        if (!FastByteComparisons.equal(sender, accountState.getOwner())) {
            return revert("only the owner can stop the contract");
        }
        if (BigInteger.ZERO.compareTo(accountState.getBalance()) != 0) {
            return revert("contract balance is not zero");
        }
        if (BigInteger.ZERO.compareTo(accountState.getNonce()) >= 0) {
            return revert("contract has stopped");
        }

        repository.setNonce(address, BigInteger.ZERO);

        ProgramResult programResult = new ProgramResult();

        return programResult;
    }

    @Override
    public ProgramStatus status(byte[] address) {
        checkThread();
        this.revert = true;
        AccountState accountState = repository.getAccountState(address);
        if (accountState == null) {
            return ProgramStatus.not_found;
        } else {
            BigInteger nonce = repository.getNonce(address);
            if (BigInteger.ZERO.compareTo(nonce) >= 0) {
                return ProgramStatus.stop;
            } else {
                return ProgramStatus.normal;
            }
        }
    }

    @Override
    public List<ProgramMethod> method(byte[] address) {
        checkThread();
        this.revert = true;
        byte[] codes = repository.getCode(address);
        return jarMethod(codes);
    }

    @Override
    public List<ProgramMethod> jarMethod(byte[] jarData) {
        this.revert = true;
        if (jarData == null || jarData.length < 1) {
            return new ArrayList<>();
        }
        Map<String, ClassCode> classCodes = ClassCodeLoader.loadJarCache(jarData);
        return getProgramMethods(classCodes);
    }

    private void checkThread() {
        if (thread == null) {
            throw new RuntimeException("must use the begin method");
        }
        Thread currentThread = Thread.currentThread();
        if (!currentThread.equals(thread)) {
            throw new RuntimeException(String.format("method must be executed in %s, current %s", thread, currentThread));
        }
    }

    private static List<ProgramMethod> getProgramMethods(Map<String, ClassCode> classCodes) {
        List<ProgramMethod> programMethods = getProgramMethodCodes(classCodes).stream().map(methodCode -> {
            ProgramMethod method = new ProgramMethod();
            method.setName(methodCode.name);
            method.setDesc(methodCode.normalDesc);
            method.setArgs(methodCode.args);
            method.setReturnArg(methodCode.returnArg);
            method.setView(methodCode.hasViewAnnotation());
            method.setPayable(methodCode.hasPayableAnnotation());
            method.setEvent(false);
            return method;
        }).collect(Collectors.toList());
        programMethods.addAll(getEventConstructor(classCodes));
        return programMethods;
    }

    public static List<MethodCode> getProgramMethodCodes(Map<String, ClassCode> classCodes) {
        Map<String, MethodCode> methodCodes = new LinkedHashMap<>();
        ClassCode contractClassCode = getContractClassCode(classCodes);
        if (contractClassCode != null) {
            contractMethods(methodCodes, classCodes, contractClassCode, false);
        }
        return methodCodes.values().stream().collect(Collectors.toList());
    }

    private static ClassCode getContractClassCode(Map<String, ClassCode> classCodes) {
        return classCodes.values().stream().filter(classCode -> classCode.interfaces.contains(ProgramConstants.CONTRACT_INTERFACE_NAME)).findFirst().orElse(null);
    }

    private static void contractMethods(Map<String, MethodCode> methodCodes, Map<String, ClassCode> classCodes, ClassCode classCode, boolean isSupperClass) {
        classCode.methods.stream().filter(methodCode -> {
            if (methodCode.isPublic && !methodCode.isAbstract) {
                return true;
            } else {
                return false;
            }
        }).forEach(methodCode -> {
            if (isSupperClass && Constants.CONSTRUCTOR_NAME.equals(methodCode.name)) {
            } else if (Constants.CLINIT_NAME.equals(methodCode.name)) {
            } else {
                String name = methodCode.name + "." + methodCode.desc;
                methodCodes.putIfAbsent(name, methodCode);
            }
        });
        String superName = classCode.superName;
        if (StringUtils.isNotEmpty(superName)) {
            classCodes.values().stream().filter(code -> superName.equals(code.name)).findFirst()
                    .ifPresent(code -> {
                        contractMethods(methodCodes, classCodes, code, true);
                    });
        }
    }

    private BigInteger getAccountBalance(byte[] address) {
        if (vmContext == null) {
            return BigInteger.ZERO;
        } else {
            return vmContext.getBalance(address);
        }
    }

    private static Set<ProgramMethod> getEventConstructor(Map<String, ClassCode> classCodes) {
        Map<String, MethodCode> methodCodes = new LinkedHashMap<>();
        getEventClassCodes(classCodes).forEach(classCode -> {
            for (MethodCode methodCode : classCode.methods) {
                if (methodCode.isConstructor) {
                    methodCodes.put(methodCode.fullName, methodCode);
                }
            }
        });
        return methodCodes.values().stream()
                .filter(methodCode -> methodCode.isConstructor)
                .map(methodCode -> {
                    ProgramMethod method = new ProgramMethod();
                    method.setName(methodCode.classCode.simpleName);
                    method.setDesc(methodCode.normalDesc);
                    method.setArgs(methodCode.args);
                    method.setReturnArg(methodCode.returnArg);
                    method.setView(methodCode.hasViewAnnotation());
                    method.setPayable(methodCode.hasPayableAnnotation());
                    method.setEvent(true);
                    return method;
                }).collect(Collectors.toSet());
    }

    private static List<ClassCode> getEventClassCodes(Map<String, ClassCode> classCodes) {
        ClassCodes allCodes = new ClassCodes(classCodes);
        return classCodes.values().stream().filter(classCode -> !classCode.isAbstract
                && allCodes.instanceOf(classCode, ProgramConstants.EVENT_INTERFACE_NAME))
                .collect(Collectors.toList());
    }

    private static Source<byte[], byte[]> stateSource(DBService dbService) {
        LevelDbDataSource.dbService = dbService;
        SystemProperties config = SystemProperties.getDefault();
        CommonConfig commonConfig = CommonConfig.getDefault();
        StateSource stateSource = commonConfig.stateSource();
        stateSource.setConfig(config);
        stateSource.setCommonConfig(commonConfig);
        return stateSource;
    }

    public void logTime(String message) {
        if (log.isDebugEnabled()) {
            long currentTime = System.currentTimeMillis();
            long step = currentTime - this.currentTime;
            long runtime = currentTime - this.beginTime;
            this.currentTime = currentTime;
            ProgramTime.cache.putIfAbsent(message, new ProgramTime());
            ProgramTime time = ProgramTime.cache.get(message);
            time.add(step);
            log.debug("[{}] runtime: {}ms, step: {}ms, {}", message, runtime, step, time);
        }
//        if (step > 100) {
//            List<String> list = new ArrayList<>();
//            list.add(String.format("%s, runtime: %sms, step: %sms", message, runtime, step));
//            try {
//                FileUtils.writeLines(new File("/tmp/long.log"), list, true);
//            } catch (IOException e) {
//                log.error("", e);
//            }
//        }
    }

}

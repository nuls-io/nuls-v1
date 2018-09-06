package io.nuls.contract.vm.program.impl;

import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.*;
import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.ClassCodeLoader;
import io.nuls.contract.vm.code.ClassCodes;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.program.*;
import io.nuls.contract.vm.util.JsonUtils;
import io.nuls.db.service.DBService;
import org.apache.commons.lang3.StringUtils;
import org.ethereum.core.AccountState;
import org.ethereum.core.Repository;
import org.ethereum.datasource.Source;
import org.ethereum.db.RepositoryRoot;
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

    private VMContext vmContext;

    private Source<byte[], byte[]> source;

    private Repository repository;

    private byte[] prevStateRoot;

    private boolean canCommit;

    private boolean committed;

    private boolean view;

    private long beginTime;

    private long currentTime;

    static {
        ClassCodeLoader.init();
        VMFactory.init();
        JsonUtils.init();
    }

    public ProgramExecutorImpl(VMContext vmContext, DBService dbService) {
        this(vmContext, new KeyValueSource(dbService), null, null);
    }

    private ProgramExecutorImpl(VMContext vmContext, Source<byte[], byte[]> source, Repository repository, byte[] prevStateRoot) {
        this.vmContext = vmContext;
        this.source = source;
        this.repository = repository;
        this.prevStateRoot = prevStateRoot;
        this.beginTime = this.currentTime = System.currentTimeMillis();
    }

    @Override
    public ProgramExecutor begin(byte[] prevStateRoot) {
        log.debug("begin vm root: {}", Hex.toHexString(prevStateRoot));
        Repository repository = new RepositoryRoot(source, prevStateRoot);
        return new ProgramExecutorImpl(vmContext, source, repository, prevStateRoot);
    }

    @Override
    public void commit() {
        if (canCommit && !view) {
            repository.commit();
            committed = true;
        }
        logTime("commit");
    }

    @Override
    public byte[] getRoot() {
        byte[] root;
        if (committed) {
            root = repository.getRoot();
        } else {
            root = this.prevStateRoot;
        }
        log.debug("end vm root: {}, runtime: {}", Hex.toHexString(root), System.currentTimeMillis() - beginTime);
        return root;
    }

    @Override
    public ProgramResult create(ProgramCreate programCreate) {
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
        return execute(programInvoke);
    }

    @Override
    public ProgramResult call(ProgramCall programCall) {
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
        return execute(programInvoke);
    }

    public ProgramResult execute(ProgramInvoke programInvoke) {
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

        logTime("start");

        try {
            Map<String, ClassCode> classCodes;
            boolean newContract = false;
            AccountState accountState = repository.getAccountState(programInvoke.getContractAddress());
            logTime("load account state");
            if (accountState == null) {
                classCodes = ClassCodeLoader.loadJarCache(programInvoke.getData());
                logTime("load new code");
                ProgramChecker.check(classCodes);
                logTime("check code");
                accountState = repository.createAccount(programInvoke.getContractAddress(), programInvoke.getSender());
                logTime("new account state");
                repository.saveCode(programInvoke.getContractAddress(), programInvoke.getData());
                newContract = true;
                logTime("save code");
            } else {
                if ("<init>".equals(programInvoke.getMethodName())) {
                    return revert("can't invoke <init> method");
                }
                if (accountState.getNonce().compareTo(BigInteger.ZERO) <= 0) {
                    return revert("contract has been stopped");
                }
                byte[] codes = repository.getCode(programInvoke.getContractAddress());
                classCodes = ClassCodeLoader.loadJarCache(codes);
                logTime("load code");
            }

            VM vm = VMFactory.createVM();

            logTime("load vm");

            vm.getMethodArea().loadClassCodes(classCodes);

            logTime("load classes");

            ClassCode contractClassCode = getContractClassCode(classCodes);
            String methodDesc = ProgramDescriptors.parseDesc(programInvoke.getMethodDesc());
            MethodCode methodCode = vm.getMethodArea().loadMethod(contractClassCode.getName(), programInvoke.getMethodName(), methodDesc);

            if (methodCode == null) {
                return revert(String.format("can't find method %s.%s", programInvoke.getMethodName(), programInvoke.getMethodDesc()));
            }
            if (!methodCode.isPublic()) {
                return revert("can only invoke public method");
            }
            if (!methodCode.hasPayableAnnotation() && programInvoke.getValue().compareTo(BigInteger.ZERO) > 0) {
                return revert("not a payable method");
            }
            if (methodCode.getArgsVariableType().size() != programInvoke.getArgs().length) {
                return revert(String.format("require %s parameters in method [%s%s]",
                        methodCode.getArgsVariableType().size(), methodCode.getName(), methodCode.getNormalDesc()));
            }

            logTime("load method");

            BigInteger accountBalance = getAccountBalance(programInvoke.getContractAddress());
            BigInteger vmBalance = repository.getBalance(programInvoke.getContractAddress());
            if (vmBalance.compareTo(accountBalance) != 0) {
                return revert(String.format("balance error: accountBalance=%s, vmBalance=%s", accountBalance, vmBalance));
            }

            logTime("load balance");

            ObjectRef objectRef;
            if (newContract) {
                objectRef = vm.getHeap().newContract(programInvoke.getContractAddress(), contractClassCode, repository);
            } else {
                objectRef = vm.getHeap().loadContract(programInvoke.getContractAddress(), contractClassCode, repository);
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
                    String error = vm.getHeap().runToString((ObjectRef) resultValue);
                    String stackTrace = vm.getHeap().stackTrace((ObjectRef) resultValue);
                    programResult.error(error);
                    programResult.setStackTrace(stackTrace);
                } else {
                    programResult.error(null);
                }

                logTime("contract exception");

                return programResult;
            }

            Map<DataWord, DataWord> contractState = vm.getHeap().contractState();
            logTime("contract state");

            for (Map.Entry<DataWord, DataWord> entry : contractState.entrySet()) {
                DataWord key = entry.getKey();
                DataWord value = entry.getValue();
                this.repository.addStorageRow(programInvoke.getContractAddress(), key, value);
            }
            logTime("add contract state");

            repository.increaseNonce(programInvoke.getContractAddress());
            programResult.setNonce(repository.getNonce(programInvoke.getContractAddress()));
            programResult.setTransfers(vm.getTransfers());
            programResult.setEvents(vm.getEvents());
            programResult.setBalance(repository.getBalance(programInvoke.getContractAddress()));

            if (resultValue != null) {
                if (resultValue instanceof ObjectRef) {
                    String result = vm.getHeap().runToString((ObjectRef) resultValue);
                    programResult.setResult(result);
                } else {
                    programResult.setResult(resultValue.toString());
                }
            }

            if (methodCode.isPublic() && methodCode.hasViewAnnotation()) {
                view = true;
                programResult.view();
            }

            this.canCommit = true;

            logTime("contract return");

            return programResult;
        } catch (ErrorException e) {
            log.error("", e);
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
        ProgramResult programResult = new ProgramResult();
        programResult.setStackTrace(stackTrace);
        logTime("revert");
        return programResult.revert(errorMessage);
    }

    @Override
    public ProgramResult stop(byte[] address, byte[] sender) {
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

        repository.setNonce(address, BigInteger.ZERO);

        ProgramResult programResult = new ProgramResult();

        this.canCommit = true;

        return programResult;
    }

    @Override
    public ProgramStatus status(byte[] address) {
        AccountState accountState = repository.getAccountState(address);
        if (accountState == null) {
            return ProgramStatus.not_found;
        } else {
            BigInteger nonce = repository.getNonce(address);
            if (BigInteger.ZERO.compareTo(nonce) == 0) {
                return ProgramStatus.stop;
            } else {
                return ProgramStatus.normal;
            }
        }
    }

    @Override
    public List<ProgramMethod> method(byte[] address) {
        byte[] codes = repository.getCode(address);
        return jarMethod(codes);
    }

    @Override
    public List<ProgramMethod> jarMethod(byte[] jarData) {
        if (jarData == null || jarData.length < 1) {
            return new ArrayList<>();
        }
        Map<String, ClassCode> classCodes = ClassCodeLoader.loadJar(jarData);
        return getProgramMethods(classCodes);
    }

    private static List<ProgramMethod> getProgramMethods(Map<String, ClassCode> classCodes) {
        List<ProgramMethod> programMethods = getProgramMethodCodes(classCodes).stream().map(methodCode -> {
            ProgramMethod method = new ProgramMethod();
            method.setName(methodCode.getName());
            method.setDesc(methodCode.getNormalDesc());
            method.setArgs(methodCode.getArgs());
            method.setReturnArg(methodCode.getReturnArg());
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
        return classCodes.values().stream().filter(classCode -> classCode.getInterfaces().contains(ProgramConstants.CONTRACT_INTERFACE_NAME)).findFirst().orElse(null);
    }

    private static void contractMethods(Map<String, MethodCode> methodCodes, Map<String, ClassCode> classCodes, ClassCode classCode, boolean isSupperClass) {
        classCode.getMethods().stream().filter(methodCode -> {
            if (methodCode.isPublic() && methodCode.isNotAbstract()) {
                return true;
            } else {
                return false;
            }
        }).forEach(methodCode -> {
            if (isSupperClass && "<init>".equals(methodCode.getName())) {
            } else if ("<clinit>".equals(methodCode.getName())) {
            } else {
                String name = methodCode.getName() + "." + methodCode.getDesc();
                methodCodes.putIfAbsent(name, methodCode);
            }
        });
        String superName = classCode.getSuperName();
        if (StringUtils.isNotEmpty(superName)) {
            classCodes.values().stream().filter(code -> superName.equals(code.getName())).findFirst()
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
        Set<MethodCode> methodCodes = new LinkedHashSet<>();
        getEventClassCodes(classCodes).forEach(classCode -> {
            methodCodes.addAll(classCode.getMethods());
        });
        return methodCodes.stream()
                .filter(methodCode -> methodCode.isConstructor())
                .map(methodCode -> {
                    ProgramMethod method = new ProgramMethod();
                    method.setName(methodCode.getClassCode().getSimpleName());
                    method.setDesc(methodCode.getNormalDesc());
                    method.setArgs(methodCode.getArgs());
                    method.setReturnArg(methodCode.getReturnArg());
                    method.setView(methodCode.hasViewAnnotation());
                    method.setPayable(methodCode.hasPayableAnnotation());
                    method.setEvent(true);
                    return method;
                }).collect(Collectors.toSet());
    }

    private static List<ClassCode> getEventClassCodes(Map<String, ClassCode> classCodes) {
        ClassCodes allCodes = new ClassCodes(classCodes);
        ClassCode contractClassCode = getContractClassCode(classCodes);
        Set<ClassCode> contractClassCodes = allCodes.allClasses(contractClassCode);
        ClassCodes contractAllCodes = new ClassCodes(contractClassCodes);
        return contractClassCodes.stream().filter(classCode -> classCode.isNotAbstract()
                && contractAllCodes.instanceOf(classCode, ProgramConstants.EVENT_INTERFACE_NAME))
                .collect(Collectors.toList());
    }

    public void logTime(String message) {
        long currentTime = System.currentTimeMillis();
        long step = currentTime - this.currentTime;
        long runtime = currentTime - this.beginTime;
        this.currentTime = currentTime;
        if (log.isDebugEnabled()) {
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

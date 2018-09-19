package io.nuls.contract.vm.program;

import java.util.List;

public interface ProgramExecutor {

    ProgramExecutor begin(byte[] prevStateRoot);

    ProgramExecutor startTracking();

    void commit();

    byte[] getRoot();

    ProgramResult create(ProgramCreate programCreate);

    ProgramResult call(ProgramCall programCall);

    ProgramResult stop(byte[] address, byte[] sender);

    List<ProgramMethod> method(byte[] address);

    List<ProgramMethod> jarMethod(byte[] jarData);

    ProgramStatus status(byte[] address);

}

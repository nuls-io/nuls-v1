package io.nuls.consensus.poc.tx.validator;

import io.nuls.consensus.poc.constant.PocConsensusConstant;
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.SeverityLevelEnum;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;

import java.util.*;

/**
 * @author Niels
 * @date 2018/5/17
 */
@Component
public class StopAgentTxValidator implements NulsDataValidator<StopAgentTransaction> {

    @Autowired
    private AgentStorageService agentStorageService;

    @Override
    public ValidateResult validate(StopAgentTransaction data) throws NulsException {
        AgentPo agentPo = agentStorageService.get(data.getTxData().getCreateTxHash());
        if (null == agentPo || agentPo.getDelHeight() > 0) {
            return ValidateResult.getFailedResult(this.getClass().getName(), "The agent is deleted or never created!");
        }
        P2PKHScriptSig sig = new P2PKHScriptSig();
        try {
            sig.parse(data.getScriptSig());
        } catch (NulsException e) {
            Log.error(e);
            return ValidateResult.getFailedResult(this.getClass().getName(), e.getMessage());
        }
        if (!Arrays.equals(agentPo.getAgentAddress(), AddressTool.getAddress(sig.getPublicKey()))) {
            ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), "The agent does not belong to this address.");
            result.setLevel(SeverityLevelEnum.FLAGRANT_FOUL);
            return result;
        }
        if (data.getCoinData().getTo() == null || data.getCoinData().getTo().size() > 1) {
            return ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.DATA_ERROR, "The coindata is wrong.");
        }
        if (!Arrays.equals(agentPo.getAgentAddress(), data.getCoinData().getTo().get(0).getOwner())) {
            ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), "The address is wrong!");
            return result;
        }

        List<Deposit> allDepositList = PocConsensusContext.getChainManager().getMasterChain().getChain().getDepositList();
        Map<NulsDigestData, Deposit> depositMap = new HashMap<>();
        Na totalNa = agentPo.getDeposit();
        Deposit ownDeposit = new Deposit();
        ownDeposit.setDeposit(agentPo.getDeposit().subtract(data.getFee()));
        depositMap.put(data.getTxData().getCreateTxHash(), ownDeposit);
        for (Deposit deposit : allDepositList) {
            if (deposit.getDelHeight() > 0) {
                continue;
            }
            if (!deposit.getAgentHash().equals(agentPo.getHash())) {
                continue;
            }
            depositMap.put(deposit.getTxHash(), deposit);
            totalNa = totalNa.add(deposit.getDeposit());
        }

        Na fromTotal = Na.ZERO;
        Map<String, Na> verifyToMap = new HashMap<>();
        for (Coin coin : data.getCoinData().getFrom()) {
            NulsDigestData txHash = new NulsDigestData();
            txHash.parse(coin.getOwner());
            Deposit deposit = depositMap.remove(txHash);
            if (deposit == null) {
                return ValidateResult.getFailedResult(this.getClass().getName(), "The stop agent tx used a wrong coin!");
            }
            if (deposit.getAgentHash() == null && !coin.getNa().equals(agentPo.getDeposit())) {
                return ValidateResult.getFailedResult(this.getClass().getName(), "The stop agent tx used a wrong agent's deposit coin!");
            } else if (!deposit.getDeposit().equals(coin.getNa())) {
                return ValidateResult.getFailedResult(this.getClass().getName(), "The stop agent tx used a wrong deposit coin!");
            }
            fromTotal = fromTotal.add(coin.getNa());
            String address = Base58.encode(deposit.getAddress());
            Na na = verifyToMap.get(address);
            if (null == na) {
                na = deposit.getDeposit();
            } else {
                na = na.add(deposit.getDeposit());
            }
            verifyToMap.put(address, na);
        }
        if (!depositMap.isEmpty()) {
            return ValidateResult.getFailedResult(this.getClass().getName(), "There is deposit of the agent never canceled");
        }
        if (!totalNa.equals(fromTotal)) {
            return ValidateResult.getFailedResult(this.getClass().getName(), "The stop agent tx used wrong coin!");
        }
        for (Coin coin : data.getCoinData().getTo()) {
            String address = Base58.encode(coin.getOwner());
            Na na = verifyToMap.remove(address);
            if (null == na || !na.equals(coin.getNa())) {
                return ValidateResult.getFailedResult(this.getClass().getName(), "The stop agent tx used wrong coin!");
            }
        }
        if (!verifyToMap.isEmpty()) {
            return ValidateResult.getFailedResult(this.getClass().getName(), "The tr produced wrong coins!");
        }

        return ValidateResult.getSuccessResult();
    }
}

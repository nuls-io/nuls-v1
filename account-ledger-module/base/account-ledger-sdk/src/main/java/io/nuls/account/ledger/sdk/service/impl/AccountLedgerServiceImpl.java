package io.nuls.account.ledger.sdk.service.impl;

import io.nuls.account.ledger.sdk.model.InputDto;
import io.nuls.account.ledger.sdk.model.OutputDto;
import io.nuls.account.ledger.sdk.model.TransactionDto;
import io.nuls.account.ledger.sdk.service.AccountLedgerService;
import io.nuls.sdk.SDKBootstrap;
import io.nuls.sdk.constant.AccountErrorCode;
import io.nuls.sdk.constant.SDKConstant;
import io.nuls.sdk.constant.TransactionErrorCode;
import io.nuls.sdk.crypto.AESEncrypt;
import io.nuls.sdk.crypto.Base58;
import io.nuls.sdk.crypto.ECKey;
import io.nuls.sdk.crypto.Hex;
import io.nuls.sdk.exception.NulsException;
import io.nuls.sdk.model.*;
import io.nuls.sdk.model.dto.BalanceDto;
import io.nuls.sdk.script.P2PKHScriptSig;
import io.nuls.sdk.utils.*;
import org.spongycastle.crypto.CryptoException;
import org.spongycastle.util.Arrays;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;

/**
 * @author: Charlie
 * @date: 2018/6/12
 */
public class AccountLedgerServiceImpl implements AccountLedgerService {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public Result getTxByHash(String hash) {
        if (StringUtils.isBlank(hash)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }

        Result result = restFul.get("/accountledger/tx/" + hash, null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map) result.getData();
        //重新组装input
        List<Map<String, Object>> inputMaps = (List<Map<String, Object>>) map.get("inputs");
        List<InputDto> inputs = new ArrayList<>();
        for (Map<String, Object> inputMap : inputMaps) {
            InputDto inputDto = new InputDto(inputMap);
            inputs.add(inputDto);
        }
        map.put("inputs", inputs);

        //重新组装output
        List<Map<String, Object>> outputMaps = (List<Map<String, Object>>) map.get("outputs");
        List<OutputDto> outputs = new ArrayList<>();
        for (Map<String, Object> outputMap : outputMaps) {
            OutputDto outputDto = new OutputDto(outputMap);
            outputs.add(outputDto);
        }
        map.put("outputs", outputs);
        TransactionDto transactionDto = new TransactionDto(map);
        result.setData(transactionDto);
        return result;
    }

    @Override
    public Result transfer(String address, String toAddress, String password, long amount, String remark) {
        if (!Address.validAddress(address) || !Address.validAddress(toAddress)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        if (!validTxRemark(remark)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address", address);
        parameters.put("toAddress", toAddress);
        parameters.put("password", password);
        parameters.put("amount", amount);
        parameters.put("remark", remark);
        Result result = restFul.post("/accountledger/transfer", parameters);
        return result;
    }

    private boolean validTxRemark(String remark) {
        if (StringUtils.isBlank(remark)) {
            return true;
        }
        try {
            byte[] bytes = remark.getBytes(SDKConstant.DEFAULT_ENCODING);
            if (bytes.length > 100) {
                return false;
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    @Override
    public Result getBalance(String address) {
        if (!Address.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Result result = restFul.get("/accountledger/balance/" + address, null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map) result.getData();
        map.put("balance", ((Map) map.get("balance")).get("value"));
        map.put("usable", ((Map) map.get("usable")).get("value"));
        map.put("locked", ((Map) map.get("locked")).get("value"));
        BalanceDto balanceDto = new BalanceDto(map);
        return result.setData(balanceDto);
    }

    @Override
    public Result createTransaction(List<InputDto> inputs, List<OutputDto> outputs, String remark) {
        if (inputs == null || inputs.isEmpty()) {
            return Result.getFailed("inputs error");
        }
        if (outputs == null || outputs.isEmpty()) {
            return Result.getFailed("outputs error");
        }
        byte[] remarkBytes = null;
        if (!StringUtils.isBlank(remark)) {
            try {
                remarkBytes = remark.getBytes(SDKConstant.DEFAULT_ENCODING);
            } catch (UnsupportedEncodingException e) {
                return Result.getFailed("remark error");
            }
        }
        List<Coin> outputList = new ArrayList<>();
        for (int i = 0; i < outputs.size(); i++) {
            OutputDto outputDto = outputs.get(i);
            Coin to = new Coin();
            try {
                to.setOwner(Base58.decode(outputDto.getAddress()));
            } catch (Exception e) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
            }

            to.setNa(Na.valueOf(outputDto.getValue()));
            if (outputDto.getLockTime() < 0) {
                return Result.getFailed("lockTime error");
            }

            to.setLockTime(outputDto.getLockTime());
            outputList.add(to);
        }

        List<Coin> inputsList = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            InputDto inputDto = inputs.get(i);
            byte[] key = Arrays.concatenate(Hex.decode(inputDto.getFromHash()), new VarInt(inputDto.getFromIndex()).encode());
            Coin coin = new Coin();
            coin.setOwner(key);
            coin.setNa(Na.valueOf(inputDto.getValue()));
            inputsList.add(coin);
        }

        Transaction tx = TransactionTool.createTransferTx(inputsList, outputList, remarkBytes);
        //计算交易手续费最小值
        int size = tx.size() + P2PKHScriptSig.DEFAULT_SERIALIZE_LENGTH;
        Na minFee = TransactionFeeCalculator.getTransferFee(size);
        //计算inputs和outputs的差额 ，求手续费
        Na fee = Na.ZERO;
        for (Coin coin : tx.getCoinData().getFrom()) {
            fee = fee.add(coin.getNa());
        }
        for (Coin coin : tx.getCoinData().getTo()) {
            fee = fee.subtract(coin.getNa());
        }
        if (fee.isLessThan(minFee)) {
            return Result.getFailed(TransactionErrorCode.FEE_NOT_RIGHT);
        }
        try {
            String txHex = Hex.encode(tx.serialize());
            return Result.getSuccess().setData(txHex);
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }

    @Override
    public Result signTransaction(String txHex, String priKey, String address, String password) {
        if (StringUtils.isBlank(priKey)) {
            return Result.getFailed("priKey error");
        }
        if (StringUtils.isBlank(txHex)) {
            return Result.getFailed("txHex error");
        }
        if (!Address.validAddress(address)) {
            return Result.getFailed("address error");
        }

        if (StringUtils.isNotBlank(password)) {
            if (StringUtils.validPassword(password)) {
                //decrypt
                byte[] privateKeyBytes = null;
                try {
                    privateKeyBytes = AESEncrypt.decrypt(Hex.decode(priKey), password);
                } catch (Exception e) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
                priKey = Hex.encode(privateKeyBytes);
            } else {
                return Result.getFailed("password error");
            }
        }
        if (!ECKey.isValidPrivteHex(priKey)) {
            return Result.getFailed("priKey error");
        }

        ECKey key = ECKey.fromPrivate(new BigInteger(Hex.decode(priKey)));
        try {
            String newAddress = AccountTool.newAddress(key).getBase58();
            if (!newAddress.equals(address)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
            }
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }

        try {
            byte[] data = Hex.decode(txHex);
            Transaction tx = TransactionTool.getInstance(new NulsByteBuffer(data));
            tx = TransactionTool.signTransaction(tx, key);

            return Result.getSuccess().setData(Hex.encode(tx.serialize()));
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR);
        }
    }

}

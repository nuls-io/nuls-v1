package io.nuls.consensus.poc.protocol.validator;

import io.nuls.consensus.poc.protocol.constant.PocConsensusProtocolConstant;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.CoinData;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.NulsData;
import io.nuls.kernel.validate.NulsDataValidator;

public abstract class BaseConsensusProtocolValidator<T extends NulsData> implements NulsDataValidator<T> {

    protected boolean isDepositOk(Na deposit, CoinData coinData) {
        if(coinData == null || coinData.getTo().size() == 0) {
            return false;
        }
        Coin coin = coinData.getTo().get(0);
        if(!deposit.equals(coin.getNa())) {
            return false;
        }
        if(coin.getLockTime() != PocConsensusProtocolConstant.LOCK_OF_LOCK_TIME) {
            return false;
        }
        return true;
    }

}

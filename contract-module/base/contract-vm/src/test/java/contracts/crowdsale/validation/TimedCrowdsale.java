package contracts.crowdsale.validation;

import contracts.crowdsale.Crowdsale;
import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Block;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigInteger;

import static io.nuls.contract.sdk.Utils.require;

public class TimedCrowdsale extends Crowdsale {

    private long openingTime;
    private long closingTime;

    @View
    public long getOpeningTime() {
        return openingTime;
    }

    @View
    public long getClosingTime() {
        return closingTime;
    }

    public void onlyWhileOpen() {
        require(Block.timestamp() >= openingTime && Block.timestamp() <= closingTime);
    }

    public TimedCrowdsale(long openingTime, long closingTime, BigInteger rate, Address wallet, Address token) {
        super(rate, wallet, token);
        require(openingTime >= Block.timestamp());
        require(closingTime >= openingTime);

        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    @View
    public boolean hasClosed() {
        return Block.timestamp() > closingTime;
    }

    @Override
    protected void preValidatePurchase(Address beneficiary, BigInteger weiAmount) {
        onlyWhileOpen();
        super.preValidatePurchase(beneficiary, weiAmount);
    }

}

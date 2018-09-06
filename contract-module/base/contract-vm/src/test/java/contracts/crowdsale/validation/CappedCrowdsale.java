package contracts.crowdsale.validation;

import contracts.crowdsale.Crowdsale;
import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigInteger;

import static io.nuls.contract.sdk.Utils.require;

public class CappedCrowdsale extends Crowdsale {

    private BigInteger cap;

    @View
    public BigInteger getCap() {
        return cap;
    }

    public CappedCrowdsale(BigInteger rate, Address wallet, Address token, BigInteger cap) {
        super(rate, wallet, token);
        require(cap.compareTo(BigInteger.ZERO) > 0);
        this.cap = cap;
    }

    @View
    public boolean capReached() {
        return getWeiRaised().compareTo(cap) >= 0;
    }

    protected void preValidatePurchase(Address beneficiary, BigInteger weiAmount) {
        super.preValidatePurchase(beneficiary, weiAmount);
        require(getWeiRaised().add(weiAmount).compareTo(cap) <= 0);
    }

}

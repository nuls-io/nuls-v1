package contracts.crowdsale.emission;

import contracts.crowdsale.distribution.RefundableCrowdsale;
import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Msg;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigInteger;

import static io.nuls.contract.sdk.Utils.emit;
import static io.nuls.contract.sdk.Utils.require;

public abstract class MintedCrowdsale extends RefundableCrowdsale {

    private Address owner;

    private BigInteger cap;

    @View
    public BigInteger getCap() {
        return cap;
    }

    public MintedCrowdsale(long openingTime, long closingTime, BigInteger rate, Address wallet, BigInteger cap, Address token, BigInteger goal) {
        super(openingTime, closingTime, rate, wallet, token, goal);
        this.owner = Msg.sender();
        require(cap.compareTo(BigInteger.ZERO) > 0);
        this.cap = cap;
    }

    public boolean capReached() {
        return getWeiRaised().compareTo(cap) >= 0;
    }

    @Override
    protected void preValidatePurchase(Address beneficiary, BigInteger weiAmount) {
        super.preValidatePurchase(beneficiary, weiAmount);
        require(getWeiRaised().add(weiAmount).compareTo(cap) <= 0);
    }

    @Override
    protected void deliverTokens(Address beneficiary, BigInteger tokenAmount) {
        String[][] args = new String[][]{{beneficiary.toString()}, {tokenAmount.toString()}};
        getToken().call("mint", null, args, null);
    }

    @Override
    @View
    public Address getOwner() {
        return owner;
    }

    @Override
    public void onlyOwner() {
        require(Msg.sender().equals(owner));
    }

    @Override
    public void transferOwnership(Address newOwner) {
        onlyOwner();
        emit(new OwnershipTransferredEvent(owner, newOwner));
        owner = newOwner;
    }

    @Override
    public void renounceOwnership() {
        onlyOwner();
        emit(new OwnershipRenouncedEvent(owner));
        owner = null;
    }

}

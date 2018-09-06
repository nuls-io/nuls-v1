package contracts.crowdsale.distribution;

import contracts.crowdsale.distribution.utils.RefundVault;
import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Msg;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigInteger;

import static io.nuls.contract.sdk.Utils.require;

public abstract class RefundableCrowdsale extends FinalizableCrowdsale {

    private BigInteger goal;

    private RefundVault vault;

    @View
    public BigInteger getGoal() {
        return goal;
    }

    public RefundVault getVault() {
        return vault;
    }

    public RefundableCrowdsale(long openingTime, long closingTime, BigInteger rate, Address wallet, Address token, BigInteger goal) {
        super(openingTime, closingTime, rate, wallet, token);
        require(goal.compareTo(BigInteger.ZERO) > 0);
        vault = new RefundVault(wallet);
        this.goal = goal;
    }

    public void claimRefund() {
        require(isFinalized());
        require(!goalReached());

        vault.refund(Msg.sender());
    }

    @View
    public boolean goalReached() {
        return getWeiRaised().compareTo(goal) >= 0;
    }

    @Override
    protected void finalization() {
        if (goalReached()) {
            vault.close();
        } else {
            vault.enableRefunds();
        }

        super.finalization();
    }

    @Override
    protected void forwardFunds() {
        vault.deposit(Msg.sender());
    }

}

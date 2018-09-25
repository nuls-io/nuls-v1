package contracts.crowdsale.distribution;

import contracts.crowdsale.validation.TimedCrowdsale;
import contracts.ownership.Ownable;
import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigInteger;

import static io.nuls.contract.sdk.Utils.emit;
import static io.nuls.contract.sdk.Utils.require;

public abstract class FinalizableCrowdsale extends TimedCrowdsale implements Ownable {

    private boolean isFinalized = false;

    @View
    public boolean isFinalized() {
        return isFinalized;
    }

    class Finalized implements Event {

    }

    public void finalized() {
        onlyOwner();
        require(!isFinalized);
        require(hasClosed());

        finalization();
        emit(new Finalized());

        isFinalized = true;
    }

    protected void finalization() {
    }

    public FinalizableCrowdsale(long openingTime, long closingTime, BigInteger rate, Address wallet, Address token) {
        super(openingTime, closingTime, rate, wallet, token);
    }

}

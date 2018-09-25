package contracts.examples;

import contracts.crowdsale.emission.MintedCrowdsale;
import io.nuls.contract.sdk.Address;

import java.math.BigInteger;

import static io.nuls.contract.sdk.Utils.require;

public class TestCrowdsale extends MintedCrowdsale {

    public TestCrowdsale(long openingTime, long closingTime, BigInteger rate, Address wallet, BigInteger cap, Address token, BigInteger goal) {
        super(openingTime, closingTime, rate, wallet, cap, token, goal);
        require(goal.compareTo(cap) <= 0);
    }

}

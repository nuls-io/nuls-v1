package contracts.crowdsale.distribution.utils;

import contracts.ownership.OwnableImpl;
import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import io.nuls.contract.sdk.Msg;
import io.nuls.contract.sdk.annotation.Payable;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static io.nuls.contract.sdk.Utils.emit;
import static io.nuls.contract.sdk.Utils.require;

public class RefundVault extends OwnableImpl {

    public int Active = 1;
    public int Refunding = 2;
    public int Closed = 3;

    private Map<Address, BigInteger> deposited = new HashMap<Address, BigInteger>();
    private Address wallet;
    private int state;

    @View
    public BigInteger getDeposited(Address address) {
        BigInteger value = deposited.get(address);
        if (value == null) {
            value = BigInteger.ZERO;
        }
        return value;
    }

    @View
    public Address getWallet() {
        return wallet;
    }

    @View
    public int getState() {
        return state;
    }

    class Closed implements Event {

    }

    class RefundsEnabled implements Event {

    }

    class Refunded implements Event {

        private Address beneficiary;
        private BigInteger weiAmount;

        public Refunded(Address beneficiary, BigInteger weiAmount) {
            this.beneficiary = beneficiary;
            this.weiAmount = weiAmount;
        }

        @Override
        public String toString() {
            return "Refunded{" +
                    "beneficiary=" + beneficiary +
                    ", weiAmount=" + weiAmount +
                    '}';
        }

    }

    public RefundVault(Address wallet) {
        super();
        this.wallet = wallet;
        this.state = Active;
    }

    @Payable
    public void deposit(Address investor) {
        onlyOwner();
        require(state == Active);
        BigInteger value = deposited.get(investor);
        if (value == null) {
            value = BigInteger.ZERO;
        }
        deposited.put(investor, value.add(Msg.value()));
    }

    public void close() {
        onlyOwner();
        require(state == Active);
        state = Closed;
        emit(new Closed());
        wallet.transfer(Msg.address().balance());
    }

    public void enableRefunds() {
        onlyOwner();
        require(state == Active);
        state = Refunding;
        emit(new RefundsEnabled());
    }

    public void refund(Address investor) {
        require(state == Refunding);
        BigInteger depositedValue = deposited.get(investor);
        if (depositedValue == null) {
            depositedValue = BigInteger.ZERO;
        }
        deposited.put(investor, BigInteger.ZERO);
        investor.transfer(depositedValue);
        emit(new Refunded(investor, depositedValue));
    }

}

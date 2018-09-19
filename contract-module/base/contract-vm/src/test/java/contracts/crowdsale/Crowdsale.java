package contracts.crowdsale;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import io.nuls.contract.sdk.Msg;
import io.nuls.contract.sdk.annotation.Payable;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigInteger;

import static io.nuls.contract.sdk.Utils.emit;
import static io.nuls.contract.sdk.Utils.require;

public class Crowdsale {

    private Address token;

    private Address wallet;

    private BigInteger rate;

    private BigInteger weiRaised = BigInteger.ZERO;

    @View
    public Address getToken() {
        return token;
    }

    @View
    public Address getWallet() {
        return wallet;
    }

    @View
    public BigInteger getRate() {
        return rate;
    }

    @View
    public BigInteger getWeiRaised() {
        return weiRaised;
    }

    class TokenPurchaseEvent implements Event {

        private Address purchaser;

        private Address beneficiary;

        private BigInteger value;

        private BigInteger amount;

        public TokenPurchaseEvent(Address purchaser, Address beneficiary, BigInteger value, BigInteger amount) {
            this.purchaser = purchaser;
            this.beneficiary = beneficiary;
            this.value = value;
            this.amount = amount;
        }

        public Address getPurchaser() {
            return purchaser;
        }

        public void setPurchaser(Address purchaser) {
            this.purchaser = purchaser;
        }

        public Address getBeneficiary() {
            return beneficiary;
        }

        public void setBeneficiary(Address beneficiary) {
            this.beneficiary = beneficiary;
        }

        public BigInteger getValue() {
            return value;
        }

        public void setValue(BigInteger value) {
            this.value = value;
        }

        public BigInteger getAmount() {
            return amount;
        }

        public void setAmount(BigInteger amount) {
            this.amount = amount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TokenPurchaseEvent that = (TokenPurchaseEvent) o;

            if (purchaser != null ? !purchaser.equals(that.purchaser) : that.purchaser != null) return false;
            if (beneficiary != null ? !beneficiary.equals(that.beneficiary) : that.beneficiary != null) return false;
            if (value != null ? !value.equals(that.value) : that.value != null) return false;
            return amount != null ? amount.equals(that.amount) : that.amount == null;
        }

        @Override
        public int hashCode() {
            int result = purchaser != null ? purchaser.hashCode() : 0;
            result = 31 * result + (beneficiary != null ? beneficiary.hashCode() : 0);
            result = 31 * result + (value != null ? value.hashCode() : 0);
            result = 31 * result + (amount != null ? amount.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "TokenPurchaseEvent{" +
                    "purchaser=" + purchaser +
                    ", beneficiary=" + beneficiary +
                    ", value=" + value +
                    ", amount=" + amount +
                    '}';
        }

    }

    public Crowdsale(BigInteger rate, Address wallet, Address token) {
        require(rate != null && rate.compareTo(BigInteger.ZERO) > 0);
        require(wallet != null);
        require(token != null);

        this.rate = rate;
        this.wallet = wallet;
        this.token = token;
    }

//    function () external payable {
//        buyTokens(msg.sender);
//    }

    @Payable
    public void buyTokens(Address beneficiary) {

        BigInteger weiAmount = Msg.value();
        preValidatePurchase(beneficiary, weiAmount);

        BigInteger tokens = getTokenAmount(weiAmount);

        weiRaised = weiRaised.add(weiAmount);

        processPurchase(beneficiary, tokens);
        emit(new TokenPurchaseEvent(Msg.sender(), beneficiary, weiAmount, tokens));

        updatePurchasingState(beneficiary, weiAmount);

        forwardFunds();
        postValidatePurchase(beneficiary, weiAmount);
    }

    protected void preValidatePurchase(Address beneficiary, BigInteger weiAmount) {
        require(beneficiary != null);
        require(weiAmount != null && weiAmount.compareTo(BigInteger.ZERO) > 0);
    }

    protected void postValidatePurchase(Address beneficiary, BigInteger weiAmount) {
        // optional override
    }

    protected void deliverTokens(Address beneficiary, BigInteger tokenAmount) {
        String[][] args = new String[][]{{beneficiary.toString()}, {tokenAmount.toString()}};
        token.call("transfer", null, args, null);
    }

    protected void processPurchase(Address beneficiary, BigInteger tokenAmount) {
        deliverTokens(beneficiary, tokenAmount);
    }

    protected void updatePurchasingState(Address beneficiary, BigInteger weiAmount) {
        // optional override
    }

    protected BigInteger getTokenAmount(BigInteger weiAmount) {
        return weiAmount.multiply(rate);
    }

    protected void forwardFunds() {
        wallet.transfer(Msg.value());
    }

}

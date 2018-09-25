package contracts.token.ERC20;

import contracts.ownership.Ownable;
import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import io.nuls.contract.sdk.Msg;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigInteger;

import static io.nuls.contract.sdk.Utils.emit;
import static io.nuls.contract.sdk.Utils.require;

public class MintableToken extends StandardToken implements Ownable {

    private boolean mintingFinished = false;

    private Address owner;

    public MintableToken() {
        this.owner = Msg.sender();
    }

    @View
    public boolean isMintingFinished() {
        return mintingFinished;
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

    public void canMint() {
        require(!mintingFinished);
    }

    public void hasMintPermission() {
        require(Msg.sender().equals(owner));
    }

    public boolean mint(Address to, BigInteger amount) {
        hasMintPermission();
        canMint();
        check(amount);
        totalSupply = totalSupply.add(amount);
        addBalance(to, amount);
        emit(new MintEvent(to, amount));
        emit(new TransferEvent(null, to, amount));
        return true;
    }

    public boolean finishMinting() {
        onlyOwner();
        canMint();
        mintingFinished = true;
        emit(new MintFinishedEvent());
        return true;
    }

    class MintEvent implements Event {

        private Address to;

        private BigInteger amount;

        public MintEvent(Address to, BigInteger amount) {
            this.to = to;
            this.amount = amount;
        }

        public Address getTo() {
            return to;
        }

        public void setTo(Address to) {
            this.to = to;
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

            MintEvent mintEvent = (MintEvent) o;

            if (to != null ? !to.equals(mintEvent.to) : mintEvent.to != null) return false;
            return amount != null ? amount.equals(mintEvent.amount) : mintEvent.amount == null;
        }

        @Override
        public int hashCode() {
            int result = to != null ? to.hashCode() : 0;
            result = 31 * result + (amount != null ? amount.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "MintEvent{" +
                    "to=" + to +
                    ", amount=" + amount +
                    '}';
        }

    }

    class MintFinishedEvent implements Event {

    }

}

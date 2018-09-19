package contracts.token.ERC20;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigInteger;

public interface ERC20 extends ERC20Basic {

    @View
    BigInteger allowance(Address owner, Address spender);

    boolean transferFrom(Address from, Address to, BigInteger value);

    boolean approve(Address spender, BigInteger value);

    class ApprovalEvent implements Event {

        private Address owner;

        private Address spender;

        private BigInteger value;

        public ApprovalEvent(Address owner, Address spender, BigInteger value) {
            this.owner = owner;
            this.spender = spender;
            this.value = value;
        }

        public Address getOwner() {
            return owner;
        }

        public void setOwner(Address owner) {
            this.owner = owner;
        }

        public Address getSpender() {
            return spender;
        }

        public void setSpender(Address spender) {
            this.spender = spender;
        }

        public BigInteger getValue() {
            return value;
        }

        public void setValue(BigInteger value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ApprovalEvent that = (ApprovalEvent) o;

            if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
            if (spender != null ? !spender.equals(that.spender) : that.spender != null) return false;
            return value != null ? value.equals(that.value) : that.value == null;
        }

        @Override
        public int hashCode() {
            int result = owner != null ? owner.hashCode() : 0;
            result = 31 * result + (spender != null ? spender.hashCode() : 0);
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ApprovalEvent{" +
                    "owner=" + owner +
                    ", spender=" + spender +
                    ", value=" + value +
                    '}';
        }

    }

}

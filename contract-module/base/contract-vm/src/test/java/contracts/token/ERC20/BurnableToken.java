package contracts.token.ERC20;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import io.nuls.contract.sdk.Msg;

import java.math.BigInteger;

import static io.nuls.contract.sdk.Utils.emit;

public class BurnableToken extends BasicToken {

    public void burn(BigInteger value) {
        Address burner = Msg.sender();
        subtractBalance(burner, value);
        totalSupply = totalSupply.subtract(value);
        emit(new BurnEvent(burner, value));
        emit(new TransferEvent(burner, null, value));
    }

    class BurnEvent implements Event {

        private Address burner;

        private BigInteger value;

        public BurnEvent(Address burner, BigInteger value) {
            this.burner = burner;
            this.value = value;
        }

        public Address getBurner() {
            return burner;
        }

        public void setBurner(Address burner) {
            this.burner = burner;
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

            BurnEvent burnEvent = (BurnEvent) o;

            if (burner != null ? !burner.equals(burnEvent.burner) : burnEvent.burner != null) return false;
            return value != null ? value.equals(burnEvent.value) : burnEvent.value == null;
        }

        @Override
        public int hashCode() {
            int result = burner != null ? burner.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "BurnEvent{" +
                    "burner=" + burner +
                    ", value=" + value +
                    '}';
        }

    }

}

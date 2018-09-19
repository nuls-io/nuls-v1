package contracts.token.ERC20;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Msg;
import io.nuls.contract.sdk.annotation.View;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static io.nuls.contract.sdk.Utils.emit;
import static io.nuls.contract.sdk.Utils.require;

public class BasicToken implements ERC20Basic {

    protected Map<Address, BigInteger> balances = new HashMap<Address, BigInteger>();

    protected BigInteger totalSupply = BigInteger.ZERO;

    @Override
    @View
    public BigInteger totalSupply() {
        return totalSupply;
    }

    @Override
    @View
    public BigInteger balanceOf(Address owner) {
        require(owner != null);
        BigInteger balance = balances.get(owner);
        if (balance == null) {
            balance = BigInteger.ZERO;
        }
        return balance;
    }

    @Override
    public boolean transfer(Address to, BigInteger value) {
        subtractBalance(Msg.sender(), value);
        addBalance(to, value);
        emit(new TransferEvent(Msg.sender(), to, value));
        return true;
    }

    protected void addBalance(Address address, BigInteger value) {
        BigInteger balance = balanceOf(address);
        check(value);
        check(balance);
        balances.put(address, balance.add(value));
    }

    protected void subtractBalance(Address address, BigInteger value) {
        BigInteger balance = balanceOf(address);
        check(balance, value);
        balances.put(address, balance.subtract(value));
    }

    protected void check(BigInteger value) {
        require(value != null && value.compareTo(BigInteger.ZERO) >= 0);
    }

    protected void check(BigInteger value1, BigInteger value2) {
        check(value1);
        check(value2);
        require(value1.compareTo(value2) >= 0);
    }

}

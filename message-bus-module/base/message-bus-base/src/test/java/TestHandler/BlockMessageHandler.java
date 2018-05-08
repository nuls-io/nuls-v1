package TestHandler;

import io.nuls.kernel.exception.NulsException;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.protocol.message.BlockMessage;

/**
 * @author: Charlie
 * @date: 2018/5/7
 */
public class BlockMessageHandler extends AbstractMessageHandler<BlockMessage> {

    @Override
    public void onMessage(BlockMessage message, String formId) throws NulsException {
        System.out.println("onMessage: this is a test funtion");
    }
}

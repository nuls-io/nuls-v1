package io.nuls.protocol.base.version;

import io.nuls.core.tools.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author: Charlie
 * @date: 2018/8/15
 */
public class ProtocolVersionHandler extends DefaultHandler {

    private static final String TX_PROTOCOL = "tx";
    private static final String MESSAGE_PROTOCOL = "message";
    private static final String PROTOCOL_ID = "id";
    private static final String PROTOCOL_CLASS = "class";

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(TX_PROTOCOL.equals(qName)){
            String id = attributes.getValue(PROTOCOL_ID);
            String className = attributes.getValue(PROTOCOL_CLASS);
            try {
                Class txClass = Class.forName(className);
                NulsVersionManager.putTxProtocol(id, txClass);
            } catch (ClassNotFoundException e) {
                Log.error(e);
            }
        }
        if(MESSAGE_PROTOCOL.equals(qName)){
            String id = attributes.getValue(PROTOCOL_ID);
            String className = attributes.getValue(PROTOCOL_CLASS);
            try {
                Class messageClass = Class.forName(className);
                NulsVersionManager.putMessageProtocol(id, messageClass);
            } catch (ClassNotFoundException e) {
                Log.error(e);
            }
        }
    }
}

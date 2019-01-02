/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
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

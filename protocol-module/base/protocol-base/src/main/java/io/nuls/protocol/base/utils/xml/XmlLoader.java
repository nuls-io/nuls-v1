package io.nuls.protocol.base.utils.xml;

import io.nuls.core.tools.cfg.ConfigLoader;
import io.nuls.core.tools.log.Log;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author: Charlie
 * @date: 2018/8/14
 */
public class XmlLoader {

        public static void loadXml(String xmlName, DefaultHandler handler) throws SAXException {
            try {
                SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
                InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(xmlName);
                saxParser.parse(inputStream, handler);
            } catch (ParserConfigurationException e) {
                Log.error(e);
            } catch (IOException e) {
                Log.error(e);
            }
        }
}

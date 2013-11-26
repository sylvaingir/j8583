/*
j8583 A Java implementation of the ISO8583 protocol
Copyright (C) 2007 Enrique Zamudio Lopez

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
*/
package com.solab.iso8583.parse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;

/** This class is used to parse a XML configuration file and configure
 * a MessageFactory with the values from it.
 * 
 * @author Enrique Zamudio
 */
public class ConfigParser {

	private final static Logger log = LoggerFactory.getLogger(ConfigParser.class);

    /** Creates a message factory configured from the default file, which is j8583.xml
   	 * located in the root of the classpath, using the specified ClassLoader. */
   	public static MessageFactory<IsoMessage> createDefault(
               ClassLoader loader) throws IOException {
   		if (loader.getResource("j8583.xml") == null) {
   			log.warn("ISO8583 ConfigParser cannot find j8583.xml, returning empty message factory");
   			return new MessageFactory<IsoMessage>();
   		} else {
   			return createFromClasspathConfig(loader, "j8583.xml");
   		}
   	}

	/** Creates a message factory configured from the default file, which is j8583.xml
	 * located in the root of the classpath, using the MessageFactory's
     * ClassLoader. */
	public static MessageFactory<IsoMessage> createDefault() throws IOException {
        return createDefault(MessageFactory.class.getClassLoader());
	}

	/** Creates a message factory from the specified path inside the classpath,
     * using the specified ClassLoader. */
	public static MessageFactory<IsoMessage> createFromClasspathConfig(
            String path) throws IOException {
        return createFromClasspathConfig(MessageFactory.class.getClassLoader(), path);
    }
    /** Creates a message factory from the specified path inside the classpath,
     * using MessageFactory's ClassLoader. */
   	public static MessageFactory<IsoMessage> createFromClasspathConfig(
               ClassLoader loader, String path) throws IOException {
		InputStream ins = loader.getResourceAsStream(path);
		MessageFactory<IsoMessage> mfact = new MessageFactory<IsoMessage>();
		if (ins != null) {
			log.debug("ISO8583 Parsing config from classpath file {}", path);
			try {
				parse(mfact, ins);
			} finally {
				ins.close();
			}
		} else {
			log.warn("ISO8583 File not found in classpath: {}", path);
		}
		return mfact;
	}

	/** Creates a message factory from the file located at the specified URL. */
	public static MessageFactory<IsoMessage> createFromUrl(URL url) throws IOException {
		MessageFactory<IsoMessage> mfact = new MessageFactory<IsoMessage>();
		InputStream stream = url.openStream();
		try {
			parse(mfact, stream);
		} finally {
			stream.close();
		}
		return mfact;
	}

	/** Reads the XML from the stream and configures the message factory with its values.
	 * @param mfact The message factory to be configured with the values read from the XML.
	 * @param stream The InputStream containing the XML configuration. */
	protected static <T extends IsoMessage> void parse(
            MessageFactory<T> mfact, InputStream stream) throws IOException {
		final DocumentBuilderFactory docfact = DocumentBuilderFactory.newInstance();
		DocumentBuilder docb = null;
		Document doc = null;
		try {
			docb = docfact.newDocumentBuilder();
			docb.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String publicId, String systemId)
						throws SAXException, IOException {
					if (systemId.contains("j8583.dtd")) {
						URL dtd = getClass().getResource("j8583.dtd");
						if (dtd == null) {
							log.warn("Cannot find j8583.dtd in classpath. j8583 config files will not be validated.");
						} else {
							return new InputSource(dtd.toString());
						}
					}
					return null;
				}
			});
			doc = docb.parse(stream);
		} catch (ParserConfigurationException ex) {
			log.error("ISO8583 Cannot parse XML configuration", ex);
			return;
		} catch (SAXException ex) {
			log.error("ISO8583 Parsing XML configuration", ex);
			return;
		}
		final Element root = doc.getDocumentElement();

		//Read the ISO headers
		NodeList nodes = root.getElementsByTagName("header");
		Element elem = null;
        boolean pass2 = false;
		for (int i = 0; i < nodes.getLength(); i++) {
			elem = (Element)nodes.item(i);
			int type = parseType(elem.getAttribute("type"));
			if (type == -1) {
				throw new IOException("Invalid type for ISO8583 header: " + elem.getAttribute("type"));
			}
			if (elem.getChildNodes() == null || elem.getChildNodes().getLength() == 0) {
                if (elem.getAttribute("ref") != null && !elem.getAttribute("ref").isEmpty()) {
                    pass2 = true;
                }
				else throw new IOException("Invalid ISO8583 header element");
			} else {
                String header = elem.getChildNodes().item(0).getNodeValue();
                if (log.isTraceEnabled()) {
                    log.trace("Adding ISO8583 header for type {}: {}", elem.getAttribute("type"), header);
                }
                mfact.setIsoHeader(type, header);
            }
		}
        if (pass2) {
            for (int i = 0; i < nodes.getLength(); i++) {
                elem = (Element)nodes.item(i);
                int type = parseType(elem.getAttribute("type"));
                if (type == -1) {
                    throw new IOException("Invalid type for ISO8583 header: "
                            + elem.getAttribute("type"));
                }
                if (elem.getAttribute("ref") != null && !elem.getAttribute("ref").isEmpty()) {
                    int t2 = parseType(elem.getAttribute("ref"));
                    if (t2 == -1) {
                        throw new IOException("Invalid type reference "
                                + elem.getAttribute("ref") + " for ISO8583 header " + type);
                    }
                    String h = mfact.getIsoHeader(t2);
                    if (h == null) {
                        throw new IllegalArgumentException("Header def " + type + " refers to nonexistent header " + t2);
                    }
                    if (log.isTraceEnabled()) {
                        log.trace("Adding ISO8583 header for type {}: {} (copied from {})",
                                elem.getAttribute("type"), h, elem.getAttribute("ref"));
                    }
                    mfact.setIsoHeader(type, h);
                }
            }
        }

		//Read the message templates
		nodes = root.getElementsByTagName("template");
		for (int i = 0; i < nodes.getLength(); i++) {
			elem = (Element)nodes.item(i);
			int type = parseType(elem.getAttribute("type"));
			if (type == -1) {
				throw new IOException("Invalid ISO8583 type for template: " + elem.getAttribute("type"));
			}
			NodeList fields = elem.getElementsByTagName("field");
            @SuppressWarnings("unchecked")
			T m = (T)new IsoMessage();
			m.setType(type);
			m.setCharacterEncoding(mfact.getCharacterEncoding());
			for (int j = 0; j < fields.getLength(); j++) {
				Element f = (Element)fields.item(j);
				int num = Integer.parseInt(f.getAttribute("num"));
				IsoType itype = IsoType.valueOf(f.getAttribute("type"));
				int length = 0;
				if (f.getAttribute("length").length() > 0) {
					length = Integer.parseInt(f.getAttribute("length"));
				}
				String v = f.getChildNodes().item(0).getNodeValue();
				CustomField<Object> _cf = mfact.getCustomField(num);
				m.setValue(num, _cf == null ? v : _cf.decodeField(v), _cf, itype, length);
			}
			mfact.addMessageTemplate(m);
		}

		//Read the parsing guides
		nodes = root.getElementsByTagName("parse");
		for (int i = 0; i < nodes.getLength(); i++) {
			elem = (Element)nodes.item(i);
			int type = parseType(elem.getAttribute("type"));
			if (type == -1) {
				throw new IOException("Invalid ISO8583 type for parse guide: " + elem.getAttribute("type"));
			}
			NodeList fields = elem.getElementsByTagName("field");
			HashMap<Integer, FieldParseInfo> parseMap = new HashMap<Integer, FieldParseInfo>();
			for (int j = 0; j < fields.getLength(); j++) {
				Element f = (Element)fields.item(j);
				int num = Integer.parseInt(f.getAttribute("num"));
				IsoType itype = IsoType.valueOf(f.getAttribute("type"));
				int length = 0;
				if (f.getAttribute("length").length() > 0) {
					length = Integer.parseInt(f.getAttribute("length"));
				}
				parseMap.put(num, FieldParseInfo.getInstance(itype, length, mfact.getCharacterEncoding()));
			}
			mfact.setParseMap(type, parseMap);
		}
	}

	/** Configures a MessageFactory using the default configuration file j8583.xml. This is useful
	 * if you have a MessageFactory created using Spring for example. */
	public static <T extends IsoMessage> void configureFromDefault(
            MessageFactory<T> mfact) throws IOException {
		if (mfact.getClass().getClassLoader().getResource("j8583.xml") == null) {
			log.warn("ISO8583 config file j8583.xml not found!");
		} else {
			configureFromClasspathConfig(mfact, "j8583.xml");
		}
	}

	/** This method attempts to open a stream from the XML configuration in the specified URL and
	 * configure the message factory from that config. */
	public static <T extends IsoMessage> void configureFromUrl(
            MessageFactory<T> mfact, URL url) throws IOException {
		InputStream stream = url.openStream();
		try {
			parse(mfact, stream);
		} finally {
			stream.close();
		}
	}

	/** Configures a MessageFactory using the configuration file at the path specified (will be searched
	 * within the classpath using the MessageFactory's ClassLoader). This is useful for configuring
	 * Spring-bound instances of MessageFactory for example. */
	public static <T extends IsoMessage> void configureFromClasspathConfig(
            MessageFactory<T> mfact, String path) throws IOException {
		InputStream ins = mfact.getClass().getClassLoader().getResourceAsStream(path);
		if (ins != null) {
			log.debug("ISO8583 Parsing config from classpath file {}", path);
			try {
				parse(mfact, ins);
			} finally {
				ins.close();
			}
		} else {
			log.warn("ISO8583 File not found in classpath: {}", path);
		}
	}

	/** Parses a message type expressed as a hex string and returns the integer number.
	 * For example, "0200" or "200" return the number 512 (0x200) */
	private static int parseType(String type) throws IOException {
		if (type.length() % 2 == 1) {
			type = "0" + type;
		}
		if (type.length() != 4) {
			return -1;
		}
		return ((type.charAt(0) - 48) << 12) | ((type.charAt(1) - 48) << 8)
			| ((type.charAt(2) - 48) << 4) | (type.charAt(3) - 48);
	}

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.libis.primo.api.resource.helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.xml.dtm.ref.DTMNodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author mehmetc
 */
public class XMLHelper {

    public static synchronized String toString(Document xml) {
        String xmlString = "";

        try {
            Transformer transformer;
            transformer = TransformerFactory.newInstance().newTransformer();

            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(xml);
            transformer.transform(source, result);
            xmlString = result.getWriter().toString();

        } catch (Exception ex) {
            Logger.getLogger(XMLHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return xmlString;
    }

    public static synchronized Document toXml(String str) {
        Document xml = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            //InputSource is = new InputSource();
            //is.setCharacterStream(new StringReader(str));
            //is.setEncoding("UTF-8");
            //Document doc = builder.parse(is);
                         
            InputStream  is = new ByteArrayInputStream(str.getBytes());
            Document doc = builder.parse(is);

            
            xml = doc;

        } catch (SAXException ex) {
            Logger.getLogger(XMLHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XMLHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XMLHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return xml;
    }

/*    
    public static synchronized String xpath2(Document xml, String x) {
        String result = null;
        JXPathContext context = JXPathContext.newContext(xml);
                
        return (String) context.selectSingleNode(x).toString();
    }*/
   
    public static synchronized String xpath(Document xml, String x) {
        String result = null;
        if (xml != null) {
            try {
                XPath xpath = XPathFactory.newInstance().newXPath();                                              
                result = XMLHelper.toString(XMLHelper.xmlFragment2Document(((DTMNodeList)xpath.evaluate(x, xml, XPathConstants.NODESET)).item(0)));                
            } catch (Exception ex) {
                Logger.getLogger(XMLHelper.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return result;
    }

    public static synchronized NodeList xpathReturnNodeList(Document xml, String x) {
        NodeList result = null;
        if (xml != null) {
            try {
                XPath xpath = XPathFactory.newInstance().newXPath();                                              
                result = (NodeList) xpath.evaluate(x, xml, XPathConstants.NODESET);                
            } catch (Exception ex) {
                Logger.getLogger(XMLHelper.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return result;
    }    
    
    public static synchronized NodeList appendXml(Document xml, NodeList nodeList, String fragmentString) {
        NodeList result = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Element element = db.parse(new ByteArrayInputStream(fragmentString.getBytes())).getDocumentElement();
            Node fragment = xml.importNode(element, true);

            nodeList.item(0).getParentNode().appendChild(fragment);
        } catch (Exception ex) {
            Logger.getLogger(XMLHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }


    public static synchronized Document xmlFragment2Document(Node recordRoot) {
        Document newDoc = null;
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer xf = tf.newTransformer();
            DOMResult dr = new DOMResult();
            xf.transform(new DOMSource(recordRoot), dr);
            newDoc = (Document) dr.getNode();
        } catch (TransformerException ex) {
            Logger.getLogger(XMLHelper.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return newDoc;
    }

    public static synchronized String xpathTextContent(Document xml, String xpath) {
        String text = xpathTextContent(xml, xpath, 0);
        return text;
    }

    public static synchronized String xpathTextContent(Document xml, String xpath, int index) {
        String text = "";
        NodeList nl = xpathReturnNodeList(xml, xpath);
        if (nl != null && nl.getLength() > 0) {
            try {
                text = new String(nl.item(index).getTextContent().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(XMLHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return text;
    }

}

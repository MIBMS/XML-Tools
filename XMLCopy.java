package xmlTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 *  common XML copy methods
 */


class XMLCopy {
	private static final Logger LOGGER = Logger.getLogger( XMLCopy.class.getName() );
	
	private XMLCopy(){}
	
	/**
	 * Adds a subtree at the first node with the given Xpath
	 * @param newDoc
	 * @param xPathExp given XPath
	 * @param newSubTree Subtree to be added
	 * @param args
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	static void addSubTree(Document newDoc, String xPathExp, String newSubTree) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException{
		InputSource addUDFSource = new InputSource(new StringReader(newSubTree));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document addUDFDoc = builder.parse(addUDFSource);


		Node toBeCopiedEntity = newDoc.importNode(addUDFDoc.getDocumentElement(), true);
		XPath xPath = XPathFactory.newInstance().newXPath();
		Node anchorNode = ((NodeList) xPath.compile(xPathExp).evaluate(newDoc, XPathConstants.NODESET)).item(0);
		anchorNode.appendChild(toBeCopiedEntity);
		LOGGER.info("Added " + newSubTree + " to " + xPathExp);
	}
	
	
	
	/**
	 * change text nodes for all nodes with the given XPath
	 * @param doc
	 * @param xPathExp
	 * @param toChange
	 * @throws XPathExpressionException
	 */
	static void changeTextNode(Document doc, String xPathExp, String toChange) throws XPathExpressionException{
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.compile(xPathExp).evaluate(doc, XPathConstants.NODESET);
		if (nodeList.getLength() == 0){
			LOGGER.info("No nodes to replace.");
		}				
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node fieldValueNode = nodeList.item(i);
			for (int j = 0; j < fieldValueNode.getChildNodes().getLength(); j++){
				Node childNode = fieldValueNode.getChildNodes().item(i);
				if (childNode.getNodeType() == Node.TEXT_NODE) {
					String oldValue = childNode.getNodeValue();
					childNode.setNodeValue(toChange);
					LOGGER.info("Changed " + xPathExp + " from " + oldValue + " to " + toChange);
				}
			}
		}
	}
	
	/**
	 * Removes nodes and children of the given XPath - can remove multiple nodes with the same XPath
	 * @param doc
	 * @param xPathExp
	 * @throws XPathExpressionException
	 */
	static void removeXPaths(Document doc, String xPathExp) throws XPathExpressionException{
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.compile(xPathExp).evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node udfNode = nodeList.item(i);
			udfNode.getParentNode().removeChild(udfNode);
			LOGGER.info("Removed " + xPathExp);
		}  
	}
	
	/**
	 * creates an ByteOutputStream representing an XML file from an XML document
	 * @param newDoc
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 */
	static ByteArrayOutputStream copyXML(Document newDoc, String customXML) 
			throws SAXException, IOException, XPathExpressionException, TransformerException, ParserConfigurationException{	
		// write the content into Xml file
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		newDoc.setXmlStandalone(true);
	    DOMSource source = new DOMSource(newDoc);
	    try ( 
	    		Writer writer = new OutputStreamWriter(byteOutput);
	    		StringWriter strWriter = new StringWriter();
	    	){
	    	
		    StreamResult result = new StreamResult(strWriter);

		    TransformerFactory transformerFactory = TransformerFactory.newInstance();
		    Transformer transformer = transformerFactory.newTransformer();
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		    //remove normal XML declaration
		    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    transformer.transform(source, result);
		    //add a custom xml declaration
		    writer.write(customXML + strWriter.toString());
	    }
		return byteOutput;
	}
}

package xmlTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 *  common XML copy methods
 */


public class XMLCopy {
	private static final Logger LOGGER = Logger.getLogger( XMLCopy.class.getName() );
	
	private XMLCopy(){}
	
	/**
	 * Copies XML document
	 * @param ruleIn
	 * @param args
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 */
	public static Document copyDoc(InputStream ruleIn) 
			throws SAXException, IOException, XPathExpressionException, TransformerException, ParserConfigurationException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		XPath xPath = XPathFactory.newInstance().newXPath();
		
		//parses the old document
		Document doc = builder.parse(ruleIn);
		Element rootElement = doc.getDocumentElement();
		StringBuilder rootElementOpeningTag = new StringBuilder(rootElement.getTagName());
		int numAttrs = rootElement.getAttributes().getLength();
		for (int i = 0; i < numAttrs; i++){
			Node attr = rootElement.getAttributes().item(i);
			String attrName = attr.getNodeName();
			String attrValue = attr.getNodeValue();
			rootElementOpeningTag.append(" " + attrName + "=" + "\"" + attrValue + "\"");
		}
		rootElementOpeningTag.append(">").insert(0, "<");
		
		StringBuilder rootElementClosingTag = new StringBuilder(rootElement.getTagName()).append(">").insert(0, "</");
		
		//creates a new doc
		String newDocXmlDecl = rootElementOpeningTag.append(rootElementClosingTag).toString();
		InputSource newDocSource = new InputSource(new StringReader(newDocXmlDecl));
		Document newDoc = builder.parse(newDocSource);
		
		//copy XML structure
		String xPathExp = rootElement.getTagName();
		NodeList nodeList = (NodeList) xPath.compile(xPathExp).evaluate(doc, XPathConstants.NODESET);
		Node mxmlNode = nodeList.item(0);
		NodeList level2Nodes = mxmlNode.getChildNodes();
		for (int i = 0; i < level2Nodes.getLength(); i++) {
			Node level2Node = level2Nodes.item(i);
			Node newLevel2Node = newDoc.importNode(level2Node, true);
			newDoc.getDocumentElement().appendChild(newLevel2Node);		
		}
		
		return newDoc;
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
	public static ByteArrayOutputStream copyXML(Document newDoc, String customXML) 
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
	public static void addSubTree(Document newDoc, String xPathExp, String newSubTree) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException{
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
	public static void changeTextNode(Document doc, String xPathExp, String toChange) throws XPathExpressionException{
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
	public static void removeXPaths(Document doc, String xPathExp) throws XPathExpressionException{
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.compile(xPathExp).evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node udfNode = nodeList.item(i);
			udfNode.getParentNode().removeChild(udfNode);
			LOGGER.info("Removed " + xPathExp);
		} 
		if (nodeList.getLength() == 0) LOGGER.info("Removed no node as there are none matching XPath" + xPathExp);
	}
	
	/**
	 * Checks if there are existing nodes with the given XPath
	 * @param doc
	 * @param xPathExp
	 * @throws XPathExpressionException
	 */
	public static int checkXPath(Document doc, String xPathExp) throws XPathExpressionException{
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.compile(xPathExp).evaluate(doc, XPathConstants.NODESET);
		return nodeList.getLength();
	}
	
	
}

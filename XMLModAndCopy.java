package xmlTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
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
 * Modifies and copies a single XML file
 */
class XMLModAndCopy{
	private static final Logger LOGGER = Logger.getLogger( XMLModAndCopy.class.getName() );
	
	/**
	 * private no-arg constructor - cannot be instantiated
	 * @throws ParserConfigurationException
	 */
	private XMLModAndCopy() {
	}
	
	
	/**
	 * Copies a single XML
	 * @param ruleMap
	 * @return 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 * @throws TransformerException 
	 * @throws ParserConfigurationException 
	 */
	public static ByteArrayOutputStream copyXML(InputStream ruleIn, HashMap<String, String> args) throws SAXException, IOException, XPathExpressionException, TransformerException, ParserConfigurationException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		XPath xPath = XPathFactory.newInstance().newXPath();
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		//convert to input streams
		Document doc = builder.parse(ruleIn);
		String newDocXmlDecl = "<MxML version=\"1-1\"></MxML>";
		InputSource newDocSource = new InputSource(new StringReader(newDocXmlDecl));
		Document newDoc = builder.parse(newDocSource);
		//copy XML structure
		
		String xPathExp = "MxML";
		NodeList nodeList = (NodeList) xPath.compile(xPathExp).evaluate(doc, XPathConstants.NODESET);
		Node mxmlNode = nodeList.item(0);
		NodeList level2Nodes = mxmlNode.getChildNodes();
		for (int i = 0; i < level2Nodes.getLength(); i++) {
			Node level2Node = level2Nodes.item(i);
			Node newLevel2Node = newDoc.importNode(level2Node, true);
			newDoc.getDocumentElement().appendChild(newLevel2Node);		
		}
		
		//call ModifyXML to modify the new XML document
		modifyXML(newDoc, args);
					
		// write the content into Xml file
		newDoc.setXmlStandalone(true);
	    DOMSource source = new DOMSource(newDoc);
	    try ( 
	    		Writer writer = new OutputStreamWriter(byteOutput);
	    		StringWriter strWriter = new StringWriter();
	    	){
	    	//add a custom xml declaration
		    String customXML = "<?xml version=\"1.0\"?>\n";
		    StreamResult result = new StreamResult(strWriter);

		    TransformerFactory transformerFactory = TransformerFactory.newInstance();
		    Transformer transformer = transformerFactory.newTransformer();
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		    //remove normal XML declaration
		    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    transformer.transform(source, result);
		    writer.write(customXML + strWriter.toString());
	    }
		return byteOutput;
	}
	
	/**
	 * Modifies a single XML
	 * @throws XPathExpressionException to CopyXML
	 * @throws IOException to CopyXML
	 * @throws SAXException to CopyXML
	 * @throws ParserConfigurationException 
	 */
	private static void modifyXML(Document newDoc, HashMap<String, String> args) throws SAXException, IOException, XPathExpressionException, ParserConfigurationException{
		Document docWithAddedNodes = null;
		if (args.get("modify") != null){
			switch (args.get("modify")){
			case "Tick Additional Entity":
				removeXPaths(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField"
						+ "[fieldLabel=\"FilterDetails\"]");
				addUDF(newDoc, docWithAddedNodes, args);
				break;
			case "Tick A Different Entity":
				changeUDF(newDoc, args);
				break;
			default:
				LOGGER.info("Nothing has been done to this XML.");
			}
		}
	}
	
	private static void changeUDF(Document doc, HashMap<String, String> args) throws XPathExpressionException{
		XPath xPath = XPathFactory.newInstance().newXPath();
		String xPathExp = "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField[fieldValue=\""
				+ args.get("fromEntity") + "\" and fieldLabel=\"TrnEntity\"]/fieldValue";
		NodeList nodeList = (NodeList) xPath.compile(xPathExp).evaluate(doc, XPathConstants.NODESET);
		if (nodeList.getLength() == 0){
			LOGGER.info("No nodes to replace.");
		}				
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node fieldValueNode = nodeList.item(i);
			for (int j = 0; j < fieldValueNode.getChildNodes().getLength(); j++){
				Node childNode = fieldValueNode.getChildNodes().item(i);
				if (childNode.getNodeType() == Node.TEXT_NODE) {
					childNode.setNodeValue(args.get("toEntity"));
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
	private static void removeXPaths(Document doc, String xPathExp) throws XPathExpressionException{
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.compile(xPathExp).evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node udfNode = nodeList.item(i);
			udfNode.getParentNode().removeChild(udfNode);
		}  
	}
		
	
	private static void addUDF(Document newDoc, Document docWithAddedNodes, HashMap<String, String> args) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException{
		InputSource addUDFSource = new InputSource(new StringReader(addAdditionalEntity(args.get("entity"))));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document addUDFDoc = builder.parse(addUDFSource);


		Node toBeCopiedEntity = newDoc.importNode(addUDFDoc.getDocumentElement(), true);
		XPath xPath = XPathFactory.newInstance().newXPath();
		String xPathExp = "MxML/mxAccountingIRULESet/mxAccountingIRULE";
		Node mxAccountingIRULENode = ((NodeList) xPath.compile(xPathExp).evaluate(newDoc, XPathConstants.NODESET)).item(0);
		mxAccountingIRULENode.appendChild(toBeCopiedEntity);
	}
	
	private static String addAdditionalEntity(String entity){
		//add the additional entity into the document
		
		String additionalEntity = ""
				+ "<userDefinedField>"
				+ "<fieldLabel>FilterDetails</fieldLabel>"
				+ "<mxAccountingIRULE_FilterDetails>"
				+ "<mxAccountingIRULE_FILTER_DETAIL>"
				+ "<businessObjectId mefClass=\"mxAccountingIRULE_FILTER_DETAIL\">"
				+ "<primarySystem>MX</primarySystem>"
				+ "</businessObjectId>"
				+ "<userDefinedField>"
				+ "<fieldLabel>FieldType</fieldLabel>"
				+ "<fieldValue>0</fieldValue>"
				+ "<fieldType>integer</fieldType>"
				+ "</userDefinedField>"
				+ "<userDefinedField>"
				+ "<fieldLabel>FieldValue</fieldLabel>"
				+ "<fieldValue>" + entity + "</fieldValue>"
				+ "<fieldType>character</fieldType>"
				+ "</userDefinedField>"
				+ "</mxAccountingIRULE_FILTER_DETAIL>"
				+ "</mxAccountingIRULE_FilterDetails>"
				+ "</userDefinedField>";
		return additionalEntity;

	}
	
}


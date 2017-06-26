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
class CTTXMLCopy{
	private static final Logger LOGGER = Logger.getLogger( CTTXMLCopy.class.getName() );
	
	/**
	 * private no-arg constructor - cannot be instantiated
	 * @throws ParserConfigurationException
	 */
	private CTTXMLCopy() {}
	
	
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
	public static Document copyDoc(InputStream ruleIn, HashMap<String, String> args) 
			throws SAXException, IOException, XPathExpressionException, TransformerException, ParserConfigurationException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		XPath xPath = XPathFactory.newInstance().newXPath();
		
		//parses the old document
		Document doc = builder.parse(ruleIn);
		
		//creates a new doc
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
		
		return newDoc;
	}
	
	
	
	/**
	 * Modifies a single XML
	 * @throws XPathExpressionException to CopyXML
	 * @throws IOException to CopyXML
	 * @throws SAXException to CopyXML
	 * @throws ParserConfigurationException 
	 */
	private static void modifyXML(Document newDoc, HashMap<String, String> args) throws SAXException, IOException, XPathExpressionException, ParserConfigurationException{
		if (args.get("selection") != null){
			switch (args.get("selection")){
			case "Add Entity":
				XMLCopy.removeXPaths(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField"
						+ "[fieldLabel=\"FilterDetails\"]");
				XMLCopy.addSubTree(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE", addAdditionalEntity(args.get("entity")));
				break;
			case "Change Entity":
				XMLCopy.changeTextNode(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField[fieldValue=\""
						+ args.get("fromEntity") + "\" and fieldLabel=\"TrnEntity\"]/fieldValue", args.get("toEntity"));
				break;
			default:
				LOGGER.info("Nothing has been done to this XML.");
			}
		}
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


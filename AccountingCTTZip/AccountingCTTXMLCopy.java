package xmlTools.AccountingCTTZip;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.util.Scanner;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import xmlTools.XMLCopy;

/**
 * Modifies and copies a single XML file
 */
class AccountingCTTXMLCopy{
	private static final Logger LOGGER = Logger.getLogger( AccountingCTTXMLCopy.class.getName() );
	
	/**
	 * private no-arg constructor - cannot be instantiated
	 * @throws ParserConfigurationException
	 */
	private AccountingCTTXMLCopy() {}
	
	/**
	 * Modifies a single XML
	 * @throws XPathExpressionException to CopyXML
	 * @throws IOException to CopyXML
	 * @throws SAXException to CopyXML
	 * @throws ParserConfigurationException 
	 */
	static Document modifyXML(Document newDoc, HashMap<String, String> args) throws SAXException, IOException, XPathExpressionException, ParserConfigurationException{
		if (args.get("selection") != null){
			switch (args.get("selection")){
			case "Edit entities":
				editEntities(newDoc, args);
				break;
			case "Edit accounting sections":
				editAccountingSections(newDoc, args);
				//XMLCopy.changeTextNode(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField[fieldValue=\""
						//+ args.get("accountingSections") + "\" and fieldLabel=\"TrnEntity\"]/fieldValue", args.get("toEntity"));
				break;
			default:
				LOGGER.info("Nothing has been done to this XML.");
			}
		}
		return newDoc;
	}
	
	private static void editEntities(Document newDoc, HashMap<String, String> args) throws SAXException, IOException, XPathExpressionException, ParserConfigurationException{
		XMLCopy.removeXPaths(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField[fieldLabel=\"TrnEntity\"]");
		XMLCopy.removeXPaths(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField/mxAccountingIRULE_FilterDetails/"
				+ "mxAccountingIRULE_FILTER_DETAIL[userDefinedField[fieldLabel=\"FieldType\"]/fieldValue=\"0\"]");
		try(Scanner scanner = new Scanner(args.get("entities"))){
			if (scanner.hasNextLine()){
				String firstEntity = scanner.nextLine();
				XMLCopy.addSubTree(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE", addMainEntity(firstEntity));
			}
			
			while (scanner.hasNextLine()) {
				String additionalEntity = scanner.nextLine();
				if (XMLCopy.checkXPath(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField/mxAccountingIRULE_FilterDetails") == 0){
					XMLCopy.addSubTree(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE", addAdditionalEntity(additionalEntity));
				}
				else{
					XMLCopy.addSubTree(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField/mxAccountingIRULE_FilterDetails", 
							addMoreAdditionalEntities(additionalEntity));
				}
			}
		}		
	}
	
	private static void editAccountingSections(Document newDoc, HashMap<String, String> args) throws SAXException, IOException, XPathExpressionException, ParserConfigurationException{
		XMLCopy.removeXPaths(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField[fieldLabel=\"TrnSection\"]");
		XMLCopy.removeXPaths(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField/mxAccountingIRULE_FilterDetails/"
				+ "mxAccountingIRULE_FILTER_DETAIL[userDefinedField[fieldLabel=\"FieldType\"]/fieldValue=\"1\"]");
		try(Scanner scanner = new Scanner(args.get("accountingSections"))){
			if (scanner.hasNextLine()){
				String firstSection = scanner.nextLine();
				XMLCopy.addSubTree(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE", addMainSection(firstSection));
			}
			
			while (scanner.hasNextLine()) {
				String additionalSection = scanner.nextLine();
				if (XMLCopy.checkXPath(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField/mxAccountingIRULE_FilterDetails") == 0){
					XMLCopy.addSubTree(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE", addAdditionalSection(additionalSection));
				}
				else{
					XMLCopy.addSubTree(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField/mxAccountingIRULE_FilterDetails", 
							addMoreAdditionalSections(additionalSection));
				}
			}
		}		
	}
		
	private static String addMainEntity(String entity){
		//add the additional entity into the document
		
		String additionalEntity = ""
				+ "<userDefinedField>"
				+ "<fieldLabel>TrnEntity</fieldLabel>"
				+ "<fieldValue>" + entity + "</fieldValue>"
				+ "<fieldType>character</fieldType>"
				+ "</userDefinedField>";
		return additionalEntity;

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
	
	private static String addMoreAdditionalEntities(String entity){
		//add the additional entity into the document
		
		String additionalEntity = ""
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
				+ "</mxAccountingIRULE_FILTER_DETAIL>";
		return additionalEntity;

	}
	
	
	private static String addMainSection(String section){
		//add the additional entity into the document
		
		String additionalSection = ""
				+ "<userDefinedField>"
				+ "<fieldLabel>TrnSection</fieldLabel>"
				+ "<fieldValue>" + section + "</fieldValue>"
				+ "<fieldType>character</fieldType>"
				+ "</userDefinedField>";
		return additionalSection;

	}
	
	private static String addAdditionalSection(String section){
		//add the additional entity into the document
		
		String additionalSection = ""
				+ "<userDefinedField>"
				+ "<fieldLabel>FilterDetails</fieldLabel>"
				+ "<mxAccountingIRULE_FilterDetails>"
				+ "<mxAccountingIRULE_FILTER_DETAIL>"
				+ "<businessObjectId mefClass=\"mxAccountingIRULE_FILTER_DETAIL\">"
				+ "<primarySystem>MX</primarySystem>"
				+ "</businessObjectId>"
				+ "<userDefinedField>"
				+ "<fieldLabel>FieldType</fieldLabel>"
				+ "<fieldValue>1</fieldValue>"
				+ "<fieldType>integer</fieldType>"
				+ "</userDefinedField>"
				+ "<userDefinedField>"
				+ "<fieldLabel>FieldValue</fieldLabel>"
				+ "<fieldValue>" + section + "</fieldValue>"
				+ "<fieldType>character</fieldType>"
				+ "</userDefinedField>"
				+ "</mxAccountingIRULE_FILTER_DETAIL>"
				+ "</mxAccountingIRULE_FilterDetails>"
				+ "</userDefinedField>";
		return additionalSection;

	}
	
	private static String addMoreAdditionalSections(String section){
		//add the additional entity into the document
		
		String additionalSection = ""
				+ "<mxAccountingIRULE_FILTER_DETAIL>"
				+ "<businessObjectId mefClass=\"mxAccountingIRULE_FILTER_DETAIL\">"
				+ "<primarySystem>MX</primarySystem>"
				+ "</businessObjectId>"
				+ "<userDefinedField>"
				+ "<fieldLabel>FieldType</fieldLabel>"
				+ "<fieldValue>1</fieldValue>"
				+ "<fieldType>integer</fieldType>"
				+ "</userDefinedField>"
				+ "<userDefinedField>"
				+ "<fieldLabel>FieldValue</fieldLabel>"
				+ "<fieldValue>" + section + "</fieldValue>"
				+ "<fieldType>character</fieldType>"
				+ "</userDefinedField>"
				+ "</mxAccountingIRULE_FILTER_DETAIL>";
		return additionalSection;

	}
	
}


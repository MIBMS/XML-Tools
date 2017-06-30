package xmlTools.AccountingCTTZip;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.util.Scanner;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import xmlTools.XMLCopy;

/**
 * Provides static methods for copying a single accounting CTT XML file
 */
class AccountingCTTXMLCopy{
	private static final Logger LOGGER = Logger.getLogger( AccountingCTTXMLCopy.class.getName() );
	
	/**
	 * private no-arg constructor - cannot be instantiated
	 * @throws ParserConfigurationException
	 */
	private AccountingCTTXMLCopy() {}
	
	/**
	 * Modifies the XML based on the args map passed to the method
	 * @throws XPathExpressionException to CopyXML
	 * @throws IOException to CopyXML
	 * @throws SAXException to CopyXML
	 * @throws ParserConfigurationException
	 * @return Document that has been modified
	 */
	static Document modifyXML(Document newDoc, Map<String, String> map) throws SAXException, IOException, XPathExpressionException, ParserConfigurationException{
		boolean modified = false;
		if (map.get("entitiesSelected") != null && map.get("entitiesSelected").equals("true")){
			editEntities(newDoc, map);
			modified = true;
		}
		if (map.get("sectionsSelected") != null && map.get("sectionsSelected").equals("true")){
			editAccountingSections(newDoc, map);
			modified = true;
		}
		if (map.get("miscSelected") != null && map.get("miscSelected").equals("true")){
			editMisc(newDoc, map);
			modified = true;
		}
		if (!modified) LOGGER.info("Nothing has been done to this XML.");
		return newDoc;
	}
	
	/**
	 * Method for editing entities of the accounting XML
	 * @param newDoc - document to be edited
	 * @param map - map of arguments from the CopyClass object
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 */
	private static void editEntities(Document newDoc, Map<String, String> map) throws SAXException, IOException, XPathExpressionException, ParserConfigurationException{
		XMLCopy.removeXPaths(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField[fieldLabel=\"TrnEntity\"]");
		XMLCopy.removeXPaths(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField/mxAccountingIRULE_FilterDetails/"
				+ "mxAccountingIRULE_FILTER_DETAIL[userDefinedField[fieldLabel=\"FieldType\"]/fieldValue=\"0\"]");
		try(Scanner scanner = new Scanner(map.get("entities"))){
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
	
	/**
	 * Method for editing accounting sections of the XML
	 * @param newDoc - document to be edited
	 * @param map - map of arguments from the CopyClass object
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 */
	private static void editAccountingSections(Document newDoc, Map<String, String> map) throws SAXException, IOException, XPathExpressionException, ParserConfigurationException{
		XMLCopy.removeXPaths(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField[fieldLabel=\"TrnSection\"]");
		XMLCopy.removeXPaths(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField/mxAccountingIRULE_FilterDetails/"
				+ "mxAccountingIRULE_FILTER_DETAIL[userDefinedField[fieldLabel=\"FieldType\"]/fieldValue=\"1\"]");
		try(Scanner scanner = new Scanner(map.get("accountingSections"))){
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
	
	/**
	 * Method for editing a UDF of the accounting XML
	 * @param newDoc - document to be edited 
	 * @param map - map of arguments from the CopyClass object
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 */
	private static void editMisc(Document newDoc, Map<String, String> map) throws SAXException, IOException, XPathExpressionException, ParserConfigurationException{
		XMLCopy.removeXPaths(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE/userDefinedField[fieldLabel=\""
				+ map.get("udfLabel") + "\"]");
		XMLCopy.addSubTree(newDoc, "MxML/mxAccountingIRULESet/mxAccountingIRULE", map.get("subTree"));	
	}
	
	/**
	 * Creates the string of an XML subtree representing a main entity
	 * @param entity
	 * @return
	 */
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
	
	/**
	 * Creates the string of an XML subtree representing an additional entity if additional filter node does not exist
	 * @param entity
	 * @return
	 */
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
	
	/**
	 * Creates the string of an XML subtree representing an additional entity if additional filter node already exists
	 * @param entity
	 * @return
	 */
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
	
	/**
	 * Creates the string of an XML subtree representing a main section
	 * @param section
	 * @return
	 */
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
	
	/**
	 * Creates the string of an XML subtree representing an additional section if additional filter node does not exist
	 * @param section
	 * @return
	 */
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
	
	/**
	 * Creates the string of an XML subtree representing an additional section if additional filter node already exists
	 * @param section
	 * @return
	 */
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


package xmlTools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * a CopyClass representing an XML file to be copied
 */
public class XMLObject extends CopyClass {
	private static final Logger LOGGER = Logger.getLogger( XMLObject.class.getName() );
	private static ArrayList<File> createdFiles = new ArrayList<>();
	private File inputXML;
	private Path outputFolder;
	private String selection;
	private int numCopies;
	private String xPath;
	private String replaceText;
	//stores number of start copying methods started
	public static int copiesInProgress = 0;
	
	/**
	 * creates a XMLObject
	 * allows only the given arguments for this subclass
	 * sets the extensions that can be accepted by the enclosing CopyPane to xml files
	 */
	public XMLObject(){
		initArgs(new ArrayList<String>(Arrays.asList("selection", "input", "output", "numCopies", "xPath", "replaceText")));
		setExtensions(new ArrayList<String>(Arrays.asList("xml")));
	}

	@Override
	public void abortCopy() {
		clearTemp();		
	}
	
	/**
	 * Clears temporary files if copy operation is interrupted
	 */
	private static void clearTemp(){
		for (int i = 0; i < createdFiles.size(); i++){
			if (createdFiles.get(i).exists()){
				createdFiles.get(i).delete();
				LOGGER.info("Temporary file \"" 
						+ Paths.get(".").toAbsolutePath().relativize(Paths.get(createdFiles.get(i).getPath()).toAbsolutePath()) 
						+ "\" is deleted.");
			}
		}
		createdFiles.clear();
	}

	@Override
	public int startCopying() throws IOException, URISyntaxException, XPathExpressionException,
			ParserConfigurationException, SAXException, TransformerException {
		//increments number of copies in progress
		copiesInProgress++;
		inputXML = new File(getArgs("input"));
		outputFolder = Paths.get(getArgs("output"));
		selection = getArgs("selection");
		xPath = getArgs("xPath");
		System.out.println(xPath);
		replaceText = getArgs("replaceText");
		int j = 0;
		
		if (selection != null){
			switch (getArgs("selection")){
			case "Make multiple copies":
				try{
					numCopies = Integer.parseInt(getArgs("numCopies"));
				}
				catch (NumberFormatException e){
					LOGGER.warning("Could not parse number of copies. Defaulted to 0 copies");
				}
				
				j = makeCopies();
				break;
			case "Replace text nodes":
				j = replaceText();
				break;
			default:
				LOGGER.info("Nothing has been done to this XML.");
			}
		}
		//decrements number of copies in progress
		copiesInProgress--;
		return j;
	}
	
	@Override
	public int copiesInProgress(){
		return copiesInProgress;
	}
	
	/**
	 * makes copies of the file
	 * @return number of copies made
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 */
	public int makeCopies() throws XPathExpressionException, SAXException, IOException, TransformerException, ParserConfigurationException {
		int j = 0;
		for (int i = 0; i < numCopies; i++){
			Path outputFilePath = Paths.get(outputFolder.toAbsolutePath().toString(), i + inputXML.getName());
			File outputFile = new File(outputFilePath.toAbsolutePath().toString());
			try(FileOutputStream outputStream = new FileOutputStream(outputFile)){
				FileInputStream inputStream = new FileInputStream(inputXML);
				ByteArrayOutputStream byteStream = XMLCopy.copyXML(XMLCopy.copyDoc(inputStream), "");
				byteStream.writeTo(outputStream);
			}
			LOGGER.info("Created file " + Paths.get(".").toAbsolutePath().relativize(outputFilePath.toAbsolutePath()));
			createdFiles.add(outputFile);
			j++;
		}
		createdFiles.clear();
		return j;
	}
	
	/**
	 * Creates multiple copies of the XML, replacing the chosen XPath with each line in the given input replaceText from the enclosing CopyPane
	 * @return number of copies created
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 * 
	 */
	public int replaceText() throws FileNotFoundException, IOException, XPathExpressionException, SAXException, TransformerException, ParserConfigurationException{
		int j =0;
		try(Scanner scanner = new Scanner(replaceText)){
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				Path outputFilePath = Paths.get(outputFolder.toAbsolutePath().toString(), j + inputXML.getName());
				File outputFile = new File(outputFilePath.toAbsolutePath().toString());
				try(FileOutputStream outputStream = new FileOutputStream(outputFile)){
					FileInputStream inputStream = new FileInputStream(inputXML);
					Document document = XMLCopy.copyDoc(inputStream);
					XMLCopy.changeTextNode(document, xPath, line);
					ByteArrayOutputStream byteStream = XMLCopy.copyXML(document, "");
					byteStream.writeTo(outputStream);
				}
				LOGGER.info("Created file " + Paths.get(".").toAbsolutePath().relativize(outputFilePath.toAbsolutePath()));
				createdFiles.add(outputFile);
				j++;
			}
		}
		createdFiles.clear();
		return j;
	}

}

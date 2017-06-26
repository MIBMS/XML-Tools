package xmlTools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

class XMLObject extends CopyableClass {
	private static final Logger LOGGER = Logger.getLogger( XMLObject.class.getName() );
	private static ArrayList<File> createdFiles = new ArrayList<>();
	
	XMLObject(){}

	@Override
	public void abortCopy() {
		clearTemp();		
	}
	
	/**
	 * Clears temporary files
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
		File inputXML = new File(args.get("input"));
		Path outputFolder = Paths.get(args.get("output"));
		int j = 0;
		for (int i = 0; i < Integer.parseInt(args.get("numCopies")); i++){
			Path outputFilePath = Paths.get(outputFolder.toAbsolutePath().toString(), i + inputXML.getName());
			File outputFile = new File(outputFilePath.toAbsolutePath().toString());
			try(FileOutputStream outputStream = new FileOutputStream(outputFile)){
				FileInputStream inputStream = new FileInputStream(inputXML);
				ByteArrayOutputStream byteStream = XMLCopy.copyXML(modifyXML(XMLCopy.copyDoc(inputStream), args, String.valueOf(i)), "");
				byteStream.writeTo(outputStream);
			};
			LOGGER.info("Created file " + Paths.get(".").toAbsolutePath().relativize(outputFilePath.toAbsolutePath()));
			createdFiles.add(outputFile);
			j++;
		}
		createdFiles.clear();
		return j;
	}
	
	/**
	 * modify a given XPath
	 * @param newDoc
	 * @param args
	 * @param toChange
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 */
	private static Document modifyXML(Document newDoc, HashMap<String, String> args, String toChange) throws SAXException, IOException, XPathExpressionException, ParserConfigurationException{
		XMLCopy.changeTextNode(newDoc, args.get("xPath"), toChange);
		return newDoc;
	}

}

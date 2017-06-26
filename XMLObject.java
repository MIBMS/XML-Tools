package xmlTools;

import java.io.File;
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
		File inputZip = new File(args.get("input"));
		Path outputFolder = Paths.get(args.get("output"));
		int j = 0;
		for (int i = 0; i < Integer.parseInt(args.get("numCopies")); i++){
			Path outputFilePath = Paths.get(outputFolder.toAbsolutePath().toString(), i + inputZip.getName());
			File outputFile = new File(outputFilePath.toAbsolutePath().toString());
			try(FileOutputStream outputStream = new FileOutputStream(outputFile)){
				
			};
			LOGGER.info("Created file " + Paths.get(".").toAbsolutePath().relativize(outputFilePath.toAbsolutePath()));
			createdFiles.add(outputFile);
			j++;
		}
		createdFiles.clear();
		return j;
	}

}

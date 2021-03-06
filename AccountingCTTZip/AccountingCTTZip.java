package xmlTools.AccountingCTTZip;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import xmlTools.CopyClass;
import xmlTools.XMLCopy;
import xmlTools.AccountingCTTZip.AccountingCTTXMLCopy;

/**
 * Unzips and rezips CTTs
 */
public class AccountingCTTZip extends CopyClass{
	private static final Logger LOGGER = Logger.getLogger( AccountingCTTZip.class.getName() );
	final static int BUFFER_SIZE = 4096;
	//stores created files for easy deletion during copy abortion
	private static List<File> createdFiles = new ArrayList<>();
	//stores number of start copying methods started
	public static int copiesInProgress = 0;
	
	/**
	 * Creates an object representing an accounting CTT zip
	 */
	public AccountingCTTZip() {
		initArgs(new ArrayList<String>(Arrays.asList("entitiesSelected", "sectionsSelected", "input", 
				"output", "entities", "accountingSections", "miscSelected", "udfLabel", "subTree")));
		setExtensions(new ArrayList<String>(Arrays.asList("zip")));
	}
	

	
	@Override
	public int startCopying() throws IOException, URISyntaxException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerException{
		//increments number of copies in progress
		copiesInProgress++;
		HashMap<Path, ByteArrayOutputStream> copiedXMLs;
		rezipFile(copiedXMLs = processingXMLs(unzipFile()));
		//decrements number of copies in progress
		copiesInProgress--;
		return copiedXMLs.size();	
	}
	
	@Override
	public int copiesInProgress(){
		return copiesInProgress;
	}
	
	
	@Override
	public void abortCopy(){
		clearTemp();
	}
	
	/**
	 * Clears temporary files created during copying if program unexpected terminates due to a handled exception
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
	
	/**
	 * Processes XMLs by passing each single XML to XMLModAndCopy
	 * @param ruleMap a HashMap of the path to the rule in the original CTT zip and the contents of each file
	 * @return a HashMap of the path to the rule in the CTT zip, and the modified contents
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 */
	private HashMap<Path, ByteArrayOutputStream> processingXMLs(HashMap<Path, ByteArrayOutputStream> ruleMap) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException, TransformerException{
		//modified rule map	
		HashMap<Path, ByteArrayOutputStream> modRuleArray = new HashMap<Path, ByteArrayOutputStream>();
				
		for (HashMap.Entry<Path, ByteArrayOutputStream> ruleOut : ruleMap.entrySet()){
			//convert to input streams
			ByteArrayInputStream ruleIn = new ByteArrayInputStream(ruleOut.getValue().toByteArray());
			//creates a new instance of the copying XML class to copy and modify each XML
			LOGGER.info("Copying " + ruleOut.getKey() + "...");
			//creates a path - XML bytestream for each rule
		    modRuleArray.put(ruleOut.getKey(), XMLCopy.copyXML(AccountingCTTXMLCopy.modifyXML( 
		    		XMLCopy.copyDoc(ruleIn), getArgsMap()), "<?xml version=\"1.0\"?>\n"));
		} 
			
	return modRuleArray;
	}

	
	/**
	 * unzips a zipped file to get accounting rules
	 * @return a HashMap of a path to each accounting rule and the contents in a byte output stream
	 * @throws IOException 
	 */
	private HashMap<Path, ByteArrayOutputStream> unzipFile() throws IOException{
		HashMap<Path, ByteArrayOutputStream> ruleArray = new HashMap<Path, ByteArrayOutputStream>();
			File inputZip = new File(getArgs("input"));
			ZipInputStream zipIn = null;
			if (!inputZip.exists() || !inputZip.canRead()){
				throw new FileNotFoundException("Cannot find/read input file " + getArgs("input") + ".");
			}
			else{
				zipIn = new ZipInputStream(new FileInputStream(getArgs("input")));
			}
			
			ZipEntry entry = null;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while ((entry = zipIn.getNextEntry()) != null) {
			    if (entry.getName().equals("static_data/CM.201.zip")) {
			    	BufferedOutputStream bos = new BufferedOutputStream(out);
			        byte[] buffer = new byte[BUFFER_SIZE];
			        int len;
			        while ((len = zipIn.read(buffer)) != -1) {
			            bos.write(buffer, 0, len);
			        }
			        bos.close();
			        break;
			    }
			}
			zipIn.close();
			
			//open the cm201zip file and add all accounting rules to a HashMap of filenames and file contents
			
			ZipInputStream cm201zip = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
			entry = null;
			while ((entry = cm201zip.getNextEntry()) != null) {
				Path entryPath = Paths.get(entry.getName());
				if (entryPath.getParent() != null && entryPath.getParent().toString().equals("mx\\accounting\\Rule")) {
					ByteArrayOutputStream out2 = new ByteArrayOutputStream();
					byte[] buffer = new byte[BUFFER_SIZE];
		            int len;
		            while ((len = cm201zip.read(buffer)) != -1) {
		                out2.write(buffer, 0, len);
		            }
		            out2.close();
		            ruleArray.put(entryPath, out2);
				}
			}
		return ruleArray;
	}
	
	/**
	 * recreates the Zip
	 * @param toBeZippedFiles a Hashmap of the path to each accounting rule and the contents in a byte stream
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private void rezipFile(HashMap<Path, ByteArrayOutputStream> toBeZippedFiles) throws IOException, URISyntaxException{
		final int BUFFER_SIZE = 4096;
		ZipInputStream zin = null;
		File outputZip = new File(getArgs("output"));
		if (outputZip.exists()){
			outputZip.delete();
		}
		createdFiles.add(outputZip);
		int outputZipIndex = createdFiles.size() - 1;
		Path outputZipPath = Paths.get(outputZip.getPath());
		Map<String, String> env = new HashMap<String, String>();
	    env.put("create", String.valueOf(Files.notExists(outputZipPath)));
	    // use a Zip filesystem URI
	    URI fileUri = outputZipPath.toUri();
	    URI zipUri = new URI("jar:" + fileUri.getScheme(), fileUri.getPath(), null);
	    try (FileSystem zipfs = FileSystems.newFileSystem(zipUri, env)) {
	    	LOGGER.info("Recreating the zip with the copied rules...");
			if (!(new File(getArgs("input"))).exists() || !(new File(getArgs("input"))).canRead()){
				throw new FileNotFoundException("Cannot find/read input file " + getArgs("input") + ".");
			}
			else if(((new File(getArgs("output"))).exists() && !(new File(getArgs("output"))).canWrite())){
				throw new FileNotFoundException("Cannot write to output file " + getArgs("output") + ".");
			}
			else{
				zin = new ZipInputStream(new FileInputStream(getArgs("input")));
			}
			ByteArrayOutputStream cm201zip = new ByteArrayOutputStream();
			//open outer zip
			
			ZipEntry entry = null;
			while ((entry = zin.getNextEntry()) != null) {
				boolean toBeDeleted = false;
				Path entryPath = Paths.get(entry.getName());
				//copy everything but cm201
				if (entryPath.getFileName().toString().equals("CM.201.zip") || entry.isDirectory()) {
					toBeDeleted = true;
			    	BufferedOutputStream bos = new BufferedOutputStream(cm201zip);
			        byte[] buffer = new byte[BUFFER_SIZE];
			        int len;
			        while ((len = zin.read(buffer)) != -1) {
			            bos.write(buffer, 0, len);
			        }
			        bos.close();
				}
				if(!toBeDeleted){
					addToTempFolder(zipfs, "/", zin, entryPath);
		        }
			}
			zin.closeEntry();
			zin.close();
			
			//change cm201zip, add the new XMLs
			File cm201Zip = new File("tempcm201.tmp");
			if (cm201Zip.exists()){
				cm201Zip.delete();
			}
			createdFiles.add(cm201Zip);
			int cm201ZipIndex = createdFiles.size() - 1;
			Path cm201ZipPath = Paths.get(cm201Zip.getPath());
		    // use a Zip filesystem URI
		    URI cm201Uri = cm201ZipPath.toUri(); 
		    URI cm201zipUri = new URI("jar:" + cm201Uri.getScheme(), cm201Uri.getPath(), null);
			Map<String, String> env2 = new HashMap<String, String>();
		    env2.put("create", String.valueOf(Files.notExists(cm201ZipPath)));
		    
		    try (FileSystem cm201fs = FileSystems.newFileSystem(cm201zipUri,env2)) {
			
				ZipInputStream cm201zipInStream = new ZipInputStream(new ByteArrayInputStream(cm201zip.toByteArray()));
				entry = null;
	
				while ((entry = cm201zipInStream.getNextEntry()) != null) {
					boolean toBeDeleted = false;
					Path entryPath = Paths.get(entry.getName());
					//replace existing rules in zip
					if (entryPath.getParent() != null && entryPath.getParent().toString().equals("mx\\accounting\\Rule")
							|| entry.isDirectory()) {
						toBeDeleted = true;
					}
					if(!toBeDeleted){
						addToTempFolder(cm201fs, "/", cm201zipInStream, entryPath);
					}
				}
				
				cm201zipInStream.close();
				
				for (HashMap.Entry<Path, ByteArrayOutputStream> modifiedXML : toBeZippedFiles.entrySet()){
					ByteArrayInputStream ruleIn = new ByteArrayInputStream(modifiedXML.getValue().toByteArray());
					addToTempFolder(cm201fs, "/", ruleIn, modifiedXML.getKey());
				}
		    }
		    //move cm201 back into the outer zip
		    Path cm201Path = zipfs.getPath("/static_data/CM.201.zip");
			Files.createDirectories(cm201Path.getParent());
		    Files.copy(cm201ZipPath, cm201Path);
		    cm201Zip.delete();
		    createdFiles.remove(cm201ZipIndex);
	    }
		//end test zip
		createdFiles.remove(outputZipIndex);

	}
	
	
	/**
	 * Helper method to add the files to a zip FS
	 * @param zipfs the FileSystem representation of the zip file 
	 * @param directoryPath the parent directory of the zip entry in the zip FS
	 * @param inputStream the zip entry as an input stream
	 * @param entryPath the name of the zip entry
	 * @throws IOException
	 */
	private void addToTempFolder(FileSystem zipfs, String directoryPath, InputStream inputStream, Path entryPath) throws IOException{
		Path internalTargetPath = zipfs.getPath(directoryPath + "/" + entryPath.toString());
		Files.createDirectories(internalTargetPath.getParent());
		OutputStream fileOut = Files.newOutputStream(internalTargetPath);
		byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            fileOut.write(buffer, 0, len);
        }
        fileOut.close();
	}
	
}

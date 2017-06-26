package xmlTools;

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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

/**
 * 
 * Unzips and rezips CTTs
 *
 */
class CTTZip extends CopyableClass{
	private static final Logger LOGGER = Logger.getLogger( CTTZip.class.getName() );
	final static int BUFFER_SIZE = 4096;
	//stores created files for easy deletion during copy abortion
	private static ArrayList<File> createdFiles = new ArrayList<>();
	
	CTTZip() {
		args.put("selection", "");
	}
	

	
	@Override
	public int startCopying() throws IOException, URISyntaxException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerException{
		HashMap<Path, ByteArrayOutputStream> copiedXMLs;
		rezipFile(copiedXMLs = processingXMLs(unzipFile(), args));
		return copiedXMLs.size();
	}
	
	/**
	 * instance method calls static method clearTemp to run abort sequence
	 * instance method is used so we can implement the abstract method in the interface Copyable
	 */
	public void abortCopy(){
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
	
	/**
	 * Processes XMLs by passing each single XML to XMLModAndCopy
	 * @param ruleMap
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 */
	private HashMap<Path, ByteArrayOutputStream> processingXMLs(HashMap<Path, ByteArrayOutputStream> ruleMap, 
			HashMap<String, String> args) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException, TransformerException{
		//modified rule map	
		HashMap<Path, ByteArrayOutputStream> modRuleArray = new HashMap<Path, ByteArrayOutputStream>();
				
		for (HashMap.Entry<Path, ByteArrayOutputStream> ruleOut : ruleMap.entrySet()){
			//convert to input streams
			ByteArrayInputStream ruleIn = new ByteArrayInputStream(ruleOut.getValue().toByteArray());
			//creates a new instance of the copying XML class to copy and modify each XML
			LOGGER.info("Copying " + ruleOut.getKey() + "...");
		    modRuleArray.put(ruleOut.getKey(), CTTXMLCopy.copyXML(ruleIn, args));
		} 
			
	return modRuleArray;
	}

	
	/**
	 * unzips a zipped file to get accounting rules
	 * @return
	 * @throws IOException 
	 */
	private HashMap<Path, ByteArrayOutputStream> unzipFile() throws IOException{
		HashMap<Path, ByteArrayOutputStream> ruleArray = new HashMap<Path, ByteArrayOutputStream>();
			File inputZip = new File(args.get("input"));
			ZipInputStream zipIn = null;
			if (!inputZip.exists() || !inputZip.canRead()){
				throw new FileNotFoundException("Cannot find/read input file " + args.get("input") + ".");
			}
			else{
				zipIn = new ZipInputStream(new FileInputStream(args.get("input")));
			}
			
			ZipEntry entry = null;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while ((entry = zipIn.getNextEntry()) != null) {
				//System.out.println(entry.getName());
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
	 * @param toBeZippedFiles
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private void rezipFile(HashMap<Path, ByteArrayOutputStream> toBeZippedFiles) throws IOException, URISyntaxException{
		final int BUFFER_SIZE = 4096;
		ZipInputStream zin = null;
		//test zip
		File outputZip = new File(args.get("output"));
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
	    //System.out.println(zipUri);
	    try (FileSystem zipfs = FileSystems.newFileSystem(zipUri, env)) {
	    	LOGGER.info("Recreating the zip with the copied rules...");
			if (!(new File(args.get("input"))).exists() || !(new File(args.get("input"))).canRead()){
				throw new FileNotFoundException("Cannot find/read input file " + args.get("input") + ".");
			}
			else if(((new File(args.get("output"))).exists() && !(new File(args.get("output"))).canWrite())){
				throw new FileNotFoundException("Cannot write to output file " + args.get("output") + ".");
			}
			else{
				zin = new ZipInputStream(new FileInputStream(args.get("input")));
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
		    URI cm201Uri = cm201ZipPath.toUri(); // here
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
	 * @param zipfs
	 * @param directoryPath
	 * @param inputStream
	 * @param entryPath
	 * @throws IOException
	 */
	private void addToTempFolder(FileSystem zipfs, String directoryPath, InputStream inputStream, Path entryPath) throws IOException{
		//System.out.println(entryPath +  Boolean.toString(Files.isDirectory(entryPath)));
		Path internalTargetPath = zipfs.getPath(directoryPath + "/" + entryPath.toString());
		//System.out.println(internalTargetPath);
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

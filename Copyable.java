package xmlTools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

/**
 * Interface for copied objects
 * Main method is startCopying which allows the user to start copying this object
 * abortCopy is used when a copy needs to be aborted unexpectedly
 * setArgs allows calling classes / methods to give more instructions for the copy to this object
 * copiesInProgress returns the number of copies that are currently in progress from all instances of a subclass of this object
 * getExtensions returns the extensions of files that are supported by this object - i.e. can be copied
 */
public interface Copyable {
	void abortCopy();
	int startCopying() throws IOException, URISyntaxException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerException;
	void setArgs(String key, String value);
	int copiesInProgress();
	List<String> getExtensions();
}
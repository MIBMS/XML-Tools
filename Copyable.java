package xmlTools;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

/**
 * 
 * Interface for copied objects
 *
 */
public interface Copyable {
	void abortCopy();
	int startCopying() throws IOException, URISyntaxException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerException;
	void setArgs(String key, String value);
	int copiesInProgress();
}
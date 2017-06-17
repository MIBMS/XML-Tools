package xmlTools;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

/**
 * 
 * Base class for copied objects - subclasses for CTT zips, XMLs, CSV(?)
 *
 */

abstract class CopiedObject{
	abstract int startCopying() throws IOException, URISyntaxException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerException;
	abstract void abort();
	abstract void setArgs(String key, String value);
	
}
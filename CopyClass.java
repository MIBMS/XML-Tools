package xmlTools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

public abstract class CopyClass implements Copyable{
	protected HashMap<String, String> args = new HashMap<String, String>();
	
	public abstract void abortCopy();
	public abstract int startCopying() throws IOException, URISyntaxException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerException;
	/**
	 * Set arguments for the CopyAndModifyXML class, e.g. instructions for modification
	 */
	
	public void setArgs(String key, String value){
		args.put(key, value);
	};
}
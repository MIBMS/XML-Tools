package xmlTools;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import javafx.event.Event;
import javafx.scene.layout.GridPane;

abstract class CopyPane extends GridPane {
	abstract void copy(Event e) throws XPathExpressionException, IOException, URISyntaxException, ParserConfigurationException, SAXException, TransformerException;
	abstract void abort(Event e);
}

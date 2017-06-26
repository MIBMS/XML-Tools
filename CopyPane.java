package xmlTools;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import javafx.event.Event;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

abstract class CopyPane extends GridPane {

	protected final TextField inputFilePath = new TextField();
	protected final TextField outputFilePath = new TextField();
	protected final FileChooser fileChooser = new FileChooser();
	
	/**
	 * copy method to be implemented by subclasses
	 * @param e
	 * @throws XPathExpressionException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws TransformerException
	 */
	abstract void copy(Event e) throws XPathExpressionException, IOException, URISyntaxException, ParserConfigurationException, SAXException, TransformerException;
	
	/**
	 * abort copying method to be implemented by subclasses
	 * @param e
	 */
	abstract void abort(Event e);
	
	/**
	 * listener method for buttons for browsing input/output files
	 * @param input
	 */
	protected void FileChooser(boolean input, ArrayList<String> extensions) {
		if (input) 
		{
			configureFileChooser(fileChooser, "Input File", extensions);
			File file = fileChooser.showOpenDialog(new Stage());
	        if (file != null) {
	        	inputFilePath.setText(file.getAbsolutePath());
	        }
		}
		else
		{	
			configureFileChooser(fileChooser, "Output File", extensions);
			File file = fileChooser.showSaveDialog(new Stage());
	        if (file != null) {
	        	outputFilePath.setText(file.getAbsolutePath());
	        }
		}		
	}
	
	/**
	 * Configures the File Open/Save dialog
	 * @param fileChooser
	 * @param title
	 * @param extensions extensions that can be chosen in the File Open/Save dialog
	 */
	private static void configureFileChooser(final FileChooser fileChooser, String title, ArrayList<String> extensions) {
		//removes all extension filters (lest we have a bug where the list keeps growing)
		fileChooser.getExtensionFilters().clear();
		for (String extension: extensions){
			FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter
					(String.format("%s files (*.%s)", extension.toUpperCase(), extension), "*." + extension);
			fileChooser.getExtensionFilters().addAll(extensionFilter);
		}
	    fileChooser.setTitle(title);
	    fileChooser.setInitialDirectory(
	        new File(System.getProperty("user.dir"))
	    ); 
	}

}

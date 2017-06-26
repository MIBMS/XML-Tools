package xmlTools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.util.converter.IntegerStringConverter;

class MultipleCopyPane extends CopyPane {
	private static final Logger LOGGER = Logger.getLogger( MultipleCopyPane.class.getName() );
	
	private GridPane inputOutputPane = new GridPane();
	private GridPane xPathPane = new GridPane();
	private GridPane multiplePane = new GridPane();
	private TextField xPath = new TextField();
	private TextField numCopies = new TextField();
	
	MultipleCopyPane(){
		super(new ArrayList<String>(Arrays.asList("xml")));
		copyObject = new XMLObject();
		//creates the input output pane
		inputOutputPane.add(new Label("Input File: "), 0, 0);
		inputOutputPane.add(inputFilePath, 1, 0);
		inputOutputPane.add(inputButton, 2, 0);
		inputOutputPane.add(new Label("Output Folder: "), 0, 1);
		inputOutputPane.add(outputFilePath, 1, 1);
		inputOutputPane.add(outputFolderButton, 2, 1);
		inputOutputPane.setAlignment(Pos.CENTER);
		//creates Textfield pane for the xPath expression to modify
		xPath.setPrefWidth(300);
		numCopies.setPrefColumnCount(4);
		numCopies.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));  
		xPathPane.add(new Label("XPath: "), 0, 0);
		xPathPane.add(xPath, 1, 0);
		xPathPane.add(new Label("Number of Copies: "), 2, 0);
		xPathPane.add(numCopies, 3, 0);
		xPathPane.setAlignment(Pos.CENTER);
		xPathPane.setHgap(12);
		//creates the multiple pane for this tab
		multiplePane.add(inputOutputPane, 0, 0);
		multiplePane.add(xPathPane, 0, 1);
		multiplePane.setAlignment(Pos.CENTER);
		multiplePane.setHgap(6);
		multiplePane.setVgap(6);
		multiplePane.setPadding(new Insets(12));
		//adds multiplePane to MultipleCopyPane
		setTop(multiplePane);
	}
	
	@Override
	void copy(Event e) throws XPathExpressionException, IOException, URISyntaxException, ParserConfigurationException,
			SAXException, TransformerException {
		try {
			if(!inputFilePath.getText().equals("") && !outputFilePath.getText().equals("") )
			{
				copyObject.setArgs("input", inputFilePath.getText());
				copyObject.setArgs("output", outputFilePath.getText());
				copyObject.setArgs("xPath", xPath.getText());
				copyObject.setArgs("numCopies", numCopies.getText());
				int numCopiedXMLs = copyObject.startCopying();
				LOGGER.log(Level.INFO, "Program successfully copied " + numCopiedXMLs + " XMLs.");
			}
		} catch (FileNotFoundException e1){
			Alert alert = new Alert(AlertType.WARNING, "Files do not exist!");
			LOGGER.log(Level.WARNING, e1.toString(), e1);
			alert.showAndWait();
		}			
	}

	@Override
	void abort(Event e) {
		// TODO Auto-generated method stub
		
	}

}

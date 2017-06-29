package xmlTools.UI;

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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.converter.IntegerStringConverter;
import xmlTools.XMLObject;

public class MultipleCopyPane extends CopyPane {
	private static final Logger LOGGER = Logger.getLogger( MultipleCopyPane.class.getName() );
	
	private GridPane inputOutputPane = new GridPane();
	//private GridPane xPathPane = new GridPane();
	private GridPane multiplePane = new GridPane();
	private TextField xPath = new TextField();
	private TextField numCopies = new TextField();
	private TextArea replaceText = new TextArea();
	private final RadioButton multipleCopiesButton = new RadioButton("Make multiple copies");
	private final RadioButton replaceTextButton = new RadioButton("Replace text nodes");
	private final ToggleGroup processingActionGroup = new ToggleGroup();
	private HBox processingActionPane = new HBox(8);
	private GridPane processingActionSubPane = new GridPane();
	
	public MultipleCopyPane(){
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
		numCopies.setPrefColumnCount(2);
		numCopies.setTextFormatter(new TextFormatter<>(new IntegerStringConverter())); 
		multipleCopiesButton.setToggleGroup(processingActionGroup);
		replaceTextButton.setToggleGroup(processingActionGroup);
		processingActionPane.getChildren().addAll(multipleCopiesButton, replaceTextButton);
		processingActionPane.setAlignment(Pos.CENTER);
		//set RadioButton listener
		processingActionGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov,
			Toggle old_toggle, Toggle new_toggle) -> changeProcessingActionSubPane(ov, old_toggle, new_toggle));
		
		
		//creates the multiple pane for this tab
		multiplePane.add(inputOutputPane, 0, 0);
		multiplePane.add(processingActionPane, 0, 1);
		multiplePane.add(processingActionSubPane, 0, 2);
		multiplePane.setAlignment(Pos.CENTER);
		multiplePane.setHgap(6);
		multiplePane.setVgap(6);
		multiplePane.setPadding(new Insets(12));
		//adds multiplePane to MultipleCopyPane
		setTop(multiplePane);
		
		//set listeners for the numCopies and Xpath textfields so only one can be editable
		numCopies.textProperty().addListener(new ChangeListener<String>() {

	        @Override
	        public void changed(ObservableValue<? extends String> ov, String t, String t1) {
	           if(!t1.equals("")){
	               xPath.setDisable(false);
	               replaceText.setDisable(false);
	           }
	        }	           
	    });
		
	}
	
	//listener method for processing action radio buttons
		private void changeProcessingActionSubPane(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle)
		{	
			RadioButton selectedRadioButton = (RadioButton)processingActionGroup.getSelectedToggle();
			if (selectedRadioButton != null) 
			{
				multiplePane.getChildren().remove(processingActionSubPane);
				processingActionSubPane = new GridPane();
				switch (selectedRadioButton.getText()){
				case "Make multiple copies":
					processingActionSubPane.add(new Label("Number of Copies: "), 0, 0);
					processingActionSubPane.add(numCopies, 1, 0);
					processingActionSubPane.setAlignment(Pos.CENTER);
					break;
				case "Replace text nodes":
					processingActionSubPane.add(new Label("XPath to text node: "), 0, 0);
					processingActionSubPane.add(xPath, 1, 0);
					processingActionSubPane.add(new Label("Replace text nodes with: "), 0, 1);
					processingActionSubPane.add(replaceText, 1, 1);				
					break;
				}
				processingActionSubPane.setAlignment(Pos.CENTER);
				multiplePane.add(processingActionSubPane, 0, 2);
				inputOutputPane.setAlignment(Pos.CENTER);
			}
		}
	
	@Override
	void copy(Event event) throws XPathExpressionException, IOException, URISyntaxException, ParserConfigurationException,
			SAXException, TransformerException {
		RadioButton selectedProcessingAction = (RadioButton)processingActionGroup.getSelectedToggle();
		if (selectedProcessingAction != null)
		{
			String selection = selectedProcessingAction.getText();
			copyObject.setArgs("selection", selection);
			switch (selection){
			case "Make multiple copies":
				copyObject.setArgs("numCopies", numCopies.getText());
				break;
			case "Replace text nodes":
				copyObject.setArgs("xPath", xPath.getText());
				copyObject.setArgs("replaceText", replaceText.getText());
				break;
			}	
		}			
		try {
			if(!inputFilePath.getText().equals("") && !outputFilePath.getText().equals("") )
			{
				copyObject.setArgs("input", inputFilePath.getText());
				copyObject.setArgs("output", outputFilePath.getText());
				int numCopiedXMLs = copyObject.startCopying();
				LOGGER.log(Level.INFO, "Program successfully copied " + numCopiedXMLs + " XMLs.");

			}
		} catch (FileNotFoundException e){
			Alert alert = new Alert(AlertType.WARNING, "Files do not exist!");
			LOGGER.log(Level.WARNING, e.toString(), e);
			alert.showAndWait();
		}			
	}
}

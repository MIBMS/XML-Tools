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

import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * 
 * Create a custom Pane to display options for copying accounting rules
 *  
 */
class AccountingCopyPane extends CopyPane{
	private static final Logger LOGGER = Logger.getLogger( AccountingCopyPane.class.getName() );
	
	private final TextField addEntityName = new TextField();
	private final TextField fromEntityName = new TextField();
	private final TextField toEntityName = new TextField();
	private final RadioButton addEntityButton = new RadioButton("Add Entity");
	private final RadioButton changeEntityButton = new RadioButton("Change Entity");
	private final ToggleGroup processingActionGroup = new ToggleGroup();
	private GridPane inputOutputPane = new GridPane();
	private HBox processingActionPane = new HBox(8);
	private HBox processingActionSubPane = new HBox(8);
	private GridPane accountingPane = new GridPane();
	
	public AccountingCopyPane() {
		super(new ArrayList<String>(Arrays.asList("zip")));
		copyObject = new CTTZip();
		inputOutputPane.add(new Label("Input File: "), 0, 0);
		inputOutputPane.add(inputFilePath, 1, 0);
		inputOutputPane.add(inputButton, 2, 0);
		inputOutputPane.add(new Label("Output File: "), 0, 1);
		inputOutputPane.add(outputFilePath, 1, 1);
		inputOutputPane.add(outputButton, 2, 1);
		//make inputOutputPane horizontally resizable
		for(Node child:inputOutputPane.getChildren()){
			if (child instanceof TextField){
				GridPane.setHgrow(child, Priority.ALWAYS);
			}
		}
		accountingPane.add(inputOutputPane, 0, 0);
		
		//radio buttons for type of processing to do on XML
		
		addEntityButton.setToggleGroup(processingActionGroup);
		changeEntityButton.setToggleGroup(processingActionGroup);
		processingActionPane.getChildren().addAll(addEntityButton, changeEntityButton);
		processingActionPane.setAlignment(Pos.CENTER);
		accountingPane.add(processingActionPane, 0, 1);
		
		accountingPane.add(processingActionSubPane, 0, 2);
		accountingPane.setHgap(6);
		accountingPane.setVgap(6);
		GridPane.setHalignment(inputButton, HPos.RIGHT);
		GridPane.setHalignment(outputButton, HPos.RIGHT);
		GridPane.setHalignment(addEntityButton, HPos.RIGHT);
		GridPane.setHalignment(changeEntityButton, HPos.RIGHT);
        accountingPane.setAlignment(Pos.CENTER);
        accountingPane.setPadding(new Insets(12));
   	
      	//set RadioButton listener
		processingActionGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov,
				Toggle old_toggle, Toggle new_toggle) -> changeProcessingActionSubPane(ov, old_toggle, new_toggle));
		
		//adds accountingPane to AccountingCopyPane
		setTop(accountingPane);
	}
	
	//listener method for processing action radio buttons
	private void changeProcessingActionSubPane(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle)
	{
		RadioButton selectedRadioButton = (RadioButton)processingActionGroup.getSelectedToggle();
		if (selectedRadioButton != null) 
		{
			accountingPane.getChildren().remove(processingActionSubPane);
			processingActionSubPane = new HBox(8);
			switch (selectedRadioButton.getText()){
			case "Add Entity":
				processingActionSubPane.getChildren().addAll(new Label("Entity to be ticked:"), addEntityName);
				processingActionSubPane.setAlignment(Pos.CENTER);
				break;
			case "Change Entity":
				processingActionSubPane.getChildren().addAll(new Label("Entity to uncheck: "), fromEntityName, 
						new Label("Entity to check: "), toEntityName);
				break;
			}
			processingActionSubPane.setAlignment(Pos.CENTER);
			accountingPane.add(processingActionSubPane, 0, 2);
			selectedRadioButton.getScene().getWindow().sizeToScene();
		}
	}
	
	/**
	 * listener method for copy button
	 */
	@Override
	void copy(Event event) throws XPathExpressionException, IOException, URISyntaxException, ParserConfigurationException, SAXException, TransformerException{
		
		RadioButton selectedProcessingAction = (RadioButton)processingActionGroup.getSelectedToggle();
		if (selectedProcessingAction != null)
		{
			switch (selectedProcessingAction.getText()){
			case "Add Entity":
				copyObject.setArgs("modify", "Tick Additional Entity");
				copyObject.setArgs("entity", addEntityName.getText());
				break;
			case "Change Entity":
				copyObject.setArgs("modify", "Tick A Different Entity");
				copyObject.setArgs("fromEntity", fromEntityName.getText());
				copyObject.setArgs("toEntity", toEntityName.getText());
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
				//Object sourceObject = event.getSource();
				//if (sourceObject instanceof Node){
				//	((Node) sourceObject).getScene().getWindow().hide();
				//}	
			}
		} catch (FileNotFoundException e){
			Alert alert = new Alert(AlertType.WARNING, "Files do not exist!");
			LOGGER.log(Level.WARNING, e.toString(), e);
			alert.showAndWait();
		}	
	}
	
	/**
	 * calls the Copyable interface abortCopy method to clear temp files, etc. when application is closed due to an unhandled exception
	 * @param event - event trigger
	 */
	@Override
	void abort(Event event){
		copyObject.abortCopy();
		LOGGER.warning("Copying was aborted.");
		Object sourceObject = event.getSource();
		if (sourceObject instanceof Node){
			((Node) sourceObject).getScene().getWindow().hide();
		}	
	}
}
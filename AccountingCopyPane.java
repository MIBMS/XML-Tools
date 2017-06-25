package xmlTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * 
 * Create a custom Pane to display options for copying accounting rules
 *  
 */
class AccountingCopyPane extends GridPane{
	private static final Logger LOGGER = Logger.getLogger( AccountingCopyPane.class.getName() );
	
	private final TextField inputFilePath = new TextField();
	private final TextField outputFilePath = new TextField();
	private final TextField addEntityName = new TextField();
	private final TextField fromEntityName = new TextField();
	private final TextField toEntityName = new TextField();
	private final Button inputButton = new Button("Browse");
	private final Button outputButton = new Button("Browse");
	private final Button copyButton = new Button("Copy");
	private final RadioButton addEntityButton = new RadioButton("Add Entity");
	private final RadioButton changeEntityButton = new RadioButton("Change Entity");
	private final ToggleGroup processingActionGroup = new ToggleGroup();
	private final FileChooser fileChooser = new FileChooser();
	private GridPane inputOutputPane = new GridPane();
	private HBox processingActionPane = new HBox(8);
	private HBox processingActionSubPane = new HBox(8);
	
	private Copyable copyMachine = new CTTZip();
	
	public AccountingCopyPane() throws RuntimeException {
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
		add(inputOutputPane, 0, 0);
		
		//radio buttons for type of processing to do on XML
		
		addEntityButton.setToggleGroup(processingActionGroup);
		changeEntityButton.setToggleGroup(processingActionGroup);
		processingActionPane.getChildren().addAll(addEntityButton, changeEntityButton);
		processingActionPane.setAlignment(Pos.CENTER);
		add(processingActionPane, 0, 1);
		
		add(processingActionSubPane, 0, 2);
		add(copyButton, 0, 3);
        setHgap(6);
        setVgap(6);
        setHalignment(inputButton, HPos.RIGHT);
        setHalignment(outputButton, HPos.RIGHT);
        setHalignment(addEntityButton, HPos.RIGHT);
        setHalignment(changeEntityButton, HPos.RIGHT);
        setHalignment(copyButton, HPos.RIGHT);
        //set Alignment of FileOpenPane
        setAlignment(Pos.CENTER);
        
        //set button listeners
		inputButton.setOnAction((ActionEvent event) -> FileChooser(true));	
		outputButton.setOnAction((ActionEvent event) -> FileChooser(false));
		copyButton.setOnAction((ActionEvent event) -> {
			try{
				copy(event);
			} catch (XPathExpressionException | IOException | URISyntaxException | ParserConfigurationException | SAXException | TransformerException e) {
				//throw an unchecked exception and log the underlying exception
				LOGGER.log(Level.SEVERE, e.toString(), e);
				abort(event);
				throw new RuntimeException(e);
			}
		});
	
		//set RadioButton listener
		processingActionGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov,
				Toggle old_toggle, Toggle new_toggle) -> changeProcessingActionSubPane(ov, old_toggle, new_toggle));
	}
	
	//listener method for processing action radio buttons
	private void changeProcessingActionSubPane(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle)
	{
		RadioButton selectedRadioButton = (RadioButton)processingActionGroup.getSelectedToggle();
		if (selectedRadioButton != null) 
		{
			this.getChildren().remove(processingActionSubPane);
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
			this.add(processingActionSubPane, 0, 2);
			selectedRadioButton.getScene().getWindow().sizeToScene();
		}
	}
	
	//listener method for input/output button
	private void FileChooser(boolean input){
		if (input) 
		{
			configureFileChooser(fileChooser, "Input File");
			File file = fileChooser.showOpenDialog(new Stage());
            if (file != null) {
            	inputFilePath.setText(file.getAbsolutePath());
            }
		}
		else
		{	
			configureFileChooser(fileChooser, "Output File");
			File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
            	outputFilePath.setText(file.getAbsolutePath());
            }
		}		
	}
	
	private static void configureFileChooser(final FileChooser fileChooser, String title){
		FileChooser.ExtensionFilter zipFilter = new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip");
		fileChooser.getExtensionFilters().addAll(zipFilter);
        fileChooser.setTitle(title);
        fileChooser.setInitialDirectory(
            new File(System.getProperty("user.dir"))
        ); 
    }
	
	//listener method for copy button
	private void copy(ActionEvent event) throws XPathExpressionException, IOException, URISyntaxException, ParserConfigurationException, SAXException, TransformerException{
		
		RadioButton selectedProcessingAction = (RadioButton)processingActionGroup.getSelectedToggle();
		if (selectedProcessingAction != null)
		{
			switch (selectedProcessingAction.getText()){
			case "Add Entity":
				copyMachine.setArgs("modify", "Tick Additional Entity");
				copyMachine.setArgs("entity", addEntityName.getText());
				break;
			case "Change Entity":
				copyMachine.setArgs("modify", "Tick A Different Entity");
				copyMachine.setArgs("fromEntity", fromEntityName.getText());
				copyMachine.setArgs("toEntity", toEntityName.getText());
				break;
			}
		}
		try {
			if(!inputFilePath.getText().equals("") && !outputFilePath.getText().equals("") )
			{
				copyMachine.setArgs("input", inputFilePath.getText());
				copyMachine.setArgs("output", outputFilePath.getText());
				int numCopiedXMLs = copyMachine.startCopying();
				LOGGER.log(Level.INFO, "Program successfully copied " + numCopiedXMLs + " XMLs.");
				Object sourceObject = event.getSource();
				if (sourceObject instanceof Node){
					((Node) sourceObject).getScene().getWindow().hide();
				}	
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
	private void abort(Event event){
		copyMachine.abortCopy();
		LOGGER.warning("Copying was aborted.");
		Object sourceObject = event.getSource();
		if (sourceObject instanceof Node){
			((Node) sourceObject).getScene().getWindow().hide();
		}	
	}
}
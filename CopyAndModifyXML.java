package xmlTools;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import javafx.stage.FileChooser;

/**
 * 
 * Create a custom Pane to be displayed in the application
 *  
 */
class FileOpenPane extends GridPane{
	private static final Logger LOGGER = Logger.getLogger( CTTZip.class.getName() );
	private static FileHandler loggerFileHandler = null;

	
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
	
	public FileOpenPane(){		
		try {
			loggerFileHandler = new FileHandler("log.txt", false);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		loggerFileHandler.setFormatter(new SimpleFormatter());
		LOGGER.addHandler(loggerFileHandler);
		LOGGER.log(Level.INFO, "Start Log");
		inputOutputPane.add(new Label("Input File: "), 0, 0);
		inputOutputPane.add(inputFilePath, 1, 0);
		inputOutputPane.add(inputButton, 2, 0);
		inputOutputPane.add(new Label("Output File: "), 0, 1);
		inputOutputPane.add(outputFilePath, 1, 1);
		inputOutputPane.add(outputButton, 2, 1);
		//make inputOutputPane horizontally resizable
		for(javafx.scene.Node child:inputOutputPane.getChildren()){
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
		copyButton.setOnAction((ActionEvent event) -> copy());

		
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
	private void copy(){
		CTTZip copyMachine = new CTTZip();
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
				copyMachine.startCopying();
				System.exit(0);
			}
		} catch (FileNotFoundException e){
			Alert alert = new Alert(AlertType.WARNING, "Files do not exist!");
			alert.showAndWait();
		} catch (XPathExpressionException | IOException | URISyntaxException | ParserConfigurationException
				| SAXException | TransformerException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}		
	}
}

/**
 * 
 * main class extends JavaFX application class
 *
 */
public class CopyAndModifyXML extends Application{
	
	@Override //Override start method in Application
	public void start(final Stage stage){

		
        final GridPane inputGridPane = new FileOpenPane();
       
        final Pane rootGroup = new VBox(12);
        rootGroup.getChildren().addAll(inputGridPane);
        rootGroup.setPadding(new Insets(12, 12, 12, 12));
 
        stage.setScene(new Scene(rootGroup));
		
		stage.setTitle("Copy XML files");
		stage.show();
	}
	
	public static void main(String[] args) {
        Application.launch(args);
    }
}

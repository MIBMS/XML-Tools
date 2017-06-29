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

import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import xmlTools.AccountingCTTZip.AccountingCTTZip;

/**
 * 
 * Create a custom Pane to display options for copying accounting rules
 *  
 */
public class AccountingCopyPane extends CopyPane<AccountingCTTZip>{
	private static final Logger LOGGER = Logger.getLogger( AccountingCopyPane.class.getName() );
	
	private final TextArea entitiesToCheck = new TextArea();
	private final TextArea accountingSectionsToCheck = new TextArea();
	private final RadioButton editEntitiesButton = new RadioButton("Edit entities");
	private final RadioButton editAccountingSectionsButton = new RadioButton("Edit accounting sections");
	private final ToggleGroup processingActionGroup = new ToggleGroup();
	private GridPane inputOutputPane = new GridPane();
	private HBox processingActionPane = new HBox(8);
	private HBox processingActionSubPane = new HBox(8);
	private GridPane accountingPane = new GridPane();
	
	public AccountingCopyPane() {
		super(new ArrayList<String>(Arrays.asList("zip")));
		copyObject = new AccountingCTTZip();
		inputOutputPane.add(new Label("Input File: "), 0, 0);
		inputOutputPane.add(inputFilePath, 1, 0);
		inputOutputPane.add(inputButton, 2, 0);
		inputOutputPane.add(new Label("Output File: "), 0, 1);
		inputOutputPane.add(outputFilePath, 1, 1);
		inputOutputPane.add(outputButton, 2, 1);

		accountingPane.add(inputOutputPane, 0, 0);
		
		//radio buttons for type of processing to do on XML
		
		editEntitiesButton.setToggleGroup(processingActionGroup);
		editAccountingSectionsButton.setToggleGroup(processingActionGroup);
		//changeEntityButton.setSelected(true);
		processingActionPane.getChildren().addAll(editEntitiesButton, editAccountingSectionsButton);
		processingActionPane.setAlignment(Pos.CENTER);
		accountingPane.add(processingActionPane, 0, 1);
		
		accountingPane.add(processingActionSubPane, 0, 2);
		accountingPane.setHgap(12);
		accountingPane.setVgap(12);
		GridPane.setHalignment(editEntitiesButton, HPos.RIGHT);
		GridPane.setHalignment(editAccountingSectionsButton, HPos.RIGHT);
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
			case "Edit entities":
				processingActionSubPane.getChildren().addAll(new Label("Entities to check:"), entitiesToCheck);
				break;
			case "Edit accounting sections":
				processingActionSubPane.getChildren().addAll(new Label("Accounting sections to check: "), accountingSectionsToCheck);
				break;
			}
			processingActionSubPane.setAlignment(Pos.CENTER);
			accountingPane.add(processingActionSubPane, 0, 2);
			inputOutputPane.setAlignment(Pos.CENTER);
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
			String selection = selectedProcessingAction.getText();
			copyObject.setArgs("selection", selection);
			switch (selection){
			case "Edit entities":
				copyObject.setArgs("entities", entitiesToCheck.getText());
				break;
			case "Edit accounting sections":
				copyObject.setArgs("accountingSections", accountingSectionsToCheck.getText());
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
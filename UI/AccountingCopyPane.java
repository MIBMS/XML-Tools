package xmlTools.UI;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import xmlTools.AccountingCTTZip.AccountingCTTZip;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Create a custom Pane to display options for copying accounting rules
 */
public class AccountingCopyPane extends CopyPane<AccountingCTTZip>{
	private static final Logger LOGGER = Logger.getLogger( AccountingCopyPane.class.getName() );
	
	private final TextArea entitiesToCheck = new TextArea();
	private final TextArea accountingSectionsToCheck = new TextArea();
	private final TextField udfLabel = new TextField();
	private final TextField subTree = new TextField();
	private final CheckBox editEntitiesButton = new CheckBox("Edit entities");
	private final CheckBox editAccountingSectionsButton = new CheckBox("Edit accounting sections");
	private final CheckBox editMiscButton = new CheckBox("Edit miscellaneous rule user defined fields");
	private GridPane inputOutputPane = new GridPane();
	private GridPane processingActionPane = new GridPane();
	private GridPane editEntitiesPane = new GridPane();
	private GridPane editSectionsPane = new GridPane();
	private GridPane editMiscPane = new GridPane();
	private GridPane accountingPane = new GridPane();
	
	/**
	 * Creates a UI Pane instance for handling accounting CTT zip copies
	 */
	public AccountingCopyPane() {
		copyObject = new AccountingCTTZip();
		//sets Extensions supported by this UI Pane to zip files
		setExtensions();
		
		//starts creating all the UI elements and controls
		inputOutputPane.add(new Label("Input File: "), 0, 0);
		inputOutputPane.add(inputFilePath, 1, 0);
		inputOutputPane.add(inputButton, 2, 0);
		inputOutputPane.add(new Label("Output File: "), 0, 1);
		inputOutputPane.add(outputFilePath, 1, 1);
		inputOutputPane.add(outputButton, 2, 1);

		accountingPane.add(inputOutputPane, 0, 0);
		
		editEntitiesPane.add(editEntitiesButton, 0, 0);
		editEntitiesPane.add(new Label("Entities to check: "), 0, 1);
		editEntitiesPane.add(entitiesToCheck, 0, 2);
		entitiesToCheck.maxWidthProperty().bind(this.widthProperty());
		
		editSectionsPane.add(editAccountingSectionsButton, 0, 0);
		editSectionsPane.add(new Label("Accounting sections to check: "), 0, 1);
		editSectionsPane.add(accountingSectionsToCheck, 0, 2);
		accountingSectionsToCheck.maxWidthProperty().bind(this.widthProperty());
		
		editMiscPane.add(editMiscButton, 0, 0);
		editMiscPane.add(new Label("Label of UDF to edit:"), 0, 1);
		udfLabel.setPrefWidth(500);
		subTree.setPrefWidth(500);
		editMiscPane.add(udfLabel, 1, 1);
		editMiscPane.add(new Label("Subtree to add: "), 0, 2);
		editMiscPane.add(subTree, 1, 2);
		editMiscPane.setAlignment(Pos.TOP_LEFT);
		
		inputOutputPane.setAlignment(Pos.CENTER);
		editEntitiesPane.setAlignment(Pos.CENTER);
		editEntitiesPane.setHgap(12);
		editEntitiesPane.setVgap(12);
		editSectionsPane.setAlignment(Pos.CENTER);
		editSectionsPane.setHgap(12);
		editSectionsPane.setVgap(12);
		processingActionPane.add(editEntitiesPane, 0, 0);
		processingActionPane.add(editSectionsPane, 1, 0);
		processingActionPane.setAlignment(Pos.CENTER);
		processingActionPane.setHgap(8);
		processingActionPane.setVgap(8);
		editMiscPane.setHgap(8);
		editMiscPane.setVgap(8);
	
		accountingPane.add(processingActionPane, 0, 1);
		accountingPane.add(editMiscPane, 0, 2);
		

		accountingPane.setHgap(12);
		accountingPane.setVgap(12);
		GridPane.setHalignment(editEntitiesButton, HPos.LEFT);
		GridPane.setHalignment(editAccountingSectionsButton, HPos.LEFT);
        accountingPane.setAlignment(Pos.CENTER);
        accountingPane.setPadding(new Insets(12));
   	
		
		//adds accountingPane to AccountingCopyPane
		setTop(accountingPane);
		
		//sets the behavior of the checkboxes upon checking
		editEntitiesButton.selectedProperty().addListener(new CheckBoxListener(editEntitiesButton));
		editAccountingSectionsButton.selectedProperty().addListener(new CheckBoxListener(editAccountingSectionsButton));
		editMiscButton.selectedProperty().addListener(new CheckBoxListener(editMiscButton));
		
		//sets all checkboxes to non-editable initially
		entitiesToCheck.setEditable(false);
		accountingSectionsToCheck.setEditable(false);
		udfLabel.setEditable(false);
		subTree.setEditable(false);
	}
	
	@Override
	void copy(Event event) throws XPathExpressionException, IOException, URISyntaxException, ParserConfigurationException, SAXException, TransformerException{
		if (editEntitiesButton.isSelected()){
			copyObject.setArgs("entitiesSelected", "true");
			copyObject.setArgs("entities", entitiesToCheck.getText());
		}
		if (editAccountingSectionsButton.isSelected()){
			copyObject.setArgs("sectionsSelected", "true");
			copyObject.setArgs("accountingSections", accountingSectionsToCheck.getText());
		}
		if (editMiscButton.isSelected()){
			copyObject.setArgs("miscSelected", "true");
			copyObject.setArgs("udfLabel", udfLabel.getText());
			copyObject.setArgs("subTree", subTree.getText());
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
	
	//creates a class to listen to the checkbox and disable textfields unless the related checkbox is selected
	private class CheckBoxListener implements ChangeListener<Boolean> {
		private final CheckBox cb;
		CheckBoxListener(CheckBox cb) {
			this.cb = cb;
		}
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean old_val, Boolean new_val) {
			String cbLabel = cb.getText();
			if (old_val && !new_val){
				switch (cbLabel){
					case "Edit entities": 
						entitiesToCheck.setEditable(false);
						break;
					case "Edit accounting sections": 
						accountingSectionsToCheck.setEditable(false);
						break;
					case "Edit miscellaneous rule user defined fields": 
						udfLabel.setEditable(false);
						subTree.setEditable(false);
						break;
				}
			}
			if (!old_val && new_val){
				switch (cbLabel){
					case "Edit entities": 
						entitiesToCheck.setEditable(true);
						break;
					case "Edit accounting sections": 
						accountingSectionsToCheck.setEditable(true);
						break;
					case "Edit miscellaneous rule user defined fields": 
						udfLabel.setEditable(true);
						subTree.setEditable(true);
						break;
				}
			}
		}
	}
}
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

import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
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
	private final CheckBox editEntitiesButton = new CheckBox("Edit entities");
	private final CheckBox editAccountingSectionsButton = new CheckBox("Edit accounting sections");
	private GridPane inputOutputPane = new GridPane();
	private GridPane processingActionPane = new GridPane();
	private GridPane editEntitiesPane = new GridPane();
	private GridPane editSectionsPane = new GridPane();
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
		
		//changeEntityButton.setSelected(true);
		editEntitiesPane.add(editEntitiesButton, 0, 0);
		editEntitiesPane.add(new Label("Entities to check: "), 0, 1);
		editEntitiesPane.add(entitiesToCheck, 0, 2);
		entitiesToCheck.maxWidthProperty().bind(this.widthProperty());
		
		editSectionsPane.add(editAccountingSectionsButton, 0, 0);
		editSectionsPane.add(new Label("Accounting sections to check: "), 0, 1);
		editSectionsPane.add(accountingSectionsToCheck, 0, 2);
		accountingSectionsToCheck.maxWidthProperty().bind(this.widthProperty());
		
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
		processingActionPane.setHgap(12);
		processingActionPane.setVgap(12);
		
	
		accountingPane.add(processingActionPane, 0, 1);
		

		accountingPane.setHgap(12);
		accountingPane.setVgap(12);
		GridPane.setHalignment(editEntitiesButton, HPos.LEFT);
		GridPane.setHalignment(editAccountingSectionsButton, HPos.LEFT);
        accountingPane.setAlignment(Pos.CENTER);
        accountingPane.setPadding(new Insets(12));
   	
		
		//adds accountingPane to AccountingCopyPane
		setTop(accountingPane);
	}
	

	/**
	 * listener method for copy button
	 */
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
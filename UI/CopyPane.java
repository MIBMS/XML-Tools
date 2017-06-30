package xmlTools.UI;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import xmlTools.CopyClass;
import xmlTools.Copyable;

/**
 * Creates a BorderPane instance that supports copying of the generic CopyClass subtype E
 */
public abstract class CopyPane<E extends CopyClass> extends BorderPane {
	private static final Logger LOGGER = Logger.getLogger( CopyPane.class.getName() );
	protected final TextField inputFilePath = new TextField();
	protected final TextField outputFilePath = new TextField();
	protected final FileChooser fileChooser = new FileChooser();
	protected final DirectoryChooser dirChooser = new DirectoryChooser();
	protected Copyable copyObject;
	protected final Button copyButton = new Button("Copy");
	protected final Button inputButton = new Button("Browse");
	protected final Button outputButton = new Button("Browse");
	protected final Button outputFolderButton = new Button("Browse");
	private TextArea logArea = new TextArea();
	
	/**
	 * Creates a CopyPane instance
	 * Defines the actions for the common output folder and copy buttons
	 */
	CopyPane(){
		GridPane.setHalignment(inputButton, HPos.RIGHT);
		GridPane.setHalignment(outputButton, HPos.RIGHT);
		inputFilePath.setPrefWidth(300);
		outputFilePath.setPrefWidth(300);

		//set directory chooser button listener
		outputFolderButton.setOnAction((ActionEvent event) -> dirChooser());	 
		
        //set copy button listener
		copyButton.setOnAction((ActionEvent event) -> {
			try{
				copy(event);
			} catch (RuntimeException | XPathExpressionException | IOException | URISyntaxException | ParserConfigurationException | SAXException | TransformerException e) {
				//throw an unchecked exception and log the underlying exception
				LOGGER.log(Level.SEVERE, e.toString(), e);
				abort(event);
				throw new RuntimeException(e);
			}
		});
		
		//put a TextArea in the middle
		logArea.setEditable(false);
		logArea.setWrapText(true);
		setCenter(logArea);
		
		//put the copy button at the bottom
		HBox copyHBox = new HBox();
		copyHBox.setPadding(new Insets(12));
		copyHBox.getChildren().add(copyButton);
		copyHBox.setAlignment(Pos.CENTER);
		setBottom(copyHBox);
		
	}
	
	/**
	 * sets extensions that can be filtered in the FileChooser for this CopyPane
	 */
	void setExtensions(){
		List<String> extensions = copyObject.getExtensions();
		inputButton.setOnAction((ActionEvent event) -> fileChooser(true, extensions));	
      	outputButton.setOnAction((ActionEvent event) -> fileChooser(false, extensions));
	}
	
	/**
	 * Event handler method that is triggered upon copy button clicks
	 * Calls the Copyable object's startCopying method
	 * @param e Event that triggers the copy button action
	 * @throws XPathExpressionException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws TransformerException
	 */
	abstract void copy(Event e) throws XPathExpressionException, IOException, URISyntaxException, ParserConfigurationException, SAXException, TransformerException;
	
	/**
	 * calls the Copyable abortCopy method to clear temp files, etc. when application is closed due to an unhandled exception
	 * @param e Triggering event
	 */
	public void abort(Event e){
		if (copyObject.copiesInProgress() > 0){
			copyObject.abortCopy();
			LOGGER.warning("Copying was aborted.");
		}	
		Object sourceObject = e.getSource();
		if (sourceObject instanceof Node){
			((Node) sourceObject).getScene().getWindow().hide();
		}	
	};
	
	/**
	 * listener method for input/output buttons
	 * @param input is the button an input or output button
	 */
	protected void fileChooser(boolean input, List<String> extensions) {
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
	 * listener method for input/output folder buttons
	 */
	protected void dirChooser() {
		{
			dirChooser.setTitle("Output Folder");
		    dirChooser.setInitialDirectory(
		        new File(System.getProperty("user.dir"))
		    );
			File dir = dirChooser.showDialog(new Stage());
	        if (dir != null) {
	        	outputFilePath.setText(dir.getAbsolutePath());
	        }
		}
	}
	
	/**
	 * Configures the File Open/Save dialog
	 * @param fileChooser FileChooser object to be configured
	 * @param title Title of the dialog box
	 * @param extensions Extensions that can be chosen in the File Open/Save dialog
	 */
	private static void configureFileChooser(final FileChooser fileChooser, String title, List<String> extensions) {
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
	
	/**
	 * allows logging output to be sent to the logArea of the CopyPane
	 * @return a Handler object that can be used by a logger to write messages to the logArea
	 */
	public Handler logToLogArea(){
		class TextAreaHandler extends ConsoleHandler {
			TextAreaHandler(){}
		    @Override
		    public void publish(final LogRecord record) {
		        Platform.runLater(new Runnable() {
		            @Override
		            public void run() {
		                StringWriter text = new StringWriter();
		                PrintWriter out = new PrintWriter(text);
		                out.printf("[%s] [Thread-%d]: %s.%s -> %s", record.getLevel(),
		                        record.getThreadID(), record.getSourceClassName(),
		                        record.getSourceMethodName(), record.getMessage());
		                out.println();
		                logArea.appendText(text.toString());
		            }

		        });
		    }
		}
		return new TextAreaHandler();
	}
	
}

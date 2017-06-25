package xmlTools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * main class
 * @author ilham
 *
 */
public class CopyAndModifyXML extends Application{	
	private static final Logger LOGGER = Logger.getLogger("");
	private static final Logger LOGGERCOPYANDMODIFYXML = Logger.getLogger(CopyAndModifyXML.class.getName());
	
	@Override //Override start method in Application
	public void start(final Stage stage) throws SecurityException, IOException{
		
		stage.setOnCloseRequest(e -> closeWindow(e));
		GridPane inputGridPane;
		inputGridPane = new AccountingCopyPane();
		final Pane rootGroup = new VBox(12);
	    rootGroup.getChildren().addAll(inputGridPane);
	    rootGroup.setPadding(new Insets(12, 12, 12, 12));
	 
	    stage.setScene(new Scene(rootGroup));
			
		stage.setTitle("Copy XML files");
		stage.show();
		        
	}
	
	@Override //Override stop method in Application
	public void stop(){
		LOGGERCOPYANDMODIFYXML.info("Application is closed.");
	}
	
	/**
	 * creates a popup upon window closing
	 * if OK to close, runs platform exit which runs stop
	 * @param e WindowEvent that triggered this function
	 */
	public void closeWindow(WindowEvent e) {
		e.consume();
		Platform.exit();
    }
	
	public static void main(String[] args) {
		try {
			Files.createDirectories(Paths.get("logs"));
			FileHandler loggerFileHandler = new FileHandler("logs/XMLCopy.log", 1024*1024, 1, true);
			loggerFileHandler.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(loggerFileHandler);
			LOGGERCOPYANDMODIFYXML.info("Application started.");
		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE, "Cannot create log file.", e1);
		}
        Application.launch(args);
    }
}

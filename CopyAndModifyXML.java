package xmlTools;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import xmlTools.AccountingCTTZip.AccountingCTTZip;
import xmlTools.UI.AccountingCopyPane;
import xmlTools.UI.CopyPane;
import xmlTools.UI.MultipleCopyPane;

/**
 * main class
 * @author ilham
 *
 */
public class CopyAndModifyXML extends Application{
	private static final Logger LOGGER = Logger.getLogger("");
	private static final Logger LOGGERCOPYANDMODIFYXML = Logger.getLogger(CopyAndModifyXML.class.getName());
	//if stop method was executed
	CopyPane<AccountingCTTZip> accountingPane = new AccountingCopyPane();
	CopyPane<XMLObject> multiplePane = new MultipleCopyPane();
	
	@Override //Override start method in Application
	public void start(final Stage stage){
		//creates a lock to prevent multiple instances of this Application from running
		if (lockInstance("CopyAndModifyXML.lock")){
			try {
				//remove all handlers from root logger
				Handler[] handlers = LOGGER.getHandlers();
				for(Handler handler : handlers) {
				    LOGGER.removeHandler(handler);
				}
				//create StreamHandler for outputting to log area
				Handler loggerAccountingHandler = accountingPane.logToLogArea();
				LOGGER.addHandler(loggerAccountingHandler);
				Handler loggerMultiHandler = multiplePane.logToLogArea();
				LOGGER.addHandler(loggerMultiHandler);
				//create FileHandler for outputting log to a file
				Files.createDirectories(Paths.get("logs"));
				FileHandler loggerFileHandler = new FileHandler("logs/CopyAndModifyXML-log.xml", 1024*1024, 1, true);
				loggerFileHandler.setFormatter(new XMLFormatter());
				LOGGER.addHandler(loggerFileHandler);
				LOGGERCOPYANDMODIFYXML.info("Application started.");
			} catch (IOException e1) {
				LOGGER.log(Level.SEVERE, "Cannot create log file logs/XMLCopy.log.", e1);
			}
		}
		else{
			Alert alert = new Alert(AlertType.ERROR, "Cannot start a new instance while another CopyAndModifyXML application is running.");
			alert.showAndWait();
			Platform.exit();
			LOGGERCOPYANDMODIFYXML.severe("Cannot start a new instance while another CopyAndModifyXML application is running.");
		}
		stage.setOnCloseRequest(e -> closeWindow(e));
		
		TabPane rootGroup = new TabPane();
		Tab accountingTab = new Tab("Copy Accounting Rules");
		accountingTab.setContent(accountingPane);
		Tab multiTab = new Tab("Copy 1 XML into many");
		multiTab.setContent(multiplePane);
		
		rootGroup.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		
	    rootGroup.getTabs().addAll(accountingTab, multiTab);
	    //rootGroup.setPadding(new Insets(12, 12, 12, 12));
	    
	    stage.setScene(new Scene(rootGroup));
	    
	    //make stage non-resizable with maximize button and the little resizing arrows
	    stage.setResizable(false);
			
		stage.setTitle("Copy XML files");
		stage.getIcons().add(new Image(Paths.get(System.getProperty("user.dir")+"/Mewrex.jpg").toUri().toString()));
		
		stage.setHeight(800);
		stage.setWidth(800);
		
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
		accountingPane.abort(e);
		multiplePane.abort(e);
		Platform.exit();
    }
	
	/**
	 * creates a lock file so only one instance of this Application can run across multiple JVMs
	 * deletes lock file and releases lock upon application shutdown
	 * Taken from https://stackoverflow.com/questions/177189/how-to-implement-a-single-instance-java-application
	 * @param lockFile
	 * @return true if there is a lock and so another application is running, false if there is no lock and no other application is running
	 */
	private static boolean lockInstance(final String lockFile) {
	    try {
	        final File file = new File(lockFile);
	        final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
	        final FileLock fileLock = randomAccessFile.getChannel().tryLock();
	        
	        if (fileLock != null) {
	            Runtime.getRuntime().addShutdownHook(new Thread() {
	                public void run() {
	                    try {
	                        fileLock.release();
	                        randomAccessFile.close();
	                        file.delete();
	                    } catch (IOException e) {
	                        LOGGERCOPYANDMODIFYXML.log(Level.WARNING, "Unable to remove lock file: " + lockFile, e);
	                    }
	                }
	            });
	            return true;
	        }
	    } catch (IOException e) {
	    	LOGGERCOPYANDMODIFYXML.log(Level.WARNING, "Unable to create and/or lock file: " + lockFile, e);
	    }
	    return false;
	}
	
	public static void main(String[] args) {
		Application.launch(args);
    }
}

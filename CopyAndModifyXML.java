package xmlTools;

import java.io.IOException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 
 * GUI interface main class
 *
 */
public class CopyAndModifyXML extends Application{

	@Override //Override start method in Application
	public void start(final Stage stage) throws SecurityException, IOException{
		GridPane inputGridPane;
		inputGridPane = new AccountingCopyPane();
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

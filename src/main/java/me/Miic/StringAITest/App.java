package me.Miic.StringAITest;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Hello world!
 *
 */
public class App extends Application
{
	private Button button;
	
	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("testpage.fxml"));
    	Parent root = loader.load();
    	Scene scene = new Scene(root);
    	button = (Button) scene.lookup("#check");
    	button.setDefaultButton(true);
    	MainController controller = loader.getController();
    	controller.passStage(stage);
    	stage.setTitle("WholesomeChat AI Demo");
    	stage.setScene(scene);
    	stage.show();
	}
		
	private void setGlobalEventHandler(Node root) {
	    root.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
	        if (ev.getCode() == KeyCode.ENTER) {
	           button.fire();
	           ev.consume(); 
	        }
	    });
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

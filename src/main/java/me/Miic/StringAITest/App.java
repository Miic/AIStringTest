package me.Miic.StringAITest;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import javafx.stage.Stage;

/**
 * Hello world!
 *
 */
public class App extends Application
{
	private Button button;
	private Button login;
	
	
	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("testpage.fxml"));
    	Parent root = loader.load();
    	Scene scene = new Scene(root);
    	FXMLLoader loginPage = new FXMLLoader(getClass().getResource("login.fxml"));
    	Parent loginRoot = loginPage.load();
    	Scene loginScene = new Scene(loginRoot);
    	
    	button = (Button) scene.lookup("#check");
    	button.setDefaultButton(true);
    	
    	login = (Button) loginScene.lookup("#login");
    	
    	MainController controller = loader.getController();
    	controller.passStage(stage);
    	controller.passAnchor((AnchorPane) scene.lookup("#anchor"));
    	
    	LoginController loginController = loginPage.getController();
    	loginController.passChatScene(scene, controller);
    	loginController.passStage(stage);
    	
    	stage.setResizable(false);
    	stage.setTitle("WholesomeChat AI Demo");
    	stage.setScene(loginScene);
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

package me.Miic.StringAITest;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;

import javafx.stage.Stage;
import me.Miic.StringAITest.Controllers.LoginController;
import me.Miic.StringAITest.Controllers.MainController;

/**
 * Hello world!
 *
 */
public class App extends Application
{	
	@Override
	public void start(Stage stage) throws Exception {
		
		//UI setup
		
		//main chatting loader
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/me/Miic/StringAITest/fxml/testpage.fxml"));
    	Parent root = loader.load();
    	Scene scene = new Scene(root);
    	
    	//intial login page
    	FXMLLoader loginPage = new FXMLLoader(getClass().getResource("/me/Miic/StringAITest/fxml/login.fxml"));
    	Parent loginRoot = loginPage.load();
    	Scene loginScene = new Scene(loginRoot);
    	
    	//Backend setup
    	
    	//Pass appropriate information to the controller classes
    	MainController controller = loader.getController();
    	controller.passStage(stage);
    	controller.passAnchor((AnchorPane) scene.lookup("#anchor"));
    	
    	LoginController loginController = loginPage.getController();
    	loginController.passChatScene(scene, controller);
    	loginController.passStage(stage);
    	
    	
    	//Initialize Stage Displays
    	stage.setResizable(false);
    	stage.setTitle("WholesomeChat AI Demo");
    	stage.setScene(loginScene);
    	stage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

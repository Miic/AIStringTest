package me.Miic.StringAITest;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginController implements Initializable {
	
	private Stage stage;
	private Scene scene;

	@FXML
	public void onButtonAction(ActionEvent event) {
		System.out.println("Button Clicked");
		stage.setScene(scene);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
	}
	
	public void passStage(Stage stage) {
		this.stage = stage;
	}
	
	public void passChatScene(Scene scene) {
		this.scene = scene;
	}

}

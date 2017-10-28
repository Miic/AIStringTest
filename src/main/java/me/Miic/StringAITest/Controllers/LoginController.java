package me.Miic.StringAITest.Controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController implements Initializable {
	
	private Stage stage;
	private Scene scene;
	private MainController passedController;

	/* 
	 * FXML event. Tied to both button and text-enter key
	 * Since this is not hooked up to a SQL database at the moment as it is a client demo,
	 * Simply just checks for both user and pass.
	 * And passes the user used as an argument to the next scene.
	 * 
	 * SQL database implemention should be entered here!
	 * Remember to add regex checking in the fields in case of SQL Injection!
	 * 
	 */
	
	
	@FXML
	public void onButtonAction(ActionEvent event) {
		Scene thisScene = stage.getScene();
		
		TextField userField = (TextField) thisScene.lookup("#username");
		PasswordField passField = (PasswordField) thisScene.lookup("#password");
		
		if (!userField.getText().equals("") && !passField.getText().equals("")) {
			if (!userField.getText().equals("")) {
				passedController.passNick(userField.getText());
			}
			stage.setScene(scene);
		} else {
			userField.clear();
			passField.clear();
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
	}
	
	public void passStage(Stage stage) {
		this.stage = stage;
	}
	
	
	/* 
	 * Used for passing the next scene after login to this controller.
	 * 
	 */
	
	public void passChatScene(Scene scene, MainController controller) {
		this.scene = scene;
		this.passedController = controller;
	}

}

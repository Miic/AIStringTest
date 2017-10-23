package com.elyssiamc.Micc.chatParsingTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneChatOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.Utterance;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.UtteranceAnalyses;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MainController implements Initializable {
	
	private Stage stage;

	@FXML
	public void onButtonAction(ActionEvent event) {
		System.out.println("Button Clicked");
		Scene scene = stage.getScene();
		
		TextField txt = (TextField) scene.lookup("#textfield");
		Label lab = (Label) scene.lookup("#label");
		Label reg = (Label) scene.lookup("#regret");
		PieChart pie = (PieChart) scene.lookup("#pie");
		BarChart<String, Float> bar = (BarChart<String, Float>) scene.lookup("#chart");
		
		if (txt.getText().length() != 0) { 
			String token = txt.getText();
			txt.clear();
			lab.setText("Cat says: " + token);
			
			//Toxicity Display
			ObservableList<PieChart.Data> pieChartData;
			reg.setVisible(false);
			try {
				float percent = getToxicity(token) * 100;
				if (percent >= 90) {
					reg.setVisible(true);
				}
				System.out.println("Toxicity: " + percent);
				pie.setTitle("Toxicity [" + percent + "%]");
				pieChartData = FXCollections.observableArrayList(
						new PieChart.Data("~" + Math.round(percent) + "%", percent),
						new PieChart.Data("", 100-percent)
						);
			} catch (Exception e) {
				pieChartData = FXCollections.observableArrayList(new PieChart.Data("Error", 100));	
			}
			pie.setData(pieChartData);
			
			//Tone Display
			bar.getData().clear();
			bar.setTitle("Tones");
			try {
				Hashtable<String, Float> tones = getTone(token);
				System.out.println(tones);
				Enumeration<String> keys = tones.keys();
				List<XYChart.Series> dat = new ArrayList<XYChart.Series>();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					XYChart.Series newElemente = new XYChart.Series();
					newElemente.setName(key);
					newElemente.getData().add(new XYChart.Data(key + " (" + tones.get(key) * 100 + "%)", tones.get(key) * 100));
					dat.add(newElemente);
				}
				XYChart.Series newElement = new XYChart.Series();
				newElement.setName("Reference");
				newElement.getData().add(new XYChart.Data("Reference (100%)", 100));
				dat.add(0, newElement);
				if (dat.size() > 1) {
					for(XYChart.Series x : dat) {
						bar.getData().add(x);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void initialize(URL location, ResourceBundle resources) {
    	Map<String, String> headers = new HashMap<String, String>();
    	headers.put("X-Watson-Learning-Opt-Out", "true");
    	service.setDefaultHeaders(headers);
	}
	
	public void passStage(Stage stage) {
		this.stage = stage;
	}

	
	
	
	
	
	
	
	

	

	
	
	
	
	
	
	
//     _  __                        __  __                              _____ _____   _  __              
//	  | |/ /                       / _|/ _|                       /\   |  __ \_   _| | |/ /              
//	  | ' / ___  ___ _ __     ___ | |_| |_   _ __ ___  _   _     /  \  | |__) || |   | ' / ___ _   _ ___ 
//	  |  < / _ \/ _ \ '_ \   / _ \|  _|  _| | '_ ` _ \| | | |   / /\ \ |  ___/ | |   |  < / _ \ | | / __|
//	  | . \  __/  __/ |_) | | (_) | | | |   | | | | | | |_| |  / ____ \| |    _| |_  | . \  __/ |_| \__ \
//	  |_|\_\___|\___| .__/   \___/|_| |_|   |_| |_| |_|\__, | /_/    \_\_|   |_____| |_|\_\___|\__, |___/
//	                | |                                 __/ |                                   __/ |    
//	                |_|                                |___/                                   |___/     
	
	
	private static ToneAnalyzer service = new ToneAnalyzer("2017-09-21", "WHAT", "NOTHIN");

    private static float getToxicity(String query) throws MalformedURLException, IOException {
       	HttpURLConnection httpcon = (HttpURLConnection) ((new URL("https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + "REMOVED_AI KEY").openConnection()));
    	httpcon.setDoOutput(true);
    	httpcon.setRequestProperty("Content-Type", "application/json");
    	httpcon.setRequestProperty("Accept", "application/json");
    	httpcon.setRequestMethod("POST");
    	httpcon.connect();
    	
    	JsonParser parser = new JsonParser();
    	JsonObject jObj = parser.parse(
    			"{comment: {text: \"" + query + "\"},"
    			+ "languages: [\"en\"], "
    			+ "requestedAttributes: {TOXICITY:{}} }").getAsJsonObject();

    	//Send Request
    	OutputStream os = httpcon.getOutputStream();
    	PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
    	pw.write(jObj.toString());
    	pw.close();
    	os.close();
    	
    	//Read response
    	InputStream is = httpcon.getInputStream();
    	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    	String line = null;
    	StringBuffer sb = new StringBuffer();
    	while ((line = reader.readLine()) != null) {
    	    sb.append(line);
    	}
    	is.close();
    	
    	//Get specified data needed.
    	JsonObject jResponse = parser.parse(sb.toString()).getAsJsonObject();
    	return jResponse.get("attributeScores").getAsJsonObject().get("TOXICITY").getAsJsonObject().get("summaryScore").getAsJsonObject().get("value").getAsFloat();
    }
    
    private static Hashtable<String, Float> getTone(String query) {
    	ToneChatOptions options = new ToneChatOptions.Builder().addUtterances(new Utterance.Builder().text(query).build()).build();
    	UtteranceAnalyses tone = service.toneChat(options).execute();    	
    	JsonObject parsed = new JsonParser().parse(tone.toString()).getAsJsonObject();
    	JsonArray tones = parsed.get("utterances_tone").getAsJsonArray().get(0).getAsJsonObject().get("tones").getAsJsonArray();
    	Hashtable<String, Float> returnTable = new Hashtable<String,Float>();
    	for(int i = 0; i < tones.size(); i++) {
    		returnTable.put(tones.get(i).getAsJsonObject().get("tone_name").getAsString(), tones.get(i).getAsJsonObject().get("score").getAsFloat());
    	}
    	return returnTable;
    }

}

package me.Miic.StringAITest.Controllers;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.service.exception.UnauthorizedException;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneChatOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.Utterance;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.UtteranceAnalyses;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.chart.RadarChart.Mode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

import me.Miic.StringAITest.Readability.Fathom;
import me.Miic.StringAITest.Readability.Fathom.Stats;
import me.Miic.StringAITest.Readability.Readability;

public class MainController implements Initializable {
	
	private Stage stage;
	private AnchorPane anchor;
	private Tile tile;
	
	private String lazyCache;
	private String nick;
	
	/* Used for changing chart pushing to API
	 * Since this is a demo client this is done here
	 * In a real server-client setup, API pushing should be done on server end.
	 */
	
	@FXML
	public void onButtonAction(ActionEvent event) {
		System.out.println("Button Clicked");
		Scene scene = stage.getScene();
		
		TextField txt = (TextField) scene.lookup("#textfield");
		//TextArea lab = (TextArea) scene.lookup("#label");
		Slider slider = (Slider) scene.lookup("#slider");
		Tile gauge = (Tile) scene.lookup("#gauge");
		Tile circle = (Tile) scene.lookup("#circle");
		@SuppressWarnings("unchecked")
		ListView<String> list = (ListView<String>) scene.lookup("#list");
		
		
		if (txt.getText().length() != 0 && !txt.getText().equals(lazyCache)) { 
			String token = txt.getText();
			
			//Toxicity Display
			
			float percent = 0;
			try {
				percent = getToxicity(token) * 100;
				System.out.println("Toxicity: " + percent);
				gauge.setValue(percent);
			} catch (IOException e) {
				String msg = "<!> Missing API Key for Perspective API. Be sure to enter it in MainController.java!";
				System.out.println(msg);
				ObservableList<String> items = list.getItems();
				if (items == null) {
					items = FXCollections.observableArrayList();
				}
				items.add(msg);
				list.setItems(items);
			} catch (Exception e) {
				gauge.setValue(0);
				e.printStackTrace();
			}
			gauge.setThreshold(slider.getValue());
			
			circle.setValue(getReadability(token).get("Complexity"));
			
			
			//Post to chat?
			if ( slider.getValue() == 0 || percent < slider.getValue() ) {
				txt.clear();
				ObservableList<String> items = list.getItems();
				if (items == null) {
					items = FXCollections.observableArrayList();
				}
				items.add( "\n[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + nick + ": " + token);
				list.setItems(items);
			} else {
				System.out.println("Blocked");
				
				ObservableList<String> items = list.getItems();
				if (items == null) {
					items = FXCollections.observableArrayList();
				}
				items.add("\n[!] Blocked by Toxicity Limiter - " + slider.getValue() + "% <= " + percent + "%");
				list.setItems(items);
				
				txt.setDisable(true);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				txt.clear();
				txt.setDisable(false);
			}
			//Auto scroll to latest thingy ma bobber
			list.scrollTo(list.getItems().get(list.getItems().size() - 1));
			
			
			//Tone Analyzer
			try {
				Hashtable<String, Float> tones = getTone(token);
				System.out.println(tones);
				Enumeration<String> keys = tones.keys();
				int counter = 0;
				List<ChartData> dat = new ArrayList<ChartData>(); 
				ChartData temp = new ChartData(percent);
				temp.setName("Toxicity ~" + Math.round(percent) + "%");
				dat.add(temp);
				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					ChartData newElemente = new ChartData(Math.round(tones.get(key) * 100));
					newElemente.setName(key + " ~" + Math.round(tones.get(key) * 100) + "%");
					dat.add(newElemente);
					counter++;
				}
				while (counter < 4) {
					dat.add(new ChartData(0));
					counter++;
				}
				createToneChart(dat);
			} catch (UnauthorizedException e) {
				String msg = "<!> Missing API Key for Watson API. Be sure to enter it in MainController.java!";
				System.out.println(msg);
				ObservableList<String> items = list.getItems();
				if (items == null) {
					items = FXCollections.observableArrayList();
				}
				items.add(msg);
				list.setItems(items);
			} catch (Exception e) {
				e.printStackTrace();
			}
   		}
	}
	
	/* Used for setting up variable beforehand.
	 * DO NOT USE FOR INITIALIZING TILES OR OBJECTS IN UI
	 * the stage is not guaranteed fully initialized at this point
	 * 
	 */
	
	
	public void initialize(URL location, ResourceBundle resources) {
    	Map<String, String> headers = new HashMap<String, String>();
    	headers.put("X-Watson-Learning-Opt-Out", "true");
    	service.setDefaultHeaders(headers);
    	nick = "Megumin";
	}
	
	/* Used to pass the stage currently being worked on.
	 * Useful for interacting with FXML generated objects.
	 * 
	 */
	
	public void passStage(Stage stage) {
		this.stage = stage;	
	}
	
	/* Used to pass the login name from the Login page
	 * Useful for receiving login info
	 * If more than this info is being passed in the future, rework into a byte based transfer system.
	 * 
	 */

	
	public void passNick(String nick) {
		this.nick = nick;
	}
	
	/* PassAnchor is used by the Application class to pass the anchorpanel being worked on
	 * Allowing the controller class to add new TilesFX since they cannot be 
	 * from an FXML file as it is a third-party library object 
	 */

	public void passAnchor(AnchorPane anchor) {
		this.anchor = anchor;
		ArrayList<ChartData> dat = new ArrayList<>();
		dat.add(new ChartData(50));
		dat.add(new ChartData(50));
		dat.add(new ChartData(50));
		dat.add(new ChartData(50));
		dat.add(new ChartData(50));
		createToneChart(dat);
		
	 	Tile gauge = TileBuilder.create()
	 	        .skinType(SkinType.GAUGE)
	 	        .prefSize(150, 150)
	 	        .layoutX(490)
	 	        .layoutY(440)
	 	        .title("Toxcity")
	 	        .unit("%")
	 	        .threshold(100)
	 	        .thresholdVisible(false)
	 	        .build();
	 	gauge.setId("gauge");
	    anchor.getChildren().add(gauge);
	    
	    Tile circularProgressTile = TileBuilder.create()
                .skinType(SkinType.CIRCULAR_PROGRESS)
                .prefSize(150, 150)
	 	        .layoutX(490 + 150 + 10)
	 	        .layoutY(440)
                .title("Complexity")
                .text("")
                .unit("\u0025")
                //.graphic(new WeatherSymbol(ConditionAndIcon.CLEAR_DAY, 48, Color.WHITE))
                .build();
	    circularProgressTile.setId("circle");
	    anchor.getChildren().add(circularProgressTile);
	}
	
	
	/* Due to the data being dynamic and the chart being static,
	 * giving a Radar Chart data that does not match its current data structure causes it to freeze
	 * Therefore, we must create a new chart and delete the old one at every chart change.
	 */
	
	@SuppressWarnings("unchecked")
	private void createToneChart(List<ChartData> list) {
		if (tile != null) {
			anchor.getChildren().remove(tile);
			tile = null;
		}
		
		List<ChartData> emptyList = new ArrayList<ChartData>();
		for(int i = 0; i < list.size(); i++) {
			emptyList.add(new ChartData(0));
		}
		
	 	tile = TileBuilder.create().skinType(SkinType.RADAR_CHART)
                .prefSize(310, 410)
                .layoutX(490)
                .layoutY(15)
                .minValue(0)
                .maxValue(100)
                //.title("RadarChart Tile")
                .unit("Tones")
                .radarChartMode(Mode.POLYGON)
                .gradientStops(new Stop(0.00000, Color.TRANSPARENT),
                               new Stop(0.00001, Color.web("#3552a0")),
                               new Stop(0.09090, Color.web("#456acf")),
                               new Stop(0.27272, Color.web("#45a1cf")),
                               new Stop(0.36363, Color.web("#30c8c9")),
                               new Stop(0.45454, Color.web("#30c9af")),
                               new Stop(0.50909, Color.web("#56d483")),
                               new Stop(0.72727, Color.web("#9adb49")),
                               new Stop(0.81818, Color.web("#efd750")),
                               new Stop(0.90909, Color.web("#ef9850")),
                               new Stop(1.00000, Color.web("#ef6050")))
                //.text("Sector")
                .tooltipText("")
                .chartData(emptyList)
                .animated(true)
                //.backgroundColor(Color.TRANSPARENT)
                .build();
	 	emptyList = tile.getChartData();
	 	for (int i = 0; i < list.size(); i++) {
	 		emptyList.get(i).setName(list.get(i).getName());
	 		emptyList.get(i).setValue(list.get(i).getValue());
	 	}
	 	
    	anchor.getChildren().add(tile);
	}
	
	
	

	

	
	
	
	
	
	
	
//     _  __                        __  __                              _____ _____   _  __              
//	  | |/ /                       / _|/ _|                       /\   |  __ \_   _| | |/ /              
//	  | ' / ___  ___ _ __     ___ | |_| |_   _ __ ___  _   _     /  \  | |__) || |   | ' / ___ _   _ ___ 
//	  |  < / _ \/ _ \ '_ \   / _ \|  _|  _| | '_ ` _ \| | | |   / /\ \ |  ___/ | |   |  < / _ \ | | / __|
//	  | . \  __/  __/ |_) | | (_) | | | |   | | | | | | |_| |  / ____ \| |    _| |_  | . \  __/ |_| \__ \
//	  |_|\_\___|\___| .__/   \___/|_| |_|   |_| |_| |_|\__, | /_/    \_\_|   |_____| |_|\_\___|\__, |___/
//	                | |                                 __/ |                                   __/ |    
//	                |_|                                |___/                                   |___/     
	
	/* 
	 * Used for calling Toxicity AI checks.
	 * 
	 */
	
	private static ToneAnalyzer service = new ToneAnalyzer("2017-09-21", "REMOVED USED", "REMOVED PASS");

    private static float getToxicity(String query) throws MalformedURLException, IOException {
       	HttpURLConnection httpcon = (HttpURLConnection) ((new URL("https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + "REMOVED KEY").openConnection()));
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
    
	/* 
	 * Used for calling Tone checks.
	 * Hashtable is used for return because the tone of the data returned is not consistent.
	 * Recommended to use the key enumerable to iterate through the keys.
	 * 
	 */
	
    
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
    
	/* 
	 * Used for calling Readability checks from the local library. (Algorithm based, not AI)
	 * 
	 */
	
    
    private static Hashtable<String, Float> getReadability(String query) {
    	Hashtable<String, Float> returnTable = new Hashtable<String,Float>();
    	Stats info = Fathom.analyze(query);
    	returnTable.put("Flesch", Readability.calcFlesch(info));
    	returnTable.put("Fog", Readability.calcFog(info));
    	returnTable.put("Kincaid", Readability.calcFlesch(info));
    	returnTable.put("Complexity", Readability.percentComplexWords(info));
    	returnTable.put("Syllables", Readability.syllablesPerWords(info));
    	return returnTable;
    }

}

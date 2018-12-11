package com.hanshow.support.monitor.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ReportController implements Initializable {

	@FXML
	private AnchorPane ap;
	@FXML
	private TextArea content;
	@FXML
	private Button finish;
	@FXML
	private Button backward;
	
	private StringBuffer answers = new StringBuffer();
	
	private static Logger logger = LoggerFactory.getLogger(ReportController.class);
	private ResourceBundle resources;

	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		resources = arg1;
		new Thread(new MyTask()).start();
	}
	
	@FXML
	private void finish() {
		
	}
	
	@FXML
	private void backward() {
		Stage stage = (Stage) ap.getScene().getWindow();
		Scene scene;
		try {
			scene = new Scene(FXMLLoader.load(getClass().getResource("/view/eslworking.fxml"),resources));
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
	}

	class MyTask extends Task<Boolean> {
		
		@Override
		protected void succeeded() {
			content.setText(answers.toString());
		}
		
		@Override
		protected void failed() {

		}
		
		@Override
		protected Boolean call() {
			EslworkingController controller = (EslworkingController) StageManager.controllers.get(EslworkingController.class.getSimpleName());
			for(String key : controller.statistics.keySet()) {
				answers.append(key.substring(0, key.lastIndexOf("."))).append("\n\n");
				File file = new File(this.getClass().getClassLoader().getResource("answer").getPath() + File.separator + key);
				if (!file.exists()) {
					return false;
				}
				try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {	
					String line = "";
					while(null != (line =reader.readLine())) {
						answers.append(line + "\n");
					}
				} catch (FileNotFoundException e) {
					logger.error(e.getMessage(), e);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				
				answers.append("\n\n\n");
			}
			return true;
		}
		
	}
}

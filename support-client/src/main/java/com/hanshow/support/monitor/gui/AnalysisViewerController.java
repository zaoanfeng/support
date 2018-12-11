package com.hanshow.support.monitor.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class AnalysisViewerController implements Initializable {

	@FXML
	private AnchorPane ap;
	@FXML
	private TextField path;
	@FXML
	private Label serverPort;
	@FXML
	private ChoiceBox<String> chooseSystem;
	@FXML
	private Button forward;
	@FXML
	private Button choosePath;
	@FXML
	private Label message;
	private Stage stage;
	
	private ResourceBundle resource;
	public String folderPath;

	private static Logger logger = LoggerFactory.getLogger(AnalysisViewerController.class);
	
	/**
	 * 设置界面打开的样式
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		resource = arg1;
		StageManager.stages.put(this.getClass().getSimpleName(), stage);
		StageManager.controllers.put(this.getClass().getSimpleName(), this);
		// 默认协议使用RESTful
		chooseSystem.setItems(FXCollections.observableArrayList(new String[] { "ESL-Working", "Shopweb" }));
		chooseSystem.getSelectionModel().selectFirst();
		message.setVisible(false);
	}
	
	@FXML
	public void choosePath() {
		stage = (Stage) ap.getScene().getWindow();
		DirectoryChooser directoryChooser=new DirectoryChooser();
		directoryChooser.setTitle("选择目录");
		File file = directoryChooser.showDialog(stage);
		message.setVisible(false);
		if (file != null) {
			folderPath = file.getPath();//选择的文件夹路径
			path.setText(folderPath);
		}	
	}
	
	@FXML
	public void forward() {
		stage = (Stage) ap.getScene().getWindow();
		try {
			if("ESL-Working".equals(chooseSystem.getSelectionModel().getSelectedItem())) {
				File file = new File(path.getText() + File.separator + "log");
				if (file.exists() && file.isDirectory()) {
					folderPath = file.getPath();
					Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/view/eslworking.fxml"),resource));
					stage.setScene(scene);
				} else {
					message.setVisible(true);
					message.setText("ESL-Working路径不正确");
				}
				
			} else {
				File file = new File(path.getText() + File.separator + "logs");
				if (file.exists() && file.isDirectory()) {
					folderPath = file.getPath();
					Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/view/shopweb.fxml"),resource));
					stage.setScene(scene);
				} else {
					message.setVisible(true);
					message.setText("Shopweb路径不正确");
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}	
	}
}

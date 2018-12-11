package com.hanshow.support.monitor.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hanshow.support.monitor.util.AnalysisUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class EslworkingController implements Initializable {

	@FXML
	private AnchorPane ap;
	@FXML
	private ListView<String> listView;
	@FXML
	private ImageView process;
	@FXML
	private Button finish;
	@FXML
	private Button backward;
	@FXML
	private Button scan;
	
	private Stage stage;
	
	private Map<String, String> problemMap = new HashMap<>();
	private String[] logFiles = {"eslworking.log"};
	public Map<String, List<Integer>> statistics = new HashMap<>();
	
	private ObservableList<String> records = FXCollections.observableArrayList();
	private static Logger logger = LoggerFactory.getLogger(EslworkingController.class);
	private ResourceBundle resources;

	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		resources = arg1;
		StageManager.stages.put(this.getClass().getSimpleName(), stage);
		StageManager.controllers.put(this.getClass().getSimpleName(), this);
		listView.setItems(records);
		finish.setDisable(true);
	}
	
	@FXML
	private void scan() {
		records.add("开始扫描............");
		process.setVisible(true);
		scan.setDisable(true);
		new Thread(new MyTask()).start();
	}
	
	@FXML
	private void finish() {
		stage = (Stage) ap.getScene().getWindow();
		Scene scene;
		try {
			scene = new Scene(FXMLLoader.load(getClass().getResource("/view/report.fxml"),resources));
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@FXML
	private void backward() {
		Stage stage = (Stage) ap.getScene().getWindow();
		Scene scene;
		try {
			scene = new Scene(FXMLLoader.load(getClass().getResource("/view/analysisViewer.fxml"),resources));
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
	}

	class MyTask extends Task<Boolean> {
		
		@Override
		protected void succeeded() {
			for(String key : statistics.keySet()) {
			//	int count = 0;
				int count = statistics.get(key).stream().max((a,b) -> a.compareTo(b)).get();
				/*for(Integer i :statistics.get(key)) {
					count += i;
				}*/
				float value = ((float)count / (float)(problemMap.get(key).split(" ").length));
				if (value > 0.7) {
					records.add(key.substring(0,key.lastIndexOf(".")));
				}
			}
			records.add("检测完成");
			scan.setDisable(false);
			finish.setDisable(false);
		}
		
		@Override
		protected void failed() {
			records.add("检测失败");
			scan.setDisable(false);
		}
		
		@Override
		protected Boolean call() {
			if (!loadProblem()) {
				return false;
			}
			
			AnalysisViewerController controller = (AnalysisViewerController) StageManager.controllers.get(AnalysisViewerController.class.getSimpleName());
			String eslworkingPath = controller.folderPath;
			File logPath = new File(eslworkingPath);
			if (logPath.exists()) {
				//迭代日志文件夹，并读取文件文件
				for(String logFile : logFiles) {
					File file = new File(logPath.getPath() + File.separator + logFile);
					if(file.exists()) {
						try {
							statistics = AnalysisUtils.analysis(file, problemMap);
						} catch (IOException e) {
							logger.error(e.getMessage());
							//return false;
						}
					}
				}
				return true;
			}
			
			
			return false;
		}
		
		/**
		 * 加载已知文件列表
		 * @return
		 */
		private boolean loadProblem() {
			File file = new File(this.getClass().getClassLoader().getResource("problem").getPath());
			if (!file.exists() || !file.isDirectory()) {
				return false;
			}
			String[] fileNames =file.list();
			for(String name : fileNames) {
				
				try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file.getPath() + File.separator + name)))) {	
					String content = "";
					String line = "";
					while(null != (line =reader.readLine())) {
						content += line;
					}
					problemMap.put(name, content);
				} catch (FileNotFoundException e) {
					logger.error(e.getMessage(), e);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				
			}
			return true;
		}
		
	}
}

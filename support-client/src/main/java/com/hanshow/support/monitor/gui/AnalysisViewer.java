package com.hanshow.support.monitor.gui;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class AnalysisViewer extends Application {
	
	@Override
	public void start(Stage stage) {
		try {

			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/analysisViewer.fxml"));
	    	Parent root = fxmlLoader.load();
	        Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String... args){
		launch(args);

		// 渲染模板启动了后台线程，必须现实调用System.exit才能完全退出。
		System.exit(0);
	}
}

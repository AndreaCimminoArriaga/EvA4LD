package tdg.link_discovery.framework.gui.desktop.controllers;

import java.io.File;

import org.apache.commons.io.FileUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;

public class DatasetsLoadController {
	
	   @FXML
	    private ProgressBar loadingBar;

	    @FXML
	    private Button cancelButton;
	    	    
	    private static Thread loadingThread;
	    private static String tdbDatasetLoading;
	    @FXML
	    protected void initialize(){
	    	MainController.addNewDatasetStage.close(); // hide this view, it will be closed after the task execution

	    	loadingBar.setProgress(-1.0);
	    }
	    

	    @FXML
	    void cancelLoadingDataset(MouseEvent event) {
	    	try{
	    		loadingThread.interrupt();
	    		FileUtils.deleteDirectory(new File(tdbDatasetLoading));
	    	}catch(Exception e){
	    		//e.printStackTrace();
	    	}
	    }
	    
	    public static void setLoadingDatasetFeatures(Thread thread, String tdbFullPathFile){
	    	loadingThread = thread;
	    	tdbDatasetLoading = tdbFullPathFile;
	    }
	    
	   
}

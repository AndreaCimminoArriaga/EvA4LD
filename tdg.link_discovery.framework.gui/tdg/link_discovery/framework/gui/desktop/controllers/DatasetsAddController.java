package tdg.link_discovery.framework.gui.desktop.controllers;

import java.io.File;
import java.util.List;

import tdg.link_discovery.framework.gui.desktop.GuiConfiguration;
import tdg.link_discovery.framework.tools.data_loader.ILoader;
import tdg.link_discovery.framework.tools.data_loader.JenaTDBLoader;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class DatasetsAddController {

	@FXML
	private AnchorPane draggableArea;

	@FXML
	private TextField datasetFullPathInput;

	@FXML
	private Button loadDatasetButton;

	@FXML
	private TextField datasetName;

	@FXML
	private Label errorExtensionNotSupported;

    @FXML
    private Label errorEmptyDatasetName;

    @FXML
    private Label errorSpecifyFilePath;

    @FXML
    private Label errorNameAlreadyExists;
   
	@FXML
	void loadDatasetFile(MouseEvent event) {
		disableErrorLabels();
		Boolean isValidInput = isValidInput();
		if(isValidInput){
			// In case input name already exists, show error
			if(DatasetsController.datasetInputNameAlreadyExists(datasetName.getText().trim())){
				errorNameAlreadyExists.setVisible(true);
			}else{
				// Otherwise load the RDF dataset
				disableModificationButtons();
				StringBuffer tdbFullPath = new StringBuffer();
				tdbFullPath.append(GuiConfiguration.DATA_DIRECTORY).append(datasetName.getText().trim()); 
				try {
					Stage loadingDatasetStage = MainController.createNewStage("Uploadig content", "views/addDatasetLoading.fxml");
	    	        // Create task in another thread to load data
					Task<Boolean> task = createALoadDatasetTask(loadingDatasetStage, tdbFullPath.toString(), datasetFullPathInput.getText().trim());
	    	    	Thread thread = new Thread(task);
	    		 	thread.start();
	    		 	DatasetsLoadController.setLoadingDatasetFeatures(thread, tdbFullPath.toString());
	            } catch(Exception e) {
	               e.printStackTrace();
	            }
			}
		}
		
		event.consume();
	}
	
	private Task<Boolean> createALoadDatasetTask(Stage stage, String tdbFullpath, String datasetFullPathFile){
		Task<Boolean> task = new Task<Boolean>() {
    	    @Override public Boolean call() {
    	    	ILoader loader = new JenaTDBLoader();
    	    	Boolean success = loader.loadDataFromFile(tdbFullpath, datasetFullPathFile);
    	    	return success;
    	    }
    	};
    	task.setOnRunning((e) -> stage.show()); // show the stage when running task
    	task.setOnSucceeded((e) -> {
    		//stage.hide();
    		stage.close(); // Close stage on success
    	    try {
    	    	Boolean successfullyFinished = task.get();
    	    	if(successfullyFinished){
    	    		//MainController.addNewDatasetStage.close();
    	    		MainController.datasetsManagerStage.close();
    	    	}
				//Handle task finished succesfully ?
			} catch (Exception e1) {
				e1.printStackTrace();
			}
    	});
    	task.setOnFailed((e) -> {
    		displayErrorLoadingDataset();
    	});
    	
    	return task; 
	}
	
	private void displayErrorLoadingDataset(){
		//TODO:
	}
	
	 private Boolean isValidInput(){
	    	if(datasetName.getText().isEmpty())
				errorEmptyDatasetName.setVisible(true);
			if(datasetFullPathInput.getText().isEmpty())
				errorSpecifyFilePath.setVisible(true);
			return !datasetName.getText().isEmpty() && !datasetFullPathInput.getText().isEmpty();
	}

	private void disableModificationButtons(){
		loadDatasetButton.setDisable(true);
		datasetFullPathInput.setDisable(true);
		datasetName.setDisable(true);
	}
	
	private void disableErrorLabels(){
		errorExtensionNotSupported.setVisible(false);
		errorEmptyDatasetName.setVisible(false);
		errorSpecifyFilePath.setVisible(false);
		errorNameAlreadyExists.setVisible(false);
	}

	/*
	 * Including info from dragged file
	 */

	
	@FXML
	void onDragDropped(DragEvent event) {
		disableErrorLabels();
		try {
			Dragboard board = event.getDragboard();
			List<File> phil = board.getFiles();
			String file = phil.get(0).getAbsolutePath();
			if (file.endsWith("nt")) {
				datasetFullPathInput.setText(file);
			} else {
				// show not supported file exception
				errorExtensionNotSupported.setVisible(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			errorExtensionNotSupported.setVisible(true);
		}
		draggableArea.setEffect(null);
		event.consume();
	}

	/*
	 * Activate drag visual effects
	 */

	@FXML
	void onDragEntered(DragEvent event) {
		draggableArea.setEffect(new GaussianBlur());
		event.consume();
	}

	@FXML
	void onDragExited(DragEvent event) {
		draggableArea.setEffect(null);
		event.consume();
	}

	/*
	 * Activate drop transactions
	 */

	@FXML
	void onDragOver(DragEvent event) {
		Dragboard board = event.getDragboard();
		if (board.hasFiles()) {
			event.acceptTransferModes(TransferMode.ANY);
		}
		event.consume();
	}

}

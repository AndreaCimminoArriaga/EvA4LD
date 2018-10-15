package tdg.link_discovery.framework.gui.desktop.controllers;

import java.util.List;
import java.util.stream.Collectors;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.WindowEvent;

public class DatasetsController {


	    @FXML
	    private AnchorPane dataAnchorPanel;
		
	  	@FXML
	    private Button newDatasetButton;

	    @FXML
	    private TextField datasetFilterByName;
	    
	    @FXML
	    private ListView<String> datasetsList;
	    
	    
	    @FXML
	    private Button removeDatasetButton;

	    
	    @FXML
	    public void initialize() {
	    	List<String> datasetsNameList = MainController.datasetsConnector.getListOfStoredDatasets();
	    	datasetsNameList.stream().forEach(datasetName -> datasetsList.getItems().add(datasetName));
	    }
	    
	    /*
	     * Add new dataset: only opens the add dataset window
	     */
	    
	    
	    @FXML
	    void addNewDataset(MouseEvent event) {
	    	try {
				MainController.loadingDataset = true;
				MainController.addNewDatasetStage = MainController.createNewStage("New Dataset", "views/addDatasetGui.fxml");
				MainController.addNewDatasetStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
							@Override
							public void handle(WindowEvent we) {
								MainController.addNewDatasetStage.close();
							}
						});
				MainController.addNewDatasetStage.show();
	    	       
	    		} catch(Exception e) {
	               e.printStackTrace();
	            }
	    	event.consume();
	    }
	    
	    /*
	     * Filter datasets list
	     */
	    
	    @FXML
	    void datasetNameFilter(KeyEvent event) {
	    	removeDatasetButton.disableProperty().set(true); // disable remove button 
	    	StringBuffer str = new StringBuffer(datasetFilterByName.getText());
	    	str.append(event.getCharacter());
	    	String filterString = str.toString();
	    	List<String> datasetsNameList = MainController.datasetsConnector.getListOfStoredDatasets();
	    	List<String> toRemove = datasetsNameList.stream().filter(elem -> !areSimilarStrings(elem, filterString)).collect(Collectors.toList());
	    	datasetsNameList.stream().forEach(datasetName -> {
	    		if(!datasetsList.getItems().contains(datasetName))
	    			datasetsList.getItems().add(datasetName);
	    	});
	    	toRemove.stream().forEach(datasetName -> datasetsList.getItems().remove(datasetName));
	    	//event.consume();
	    }
	    
	    private Boolean areSimilarStrings(String element, String filter){
	    	Boolean filterString = true;
	    	if(!element.contains(filter))
	    		filterString = false;
	    	
	    	return filterString;
	    }
	    	
	    @FXML
	    void selectedDataset(MouseEvent event) {
	    	removeDatasetButton.disableProperty().set(false);
	    	event.consume();
	    }
	 
	    @FXML
	    void disableRemoveButton(MouseEvent event) {
	    	removeDatasetButton.disableProperty().set(true);
	    	event.consume();
	    }
	    
	    @FXML
	    void removeCurrentDataset(MouseEvent event) {
	    	String selectedDataset = datasetsList.getSelectionModel().getSelectedItem().replaceAll("\\([0-9]+\\s*[A-Za-z]+\\)\\s*$", "").trim();
	    	
	    	MainController.datasetsConnector.removeDataset(selectedDataset);
			datasetsList.getItems().remove(datasetsList.getSelectionModel().getSelectedItem());
			
	    	removeDatasetButton.disableProperty().set(false);
	    	event.consume();
	    }

		public static boolean datasetInputNameAlreadyExists(String datasetInputName) {
			return MainController.datasetsConnector.getListOfStoredDatasets().stream().anyMatch(elem -> elem.replaceAll("\\([0-9]+\\s*[A-Za-z]+\\)\\s*$", "").trim().equals(datasetInputName));
		}
	    
}

package tdg.link_discovery.framework.gui.desktop.controllers;

import java.util.Map;
import org.apache.jena.ext.com.google.common.collect.Maps;

import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;

public class AlgorithmsAddController {

	 	@FXML
	    private ComboBox<String> initializerComboBox;

	    @FXML
	    private ComboBox<String> selectorComboBox;

	    @FXML
	    private ComboBox<String> replacementComboBox;

	    @FXML
	    private ComboBox<String> fitnessComboBox;

	    @FXML
	    private ComboBox<String> crossoverComboBox;

	    @FXML
	    private ComboBox<String> mutationComboBox;

	    @FXML
	    private Button cancelButton;

	    @FXML
	    private Button nextButton;

	    @FXML
	    private TextField algorithmNameInput;

	    @FXML
	    private Label errorNameAlreadyExists;
	    
	    @FXML
	    private ListView<String> aggregateList;

	    @FXML
	    private ListView<String> metricsList;

	    @FXML
	    private ListView<String> transformationsList;

	    @FXML
	    private ComboBox<String> attrLearnerComboBox;
	    
	    @FXML
		public void initialize() {
	    	initializerComboBox.getItems().setAll(MainController.algorithmConnector.getIntializersNameList());
	    	selectorComboBox.getItems().setAll(MainController.algorithmConnector.getSelectorsNameList());
	    	replacementComboBox.getItems().setAll(MainController.algorithmConnector.getReplacementsNameList());
	    	fitnessComboBox.getItems().setAll(MainController.algorithmConnector.getFitnessNameList());
	    	crossoverComboBox.getItems().setAll(MainController.algorithmConnector.getCrossoversNameList());
	    	mutationComboBox.getItems().setAll(MainController.algorithmConnector.getMutationsNameList());
	    	attrLearnerComboBox.getItems().setAll(MainController.algorithmConnector.getAttributeLearnerNameList());
	    	
	    	
	    	
	    	aggregateList.getItems().addAll(MainController.algorithmConnector.getAggregatesNameList());
	    	metricsList.getItems().addAll(MainController.algorithmConnector.getMetricsNameList());
	    	transformationsList.getItems().addAll(MainController.algorithmConnector.getTransformationsNameList());
	    	
	    	aggregateList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	    	metricsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	    	transformationsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	    }
	    
	    
	    @FXML
	    void cancelButtonClicked(MouseEvent event) {
	    	MainController.addNewAlgorithmStage.close();
	    }

	    @FXML
	    void nextButtonClicked(MouseEvent event) {
	    	Map<String,String> geneticOperators = Maps.newHashMap();
	    	geneticOperators.put(FrameworkConfiguration.ALGORITHM_SELECTOR_INFILE_TOKEN, selectorComboBox.getSelectionModel().getSelectedItem());
	    	geneticOperators.put(FrameworkConfiguration.ALGORITHM_INITIALIZATION_INFILE_TOKEN, initializerComboBox.getSelectionModel().getSelectedItem());
	    	geneticOperators.put(FrameworkConfiguration.ALGORITHM_REPLACEEMT_INFILE_TOKEN, replacementComboBox.getSelectionModel().getSelectedItem());
	    	geneticOperators.put(FrameworkConfiguration.ALGORITHM_FITNESS_INFILE_TOKEN, fitnessComboBox.getSelectionModel().getSelectedItem());
	    	geneticOperators.put(FrameworkConfiguration.ALGORITHM_CROSSOVER_INFILE_TOKEN, crossoverComboBox.getSelectionModel().getSelectedItem());
	    	geneticOperators.put(FrameworkConfiguration.ALGORITHM_MUTATION_INFILE_TOKEN, mutationComboBox.getSelectionModel().getSelectedItem());
	    	geneticOperators.put(FrameworkConfiguration.ALGORITHM_NAME_INFILE_TOKEN, algorithmNameInput.getText());
	    	
	    	if(!attrLearnerComboBox.getSelectionModel().isEmpty()){
	    		geneticOperators.put(FrameworkConfiguration.ALGORITHM_ATTRIBUTE_LEARNER_INFILE_TOKEN, attrLearnerComboBox.getSelectionModel().getSelectedItem());
	    	}else{
	    		geneticOperators.put(FrameworkConfiguration.ALGORITHM_ATTRIBUTE_LEARNER_INFILE_TOKEN, null);
	    	}
	    	String aggregations = prepareLineOfFunctions(aggregateList.getSelectionModel().getSelectedItems());
	    	geneticOperators.put(FrameworkConfiguration.ALGORITHM_AGGREGATES_INFILE_TOKEN, aggregations);
	    	String metrics = prepareLineOfFunctions(metricsList.getSelectionModel().getSelectedItems());
	    	geneticOperators.put(FrameworkConfiguration.ALGORITHM_STRING_METRICS_INFILE_TOKEN, metrics);
	    	String transformations = prepareLineOfFunctions(transformationsList.getSelectionModel().getSelectedItems());
	    	geneticOperators.put(FrameworkConfiguration.ALGORITHM_TRANSFORMATIONS_INFILE_TOKEN, transformations);
	    	
	    	if(!algorithmInputNameAlreadyExists(algorithmNameInput.getText())){
	    		Boolean correctlyWritten = MainController.algorithmConnector.addAlgorithm(geneticOperators);
	    		if(correctlyWritten)
	    			restartWindow();
	    	}else{
	    		errorNameAlreadyExists.setVisible(true);
	    	}
	    	
	    }
	    
	    private void restartWindow(){
	    	MainController.algorithmsManagerStage.close();
	    	MainController.algorithmsManagerStage = MainController.createNewStage("Algorithms Manager","views/algorithmsTab.fxml");
	    	MainController.algorithmsManagerStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	            @Override
				public void handle(WindowEvent we) {
	            	MainController.algorithmsManagerStage.close();
	            }});   
	    	MainController.addNewAlgorithmStage.close();
	    }
	    
	    private String prepareLineOfFunctions(ObservableList<String> list){
	    	StringBuffer line = new StringBuffer();
	    	if(list.isEmpty() || list == null)
	    		return null;
	    	list.forEach(elem -> line.append(",").append(elem));
	    	String lineToReturn = line.replace(0, 1, "").toString();
	    	return lineToReturn;
	    }
	    
	    
		private boolean algorithmInputNameAlreadyExists(String algorithmInputName) {
			return MainController.algorithmConnector.getListOfStoredAlgorithmsNames().stream().anyMatch(elem -> elem.trim().equals(algorithmInputName));
		}
	    
	    @FXML
	    void checkValidInput(ActionEvent event) {
	    	if(allCheckBoxesMarked()){
	    		nextButton.setDisable(false);
	    	}else{
	    		nextButton.setDisable(true);
	    	}
	    }
	    
	    @FXML
	    void algorithmNameTyped(KeyEvent event) {
	    	errorNameAlreadyExists.setVisible(false);
	    	if(allCheckBoxesMarked()){
	    		nextButton.setDisable(false);
	    	}else{
	    		nextButton.setDisable(true);
	    	}
	    	
	    }
	   
	    
	    private Boolean allCheckBoxesMarked(){
	    	//TODO: can crossover and mutation be empty ?
	    	return !initializerComboBox.getSelectionModel().isEmpty() &&
	    			!selectorComboBox.getSelectionModel().isEmpty() &&
	    			!replacementComboBox.getSelectionModel().isEmpty() &&
	    			!fitnessComboBox.getSelectionModel().isEmpty() &&
	    			!crossoverComboBox.getSelectionModel().isEmpty() &&  
	    			!mutationComboBox.getSelectionModel().isEmpty() && !algorithmNameInput.getText().isEmpty() && 
	    			metricsList.getSelectionModel().getSelectedItems()!=null;
	    }
	    

	
}

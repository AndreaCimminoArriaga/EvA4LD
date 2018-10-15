package tdg.link_discovery.framework.gui.desktop.controllers;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

public class ExperimentsController {

    private static Thread executingThread;
    
	@FXML
	private ListView<String> algorithmsList;

	@FXML
	private ListView<String> evaluatorsList;
	
	@FXML
	private ListView<String> environmentsList;

	@FXML
	private TextField experimentNameTextInput;

	@FXML
	private CheckBox pruneStrMetricsCheckbox;

	@FXML
	private CheckBox strTransformationsCheckbox;

	@FXML
	private CheckBox attrSelectorCheckbox;
	
	@FXML
	private CheckBox contextNodesCheckbox;

	@FXML
	private TableView<ExperimentRow> tableOfExperiments;

	@FXML
	private Button addExperimentButton;

	@FXML
	private Button startExperimentsButton;

	@FXML
	private Button cancelButton;

	@FXML
	private ProgressBar progressBar;

	@FXML
	private TableColumn<ExperimentRow, String> algorithmColumn;

	@FXML
	private TableColumn<ExperimentRow, String> environmentColumn;

	@FXML
	private TableColumn<ExperimentRow, Integer> executionsColumn;

	@FXML
	private TableColumn<ExperimentRow, Boolean> pruneMetricsColumn;

	@FXML
	private TableColumn<ExperimentRow, Boolean> stringTransformationsColumn;

	@FXML
	private TableColumn<ExperimentRow, Boolean> attributeSelectorColumn;

	@FXML
	private TableColumn<ExperimentRow, Boolean> contextColumn;
	
    @FXML
    private Button removeExperimentRow;
    
    @FXML
    private CheckBox executeInParallelCheckBox;

    @FXML
    private TextField executionNumberInput;
	
	@FXML
	public void initialize() {
		List<String> environmentsStored = MainController.environmentConnector.getListOfStoredEnvironments();
		environmentsStored.stream().forEach(name -> environmentsList.getItems().add(name));

		List<String> algorithmsStored = MainController.algorithmConnector.getListOfStoredAlgorithmsNames();
		algorithmsStored.stream().forEach(name -> algorithmsList.getItems().add(name));
		
		contextNodesCheckbox.setDisable(true);
		removeExperimentRow.setDisable(true);
		//Initialize table columns factories
		algorithmColumn.setCellValueFactory(new PropertyValueFactory<>("algorithmName"));
		environmentColumn.setCellValueFactory(new PropertyValueFactory<>("environmentName"));
		executionsColumn.setCellValueFactory(new PropertyValueFactory<>("executions"));
		//TODO: MAKE THIS ROWS EDITABLE
		pruneMetricsColumn.setCellValueFactory(new PropertyValueFactory<>("pruneMetrics"));
		stringTransformationsColumn.setCellValueFactory(new PropertyValueFactory<>("stringTransformations"));
		attributeSelectorColumn.setCellValueFactory(new PropertyValueFactory<>("attrSelector"));
		contextColumn.setCellValueFactory(new PropertyValueFactory<>("contextNode"));
		
		// Table initialization to empty
		tableOfExperiments.setPlaceholder(new Label("Empty table"));
		tableOfExperiments.setEditable(false);
	}
	

	@FXML
	void addExperimentButtonClicked(MouseEvent event) {
		String algorithmSelected = algorithmsList.getSelectionModel().getSelectedItem();
		String enivornmentSelected = environmentsList.getSelectionModel().getSelectedItem();
		Boolean selectedStringTransformations = strTransformationsCheckbox.selectedProperty().get();
		Boolean selectedStringMetrics = pruneStrMetricsCheckbox.selectedProperty().get();
		Boolean selectedAttributeSelector = attrSelectorCheckbox.selectedProperty().get();
		Boolean selectedContextNodes = contextNodesCheckbox.selectedProperty().get();
		String executionsNumber = executionNumberInput.getText();
	
		if(algorithmSelected!= null && enivornmentSelected!=null && isANumber(executionsNumber)){
			Integer numberOfExecutions = Integer.valueOf(executionsNumber);
			ExperimentRow newRow = new ExperimentRow(selectedStringMetrics,selectedStringTransformations,selectedAttributeSelector, selectedContextNodes,algorithmSelected,enivornmentSelected,numberOfExecutions);
			tableOfExperiments.getItems().add(newRow);
			tableOfExperiments.setEditable(false);
			tableOfExperiments.refresh();
		}
	
		deselectAllElements();
	}

	private boolean isANumber(String string) {
		return string.matches("^-?\\d+$");
	}
	
	@FXML
    void tableOfExperimentsClicked(MouseEvent event) {
		if(tableOfExperiments.getItems().size()>0)
			removeExperimentRow.setDisable(false);
    	tableOfExperiments.setEditable(false);
    }
	
	@FXML
	void removeExperimentRow(MouseEvent event) {
		if(tableOfExperiments.getItems().size()>0){
			ExperimentRow rowToRemove = tableOfExperiments.getSelectionModel().getSelectedItem();
			tableOfExperiments.getItems().remove(rowToRemove);
			tableOfExperiments.refresh();
			deselectAllElements();
		}	
	}
	
	@FXML
	void experimentsAnchorPanelClicked(MouseEvent event) {
		  //deselectAllElements();
	}

	@FXML
	void cancelButtonClicked(MouseEvent event) {
		try{
    		executingThread.interrupt();
    	}catch(Exception e){
    		//e.printStackTrace();
    	}
	}

	@FXML
	void startExperimentsButtonClicked(MouseEvent event) {
		// check exists experimentname is not empty
		String experimentName = experimentNameTextInput.getText();
		if(experimentName!=null && !experimentName.isEmpty()){
			Boolean executeInParallel = executeInParallelCheckBox.selectedProperty().get();
			// TODO: generate thread and launch everything
		}else{
			// TODO: show no experiment name at input error
		}
	}
	
	private void deselectAllElements(){
		algorithmsList.getSelectionModel().clearSelection();;
		environmentsList.getSelectionModel().clearSelection();
		strTransformationsCheckbox.selectedProperty().set(false);
		pruneStrMetricsCheckbox.selectedProperty().set(false);
		attrSelectorCheckbox.selectedProperty().set(false);
		contextNodesCheckbox.selectedProperty().set(false);
		executionNumberInput.setText("1");
		tableOfExperiments.getSelectionModel().clearSelection();
		removeExperimentRow.setDisable(true);
		tableOfExperiments.setEditable(false);
	}
	
	
	
	
	public class ExperimentRow{
		private Boolean pruneMetrics, stringTransformations, attrSelector, contextNode;
		private String algorithmName;
		private String environmentName;
		private Integer executions;
		
		public ExperimentRow(Boolean pruneMetrics,
				Boolean stringTransformations, Boolean attrSelector,
				Boolean contextNode, String algorithmName,
				String environmentName, Integer executions) {
			super();
			this.pruneMetrics = pruneMetrics;
			this.stringTransformations = stringTransformations;
			this.attrSelector = attrSelector;
			this.contextNode = contextNode;
			this.algorithmName = algorithmName;
			this.environmentName = environmentName;
			this.executions = executions;
		}

		public Boolean getPruneMetrics() {
			return pruneMetrics;
		}

		public void setPruneMetrics(Boolean pruneMetrics) {
			this.pruneMetrics = pruneMetrics;
		}

		public Boolean getStringTransformations() {
			return stringTransformations;
		}

		public void setStringTransformations(Boolean stringTransformations) {
			this.stringTransformations = stringTransformations;
		}

		public Boolean getAttrSelector() {
			return attrSelector;
		}

		public void setAttrSelector(Boolean attrSelector) {
			this.attrSelector = attrSelector;
		}

		public Boolean getContextNode() {
			return contextNode;
		}

		public void setContextNode(Boolean contextNode) {
			this.contextNode = contextNode;
		}

		public String getAlgorithmName() {
			return algorithmName;
		}

		public void setAlgorithmName(String algorithmName) {
			this.algorithmName = algorithmName;
		}

		public String getEnvironmentName() {
			return environmentName;
		}

		public void setEnvironmentName(String environmentName) {
			this.environmentName = environmentName;
		}

		public Integer getExecutions() {
			return executions;
		}

		public void setExecutions(Integer executions) {
			this.executions = executions;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((algorithmName == null) ? 0 : algorithmName.hashCode());
			result = prime * result
					+ ((attrSelector == null) ? 0 : attrSelector.hashCode());
			result = prime * result
					+ ((contextNode == null) ? 0 : contextNode.hashCode());
			result = prime
					* result
					+ ((environmentName == null) ? 0 : environmentName
							.hashCode());
			result = prime * result
					+ ((executions == null) ? 0 : executions.hashCode());
			result = prime * result
					+ ((pruneMetrics == null) ? 0 : pruneMetrics.hashCode());
			result = prime
					* result
					+ ((stringTransformations == null) ? 0
							: stringTransformations.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExperimentRow other = (ExperimentRow) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (algorithmName == null) {
				if (other.algorithmName != null)
					return false;
			} else if (!algorithmName.equals(other.algorithmName))
				return false;
			if (attrSelector == null) {
				if (other.attrSelector != null)
					return false;
			} else if (!attrSelector.equals(other.attrSelector))
				return false;
			if (contextNode == null) {
				if (other.contextNode != null)
					return false;
			} else if (!contextNode.equals(other.contextNode))
				return false;
			if (environmentName == null) {
				if (other.environmentName != null)
					return false;
			} else if (!environmentName.equals(other.environmentName))
				return false;
			if (executions == null) {
				if (other.executions != null)
					return false;
			} else if (!executions.equals(other.executions))
				return false;
			if (pruneMetrics == null) {
				if (other.pruneMetrics != null)
					return false;
			} else if (!pruneMetrics.equals(other.pruneMetrics))
				return false;
			if (stringTransformations == null) {
				if (other.stringTransformations != null)
					return false;
			} else if (!stringTransformations
					.equals(other.stringTransformations))
				return false;
			return true;
		}

		private ExperimentsController getOuterType() {
			return ExperimentsController.this;
		}
		
		
		
		
	}

}

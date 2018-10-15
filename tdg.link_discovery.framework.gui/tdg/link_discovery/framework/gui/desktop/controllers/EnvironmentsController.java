package tdg.link_discovery.framework.gui.desktop.controllers;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;

import tdg.link_discovery.framework.gui.desktop.GuiConfiguration;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.objects.Tuple;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class EnvironmentsController {


    @FXML
    private TableView<EnvironmentRow> environmentsTable;


    @FXML
    private AnchorPane environmentsPanel;
    
    
    @FXML
    private Button newEnvironmentButton;

    @FXML
    private TextField environmentSearchBar;

    @FXML
    private Button removeEnvironmentButton;

    @FXML
    private CheckBox showDatasetsCheckbox;

    @FXML
    private CheckBox showGoldStdCheckbox;

    @FXML
    private CheckBox showExamplesCheckbox;

    @FXML
    private CheckBox showAttributesCheckbox;

    @FXML
    private CheckBox showRestrictionsCheckbox;

    @FXML
    private CheckBox showOutputsCheckbox;

    /*
     * Columns
     */
    @FXML
    private TableColumn<EnvironmentRow, String> nameColumn;

    @FXML
    private TableColumn<EnvironmentRow, String> sourceColumn;

    @FXML   
    private TableColumn<EnvironmentRow, String> targetColumn;

    @FXML
    private TableColumn<EnvironmentRow, String> goldStdColumn;

    @FXML
    private TableColumn<EnvironmentRow, String> examplesColumn;

    @FXML
    private TableColumn<EnvironmentRow, String> attributeColumns;
    
    @FXML
    private TableColumn<EnvironmentRow, String> attributesSourceColumn;

    @FXML
    private TableColumn<EnvironmentRow, String> attributesTargetColumn;

    @FXML  
    private TableColumn<EnvironmentRow, String> restrictionsColumn;

    @FXML  
    private TableColumn<EnvironmentRow, String> restrictionsSourceColumn;

    @FXML
    private TableColumn<EnvironmentRow, String> restrictionsTargetColumn;

    @FXML
    private TableColumn<EnvironmentRow, String> outputsColumn;

    @FXML
    private TableColumn<EnvironmentRow, String> outputsStatsColumn;

    @FXML   
    private TableColumn<EnvironmentRow, String> outputsSpecificationsColumn;

    @FXML
    private TableColumn<EnvironmentRow, String> outputsLinksColumn;

    private Boolean showDatasets, showGoldStd, showExamples, showAttributes, showRestrictions, showOutputs;
 
   // ----------
   

	@FXML
	public void initialize() {
		showDatasets=true;
		showGoldStd=true;
		showExamples=true;
		showAttributes=true;
		showRestrictions=true;
		showOutputs=true;
		// initi table columns
		 initTableColumns();
		//Init table data
		List<EnvironmentRow> rows = Lists.newArrayList();
		MainController.environmentConnector.getListOfStoredEnvironments().forEach(name -> rows.add(getEnvironmentObjectsFromFile(name)));
		
		environmentsTable.getItems().addAll(rows);
		
	}
	
	
    @FXML
    void newEnvironmentClicked(MouseEvent event) {
    	//TODO
    }

    @FXML
    void removeEnvironmentClicked(MouseEvent event) {
    	//TODO
    }
   
	
	private void initTableColumns(){

		nameColumn.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("nameColumn"));
		sourceColumn.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("sourceColumn"));
		targetColumn.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("targetColumn"));
		goldStdColumn.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("goldStdColumn"));
		examplesColumn.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("examplesColumn"));
		//attributeColumns.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("attributeColumns"));
		attributesSourceColumn.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("attributesSourceColumn"));
		attributesTargetColumn.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("attributesTargetColumn"));
		//restrictionsColumn.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("restrictionsColumn"));
		restrictionsSourceColumn.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("restrictionsSourceColumn"));
		restrictionsTargetColumn.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("restrictionsTargetColumn"));
		//outputsColumn.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("outputsColumn"));
		outputsStatsColumn.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("outputsStatsColumn"));
		outputsSpecificationsColumn.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("outputsSpecificationsColumn"));
		outputsLinksColumn.setCellValueFactory(new PropertyValueFactory<EnvironmentRow,String>("outputsLinksColumn"));

			
	}
	
	private EnvironmentRow getEnvironmentObjectsFromFile(String file){
		String filePath = GuiConfiguration.ENVIRONMENTS_DIRECTORY+""+file+".cnf";
		EnvironmentRow environmentObject = null;
		try{
			List<String> lines = FileUtils.readLines(new File(filePath), "UTF-8");
			List<Tuple<String,String>> values = lines.stream().filter(line-> !line.isEmpty() && !line.startsWith("#"))
						  .map(line -> transformLineToTuple(line))
						  .collect(Collectors.toList());
			
			environmentObject = createEnvironmentObject(values, file);
		}catch( Exception e){
			e.printStackTrace();
		}
		return environmentObject;
	}
	
	private EnvironmentRow createEnvironmentObject(List<Tuple<String, String>> values, String name) {
		String nameColumn = name;
		String sourceColumn = initValueFromList(values, FrameworkConfiguration.ENVIRONMENT_SOURCEDATASET_INFILE_TOKEN);
		String targetColumn = initValueFromList(values, FrameworkConfiguration.ENVIRONMENT_TARGETDATASET_INFILE_TOKEN);
		String goldStdColumn = initValueFromList(values, FrameworkConfiguration.ENVIRONMENT_GOLD_STANDARD_INFILE_TOKEN);
		String examplesColumn = initValueFromList(values, FrameworkConfiguration.ENVIRONMENT_EXAMPLESFILE_INFILE_TOKEN);
		String attributesSourceColumn = initValueFromList(values, FrameworkConfiguration.ENVIRONMENT_SUITABLE_ATTRIBUTES_INFILE_TOKEN);
		String attributesTargetColumn = initValueFromList(values, FrameworkConfiguration.ENVIRONMENT_SUITABLE_ATTRIBUTES_INFILE_TOKEN);
		String restrictionsSourceColumn = initValueFromList(values, FrameworkConfiguration.ENVIRONMENT_SOURCE_RESTRICTIONS_INFILE_TOKEN);
		String restrictionsTargetColumn = initValueFromList(values, FrameworkConfiguration.ENVIRONMENT_TARGET_RESTRICTIONS_INFILE_TOKEN);
		String outputsStatsColumn = initValueFromList(values, FrameworkConfiguration.ENVIRONMENT_ALGORITHM_STATISTICS_INFILE_TOKEN);
		String outputsSpecificationsColumn = initValueFromList(values, FrameworkConfiguration.ENVIRONMENT_SPECIFICATIONS_OUTPUT_FILE_INFILE_TOKEN);
		String outputsLinksColumn = initValueFromList(values, FrameworkConfiguration.ENVIRONMENT_LINKS_OUTPUT_FILE_INFILE_TOKEN);
		EnvironmentRow newRow = new EnvironmentRow(nameColumn, sourceColumn, targetColumn, goldStdColumn, examplesColumn, attributesSourceColumn, attributesTargetColumn, restrictionsSourceColumn, restrictionsTargetColumn, outputsStatsColumn, outputsSpecificationsColumn, outputsLinksColumn);
		return newRow;
	}
	
	public String initValueFromList(List<Tuple<String,String>> tuples, String key){
		String result = "not specified";
		List<String> values = tuples.stream().filter(tuple -> tuple.getFirstElement().equals(key)).map(tuple -> tuple.getSecondElement()).collect(Collectors.toList());
		if(!values.isEmpty()){
			StringBuffer resultTmp = new StringBuffer();
			if(values.size()>1)
				values.stream().forEach(value -> resultTmp.append(value).append("\n"));
			if(values.size()==1)
				values.stream().forEach(value -> resultTmp.append(value));
			result = resultTmp.toString();
		}
		return result.toString();
	}

	private Tuple<String,String> transformLineToTuple(String line){
		if(line.isEmpty() || line.startsWith("#"))
			return null;
		return new Tuple<String,String>(line.split(" := ")[0].trim(),line.split(" := ")[1].trim());
	}
    
    @FXML
    void environmentSearchTyped(KeyEvent event) {
    	disableEditionButtons();
		
    	StringBuffer str = new StringBuffer(environmentSearchBar.getText());
    	str.append(event.getCharacter());
    	String filterString = str.toString();
    	System.out.println(filterString);
    	
    	//Init table data
    	List<EnvironmentRow> rows = Lists.newArrayList();
    	MainController.environmentConnector.getListOfStoredEnvironments().stream()
			.filter(elem -> areSimilarStrings(elem, filterString))
			.forEach(name -> rows.add(getEnvironmentObjectsFromFile(name)));
    	environmentsTable.getItems().clear();
    	environmentsTable.getItems().addAll(rows);
    	
    	
	}

	private Boolean areSimilarStrings(String element, String filter) {
		Boolean filterString = true;
		if (!element.contains(filter))
			filterString = false;

		return filterString;
	}


    @FXML
    void anchorPanelEnvironmentClicked(MouseEvent event) {
    	disableEditionButtons();
    }
    
    
    private void disableEditionButtons(){
    	environmentsTable.getSelectionModel().clearSelection();
    	removeEnvironmentButton.setDisable(true);
    }
   
    @FXML
    void showDatasetsClicked(MouseEvent event) {
    	if(showDatasets){
	    	sourceColumn.setVisible(false);  
	    	targetColumn.setVisible(false); 
	    	showDatasets = false;
    	}else{
    		sourceColumn.setVisible(true);  
	    	targetColumn.setVisible(true); 
	    	showDatasets = true;
    	}
    }

    @FXML
    void showExamplesClicked(MouseEvent event) {
    	if(showExamples){
    		examplesColumn.setVisible(false);
    		showExamples = false;
    	}else{
    		examplesColumn.setVisible(true);
    		showExamples = true;
    	}
    }

    @FXML
    void showGoldStdClicked(MouseEvent event) {
    	if(showGoldStd){
    		goldStdColumn.setVisible(false);
    		showGoldStd = false;
    	}else{
    		goldStdColumn.setVisible(true);
    		showGoldStd = true;
    	}
    }

    @FXML
    void showOutputsClicked(MouseEvent event) {
    	 if(showOutputs){
    		 outputsColumn.setVisible(false);
    		 outputsStatsColumn.setVisible(false);
    		 outputsSpecificationsColumn.setVisible(false);
    		 outputsLinksColumn.setVisible(false);
    		 showOutputs=false;
    	 }else{
    		 outputsColumn.setVisible(true);
    		 outputsStatsColumn.setVisible(true);
    		 outputsSpecificationsColumn.setVisible(true);
    		 outputsLinksColumn.setVisible(true);
    		 showOutputs=true;
    	 }
    }

    @FXML
    void showRestrictionsClicked(MouseEvent event) {
    	if(showRestrictions){
    		restrictionsColumn.setVisible(false);
    		restrictionsSourceColumn.setVisible(false);
    		restrictionsTargetColumn.setVisible(false);
    		showRestrictions=false;
    	}else{
    		restrictionsColumn.setVisible(true);
    		restrictionsSourceColumn.setVisible(true);
    		restrictionsTargetColumn.setVisible(true);
    		showRestrictions=true;
    	}
    }

	@FXML
	void showattributesClicked(MouseEvent event) {
		if (showAttributes) {
			attributeColumns.setVisible(false);
			attributesSourceColumn.setVisible(false);
			attributesTargetColumn.setVisible(false);
			showAttributes = false;
		} else {
			attributeColumns.setVisible(false);
			attributesSourceColumn.setVisible(false);
			attributesTargetColumn.setVisible(false);
			showAttributes = true;
		}
	}
    
    
    
    
    public class EnvironmentRow{
    	private String nameColumn;
    	private String sourceColumn;
    	private String targetColumn;
    	private String goldStdColumn;
    	private String examplesColumn;

    	private String attributesSourceColumn;
    	private String attributesTargetColumn;

    	private String restrictionsSourceColumn;
    	private String restrictionsTargetColumn;

    	private String outputsStatsColumn;
    	private String outputsSpecificationsColumn;
    	private String outputsLinksColumn;
		public EnvironmentRow(String nameColumn, String sourceColumn,
				String targetColumn, String goldStdColumn,
				String examplesColumn, String attributesSourceColumn,
				String attributesTargetColumn, String restrictionsSourceColumn,
				String restrictionsTargetColumn, String outputsStatsColumn,
				String outputsSpecificationsColumn, String outputsLinksColumn) {
			super();
			this.nameColumn = nameColumn;
			this.sourceColumn = sourceColumn;
			this.targetColumn = targetColumn;
			this.goldStdColumn = goldStdColumn;
			this.examplesColumn = examplesColumn;
			this.attributesSourceColumn = attributesSourceColumn;
			this.attributesTargetColumn = attributesTargetColumn;
			this.restrictionsSourceColumn = restrictionsSourceColumn;
			this.restrictionsTargetColumn = restrictionsTargetColumn;
			this.outputsStatsColumn = outputsStatsColumn;
			this.outputsSpecificationsColumn = outputsSpecificationsColumn;
			this.outputsLinksColumn = outputsLinksColumn;
		}
		public String getNameColumn() {
			return nameColumn;
		}
		public void setNameColumn(String nameColumn) {
			this.nameColumn = nameColumn;
		}
		public String getSourceColumn() {
			return sourceColumn;
		}
		public void setSourceColumn(String sourceColumn) {
			this.sourceColumn = sourceColumn;
		}
		public String getTargetColumn() {
			return targetColumn;
		}
		public void setTargetColumn(String targetColumn) {
			this.targetColumn = targetColumn;
		}
		public String getGoldStdColumn() {
			return goldStdColumn;
		}
		public void setGoldStdColumn(String goldStdColumn) {
			this.goldStdColumn = goldStdColumn;
		}
		public String getExamplesColumn() {
			return examplesColumn;
		}
		public void setExamplesColumn(String examplesColumn) {
			this.examplesColumn = examplesColumn;
		}
		public String getAttributesSourceColumn() {
			return attributesSourceColumn;
		}
		public void setAttributesSourceColumn(String attributesSourceColumn) {
			this.attributesSourceColumn = attributesSourceColumn;
		}
		public String getAttributesTargetColumn() {
			return attributesTargetColumn;
		}
		public void setAttributesTargetColumn(String attributesTargetColumn) {
			this.attributesTargetColumn = attributesTargetColumn;
		}
		public String getRestrictionsSourceColumn() {
			return restrictionsSourceColumn;
		}
		public void setRestrictionsSourceColumn(String restrictionsSourceColumn) {
			this.restrictionsSourceColumn = restrictionsSourceColumn;
		}
		public String getRestrictionsTargetColumn() {
			return restrictionsTargetColumn;
		}
		public void setRestrictionsTargetColumn(String restrictionsTargetColumn) {
			this.restrictionsTargetColumn = restrictionsTargetColumn;
		}
		public String getOutputsStatsColumn() {
			return outputsStatsColumn;
		}
		public void setOutputsStatsColumn(String outputsStatsColumn) {
			this.outputsStatsColumn = outputsStatsColumn;
		}
		public String getOutputsSpecificationsColumn() {
			return outputsSpecificationsColumn;
		}
		public void setOutputsSpecificationsColumn(String outputsSpecificationsColumn) {
			this.outputsSpecificationsColumn = outputsSpecificationsColumn;
		}
		public String getOutputsLinksColumn() {
			return outputsLinksColumn;
		}
		public void setOutputsLinksColumn(String outputsLinksColumn) {
			this.outputsLinksColumn = outputsLinksColumn;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime
					* result
					+ ((attributesSourceColumn == null) ? 0
							: attributesSourceColumn.hashCode());
			result = prime
					* result
					+ ((attributesTargetColumn == null) ? 0
							: attributesTargetColumn.hashCode());
			result = prime
					* result
					+ ((examplesColumn == null) ? 0 : examplesColumn.hashCode());
			result = prime * result
					+ ((goldStdColumn == null) ? 0 : goldStdColumn.hashCode());
			result = prime * result
					+ ((nameColumn == null) ? 0 : nameColumn.hashCode());
			result = prime
					* result
					+ ((outputsLinksColumn == null) ? 0 : outputsLinksColumn
							.hashCode());
			result = prime
					* result
					+ ((outputsSpecificationsColumn == null) ? 0
							: outputsSpecificationsColumn.hashCode());
			result = prime
					* result
					+ ((outputsStatsColumn == null) ? 0 : outputsStatsColumn
							.hashCode());
			result = prime
					* result
					+ ((restrictionsSourceColumn == null) ? 0
							: restrictionsSourceColumn.hashCode());
			result = prime
					* result
					+ ((restrictionsTargetColumn == null) ? 0
							: restrictionsTargetColumn.hashCode());
			result = prime * result
					+ ((sourceColumn == null) ? 0 : sourceColumn.hashCode());
			result = prime * result
					+ ((targetColumn == null) ? 0 : targetColumn.hashCode());
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
			EnvironmentRow other = (EnvironmentRow) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (attributesSourceColumn == null) {
				if (other.attributesSourceColumn != null)
					return false;
			} else if (!attributesSourceColumn
					.equals(other.attributesSourceColumn))
				return false;
			if (attributesTargetColumn == null) {
				if (other.attributesTargetColumn != null)
					return false;
			} else if (!attributesTargetColumn
					.equals(other.attributesTargetColumn))
				return false;
			if (examplesColumn == null) {
				if (other.examplesColumn != null)
					return false;
			} else if (!examplesColumn.equals(other.examplesColumn))
				return false;
			if (goldStdColumn == null) {
				if (other.goldStdColumn != null)
					return false;
			} else if (!goldStdColumn.equals(other.goldStdColumn))
				return false;
			if (nameColumn == null) {
				if (other.nameColumn != null)
					return false;
			} else if (!nameColumn.equals(other.nameColumn))
				return false;
			if (outputsLinksColumn == null) {
				if (other.outputsLinksColumn != null)
					return false;
			} else if (!outputsLinksColumn.equals(other.outputsLinksColumn))
				return false;
			if (outputsSpecificationsColumn == null) {
				if (other.outputsSpecificationsColumn != null)
					return false;
			} else if (!outputsSpecificationsColumn
					.equals(other.outputsSpecificationsColumn))
				return false;
			if (outputsStatsColumn == null) {
				if (other.outputsStatsColumn != null)
					return false;
			} else if (!outputsStatsColumn.equals(other.outputsStatsColumn))
				return false;
			if (restrictionsSourceColumn == null) {
				if (other.restrictionsSourceColumn != null)
					return false;
			} else if (!restrictionsSourceColumn
					.equals(other.restrictionsSourceColumn))
				return false;
			if (restrictionsTargetColumn == null) {
				if (other.restrictionsTargetColumn != null)
					return false;
			} else if (!restrictionsTargetColumn
					.equals(other.restrictionsTargetColumn))
				return false;
			if (sourceColumn == null) {
				if (other.sourceColumn != null)
					return false;
			} else if (!sourceColumn.equals(other.sourceColumn))
				return false;
			if (targetColumn == null) {
				if (other.targetColumn != null)
					return false;
			} else if (!targetColumn.equals(other.targetColumn))
				return false;
			return true;
		}
		private EnvironmentsController getOuterType() {
			return EnvironmentsController.this;
		}
		@Override
		public String toString() {
			return "EnvironmentRow [nameColumn=" + nameColumn
					+ ", sourceColumn=" + sourceColumn + ", targetColumn="
					+ targetColumn + ", goldStdColumn=" + goldStdColumn
					+ ", examplesColumn=" + examplesColumn
					+ ", attributesSourceColumn=" + attributesSourceColumn
					+ ", attributesTargetColumn=" + attributesTargetColumn
					+ ", restrictionsSourceColumn=" + restrictionsSourceColumn
					+ ", restrictionsTargetColumn=" + restrictionsTargetColumn
					+ ", outputsStatsColumn=" + outputsStatsColumn
					+ ", outputsSpecificationsColumn="
					+ outputsSpecificationsColumn + ", outputsLinksColumn="
					+ outputsLinksColumn + "]";
		}
    	
    	
    }
}

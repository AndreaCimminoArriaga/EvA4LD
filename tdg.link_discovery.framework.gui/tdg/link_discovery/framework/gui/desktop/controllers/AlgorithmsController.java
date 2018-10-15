package tdg.link_discovery.framework.gui.desktop.controllers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.WindowEvent;

import org.apache.jena.ext.com.google.common.collect.Lists;

import tdg.link_discovery.framework.gui.desktop.GuiConfiguration;
import tdg.link_discovery.middleware.objects.Tuple;

public class AlgorithmsController {

	@FXML
	private ListView<String> algorithmList;
	
	@FXML
	private TextField searchAlgorithmBar;

	@FXML
	private Button addAlgorithmButton;

	@FXML
	private Button removeAlgorithmButton;

	@FXML
	private TableView<Tuple<String, String>> algorithmParametersTable;

	@FXML
	private TableColumn<Tuple<String, String>, String> columnAlgorithmValues;

	@FXML
	private TableColumn<Tuple<String, String>, String> columnAlgorithmParameters;

	@FXML
	private AnchorPane algorithmsAnchorPanel;

	/*
	 * Initialize table so it can be editable, handle its edition
	 */

	@FXML
	public void initialize() {
		// Read list of available algorithms
		 List<String> algorithmsListString = MainController.algorithmConnector.getListOfStoredAlgorithmsNames();
		algorithmsListString.stream().forEach( algorithm -> algorithmList.getItems().add(algorithm));
		// Initialize the table with an empty row
		algorithmParametersTable.setPlaceholder(new Label("Empty"));
		List<Tuple<String,String>> algorithmsSetup = Lists.newArrayList();
		// Set table columns factories to be modified
		columnAlgorithmParameters.setCellValueFactory(new PropertyValueFactory<>("firstElement"));
		columnAlgorithmValues.setCellValueFactory(new PropertyValueFactory<>("secondElement"));
		columnAlgorithmValues.setCellFactory(TextFieldTableCell.forTableColumn());
		columnAlgorithmValues.setOnEditCommit((
						TableColumn.CellEditEvent<Tuple<String, String>, String> t) -> onValueCellModification(t));

		algorithmParametersTable.getItems().setAll(algorithmsSetup);
		algorithmParametersTable.setEditable(false);

	}

	private void onValueCellModification(TableColumn.CellEditEvent<Tuple<String, String>, String> t) {
		Tuple<String, String> selectedTuple = t.getTableView().getItems().get(t.getTablePosition().getRow());
		String oldLabel = selectedTuple.getFirstElement();
		String newValue = t.getNewValue();
		// TODO: check newValue is correct
		selectedTuple.setSecondElement(newValue);
		System.out.println("Persist Changes in file");
		persistChangesInParametersFile(selectedTuple, oldLabel);
	}

	private void persistChangesInParametersFile(Tuple<String, String> newValue,
			String oldLabel) {
		// Retrieve setup file
		List<String> newLines = Lists.newArrayList();
		String algorithmSetupFullPath = getSelectedAlgorithmSetupFile();
		//Boolean matched = false; // in case is a new label
		StringBuffer newLine = new StringBuffer();
		newLine.append(newValue.getFirstElement()).append(" := ")
				.append(newValue.getSecondElement());
		// Rewrite the setupfile
		try {
			for (String line : Files.readAllLines(Paths
					.get(algorithmSetupFullPath))) {
				if (line.contains(oldLabel)) {
					//matched = true;
					newLines.add(newLine.toString());
					System.out.println("lineModified");
				} else {
					newLines.add(line);
				}
			}
			// if(!matched)
			// TODO: parameter is not in the original parameters

			Files.write(Paths.get(algorithmSetupFullPath), newLines,
					StandardCharsets.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Algorithms actions once are selected: add, remove, edit, filter
	 */

	@FXML
	void addAlgorithmClicked(MouseEvent event) {
		try {
			MainController.addNewAlgorithmStage = MainController.createNewStage("New Dataset", "views/addEditAlgorithmGui.fxml");
			MainController.addNewAlgorithmStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
						@Override
						public void handle(WindowEvent we) {
							
							if(algorithmList!=null){
								// Update list of available algorithms
						    	List<String> algorithmsListString = MainController.algorithmConnector.getListOfStoredAlgorithmsNames();
						    	algorithmList.getItems().clear();
						    	algorithmsListString.stream().forEach( algorithm -> algorithmList.getItems().add(algorithm));
						    	
								MainController.addNewAlgorithmStage.close();
							}
						}
					});
			
			MainController.addNewAlgorithmStage.show();
    	       
    		} catch(Exception e) {
               e.printStackTrace();
            }
    	event.consume();
	}
	
	

	@FXML
	void removeAlgorithmButton(MouseEvent event) {
		// Retrieve in-memory selections
		ObservableList<String> selectedAlgorithms = algorithmList.getSelectionModel().getSelectedItems();
		
    	 // remove
    	selectedAlgorithms.forEach(algorithmName -> MainController.algorithmConnector.removeAlgorithm(algorithmName));			
    	disableModificationButtons();
    	
    	// Update table of params
    	unfillParamsTable(event);
    	algorithmList.getSelectionModel().clearSelection();
    	
    	// Update list of available algorithms
    	List<String> algorithmsListString = MainController.algorithmConnector.getListOfStoredAlgorithmsNames();
    	algorithmList.getItems().clear();
    	algorithmsListString.stream().forEach( algorithm -> algorithmList.getItems().add(algorithm));
    	event.consume();
	}

	@FXML
	void searchAlgorithm(KeyEvent event) {
		disableModificationButtons();
		
    	StringBuffer str = new StringBuffer(searchAlgorithmBar.getText());
    	str.append(event.getCharacter());
    	String filterString = str.toString();
    	List<String> algorithmNameList = MainController.algorithmConnector.getListOfStoredAlgorithmsNames();
    	
    	
    	List<String> toRemove = algorithmNameList.stream().filter(elem -> !areSimilarStrings(elem, filterString)).collect(Collectors.toList());
    	algorithmNameList.stream().forEach(algorithnName -> {
    		if(!algorithmList.getItems().contains(algorithnName))
    			algorithmList.getItems().add(algorithnName);
    	});
    	toRemove.stream().forEach(datasetName -> algorithmList.getItems().remove(datasetName));
	}

	private Boolean areSimilarStrings(String element, String filter) {
		Boolean filterString = true;
		if (!element.contains(filter))
			filterString = false;

		return filterString;
	}

	@FXML
	void editSelectedAlgorithm(MouseEvent event) {

	}

	/*
	 * Algorithms list actions when one is selected from the list
	 */

	// Select an algorithm
	@FXML
	void algorithmSelected(MouseEvent event) {
		if(algorithmList.getSelectionModel().getSelectedItem()!=null){
			// Load parameters in table
			String algorithmSetupFullPath = getSelectedAlgorithmSetupFile();
			initializeTableFromFile(algorithmSetupFullPath);
			// Enable modification buttons
			enableModificationButtons();
			// Enable table modification
			algorithmParametersTable.setEditable(true);
		}
	}

	private String getSelectedAlgorithmSetupFile() {
		String algorithmSetupFile = algorithmList.getSelectionModel().getSelectedItem().concat("_setup.cnf");
		
		String algorithmSetupFullPath = GuiConfiguration.ALGORTIHMS_DIRECTORY
				+ "" + algorithmSetupFile;
		return algorithmSetupFullPath;
	}

	// Deselect algorithm
	@FXML
	void unfillParamsTable(MouseEvent event) {
		// Deselect table params
		List<Tuple<String, String>> algorithmsSetup = Lists.newArrayList();
		algorithmsSetup.add(new Tuple<String, String>("", ""));
		algorithmParametersTable.getItems().setAll(algorithmsSetup);
		// Disable modification buttons
		disableModificationButtons();
		// Disable parameters modification
		algorithmParametersTable.setEditable(false);
	}

	private void enableModificationButtons() {
		removeAlgorithmButton.disableProperty().set(false);
	}

	private void disableModificationButtons() {
		removeAlgorithmButton.disableProperty().set(true);
	}

	

	private void initializeTableFromFile(String file) {
		List<Tuple<String, String>> algorithmsSetup = readFileContent(file);
		algorithmParametersTable.getItems().setAll(algorithmsSetup);
	}

	private List<Tuple<String, String>> readFileContent(String file) {
		List<Tuple<String, String>> params = Lists.newArrayList();
		try {
			FileInputStream fstream = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				Tuple<String, String> processedLine = processLine(strLine);
				if (processedLine != null)
					params.add(processedLine);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return params;
	}

	private Tuple<String, String> processLine(String line) {
		Tuple<String, String> tuple = null;
		if (!line.startsWith("#") && !line.isEmpty()) {
			String[] args = line.split(":=");
			tuple = new Tuple<String, String>(args[0].trim(), args[1].trim());
		}
		return tuple;
	}
}

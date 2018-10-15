package tdg.link_discovery.framework.gui.desktop.controllers;

import tdg.link_discovery.framework.gui.connector.FileAlgorithmsConnector;
import tdg.link_discovery.framework.gui.connector.FileDatasetsConnector;
import tdg.link_discovery.framework.gui.connector.FileEnvironmentConnector;
import tdg.link_discovery.framework.gui.connector.IAlgorithmsConnector;
import tdg.link_discovery.framework.gui.connector.IDatasetsConnector;
import tdg.link_discovery.framework.gui.connector.IEnvironmentsConnector;
import tdg.link_discovery.framework.gui.desktop.Main;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainController {

	/*
	 * Connector to the rest of the framework
	 */
	public static IDatasetsConnector datasetsConnector;
	public static IEnvironmentsConnector environmentConnector;
	public static IAlgorithmsConnector algorithmConnector;
	/*
	 * Controllers
	 */
	
	@FXML
	private DatasetsController datasetsController;

	@FXML
	private AlgorithmsController algorithmsParametersController;
	
	@FXML
	private EnvironmentsController environmentsController;

	@FXML
	private ExperimentsController experimentsController;
	
	/*
	 * Menu buttons
	 */
	
	// New
    @FXML
    private MenuItem datasetsNewButton;

    @FXML
    private MenuItem algorithmsNewButton;
    
    @FXML
    private MenuItem environmentsNewButton;
    
  
    
    // Edit
    
    @FXML
    private MenuItem datasetsEditButton;
    
    @FXML
    private MenuItem algorithmsEditButton;
    
    @FXML
    private MenuItem environmentsEditButton;
    
    //Import

    @FXML
    private MenuItem importTDBButton;

    @FXML
    private MenuItem importAlgorithmButton;

    @FXML
    private MenuItem importEnvironmentButton;

    // Other buttons
    
    @FXML
    private MenuItem exitButton;

    @FXML
    private MenuItem aboutButton;

    @FXML
    private MenuItem contactButton;

    @FXML
    private MenuItem helpButton;
    
	/*
	 *	GUI auxiliar stages 
	 */
	    
   
    public static Boolean loadingDataset = false; // required to allow loading cancel button
    public static Stage addNewDatasetStage;
    public static Stage addNewAlgorithmStage;
    public static Stage addNewEnvironmentStage;
   

    public static Stage datasetsManagerStage;
    public static Stage algorithmsManagerStage;
    public static Stage environmentsManagerStage;
    public static Stage experimentsManagerStage;
   
    public static Stage tdbDatasetsImportStage;
    public static Stage algorithmsImportStage;
    public static Stage environemntsImportStage;
    
    public static Stage aboutStage;
    public static Stage contactStage;
    public static Stage helpStage;
    
    public static Stage splashScreenStage;

    public MainController(){
    	datasetsConnector = new FileDatasetsConnector();
    	algorithmConnector = new FileAlgorithmsConnector();
    	environmentConnector = new FileEnvironmentConnector();
    }
    
    
    
    @FXML
    void datasetsNewButtonClicked(ActionEvent event) {
    	openDatasetsMangaerView(null);
    	datasetsManagerStage.hide();	// Required to load current datasets in the system, closed when loading is done
    	addNewDatasetStage = createNewStage("New Dataset", "views/addDatasetGui.fxml");
    	addNewDatasetStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
			public void handle(WindowEvent we) {
            	addNewDatasetStage.close();
            }});   
    	event.consume();
    }
    
    
    @FXML
    void openDatasetsMangaerView(ActionEvent event) {
    	datasetsManagerStage = createNewStage("Datasets Manager", "views/datasetsTab.fxml");
    	datasetsManagerStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
			public void handle(WindowEvent we) {
            	datasetsManagerStage.close();
            }}); 
    	if(event!=null)
    		event.consume();
    }
    
    @FXML
    void openAlgorithmsMangaerView(ActionEvent event) {
    	algorithmsManagerStage = createNewStage("Algorithms Manager","views/algorithmsTab.fxml");
    	algorithmsManagerStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
			public void handle(WindowEvent we) {
            	algorithmsManagerStage.close();
            }});   
    	event.consume();
    }

    @FXML
    void algorithmsNewButtonClicked(ActionEvent event) {
    	addNewAlgorithmStage = createNewStage("New Algorithm","views/addEditAlgorithmGui.fxml");
    	addNewAlgorithmStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
			public void handle(WindowEvent we) {
            	addNewAlgorithmStage.close();
            }});   
    	event.consume();
    }
  
  
    @FXML
    void environmentsNewButtonClicked(ActionEvent event) {

    }
    
    @FXML
    void environmentsEditClicked(ActionEvent event) {
    	environmentsManagerStage = createNewStage("Environments Manager","views/environmentsTab.fxml");
    	environmentsManagerStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
			public void handle(WindowEvent we) {
            	environmentsManagerStage.close();
            }});   
    	event.consume();
    }

 



 

    @FXML
    void importAlgorithmClicked(ActionEvent event) {

    }

    @FXML
    void importEnvironmentClicked(ActionEvent event) {

    }

    @FXML
    void importTDBCliked(ActionEvent event) {

    }
    
    /*
     * Non-important stage openers
     */

    @FXML
    void aboutButtonClicked(ActionEvent event) {

    }

    @FXML
    void contactButtonClicked(ActionEvent event) {

    }
    
    @FXML
    void exitButtonClicked(ActionEvent event) {

    }

    @FXML
    void helpButtonClicked(ActionEvent event) {

    }
 
	
	/*
	 * Usefull methods
	 */
	public static Stage createNewStage(String title, String fxmlFile) {
		Stage newStage = null;
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(
					Main.class.getResource(fxmlFile));
			Parent root1 = (Parent) fxmlLoader.load();
			newStage = new Stage();
			newStage.setTitle(title);
			newStage.setScene(new Scene(root1));
			newStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newStage;
	}

}

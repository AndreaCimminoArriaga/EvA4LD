package tdg.link_discovery.framework.gui.desktop;

import java.sql.Connection;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import tdb.link_discovery.framework.gui.persistence.GuiPersistence;

public class Main extends Application{

	public static Scene scene;
	public static Boolean opened = false;

	
	public static void main(String[] args) {
		if(!opened){
			
			opened = true;
			launch(args);
		}
    }
	
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Main.class.getResource("views/desktopGui.fxml"));
        
        Scene scene = new Scene(root);
        //stage.getIcons().add(new Image("icon.jpg"));
        stage.setTitle("Scindere Framework");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
			public void handle(WindowEvent we) {
            		stage.close();
            }
        });   
    }
    
  
}

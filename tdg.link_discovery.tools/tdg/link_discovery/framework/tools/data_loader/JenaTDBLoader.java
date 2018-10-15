package tdg.link_discovery.framework.tools.data_loader;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.TDBLoader;

public class JenaTDBLoader implements ILoader{

	
	
	@Override
	public Boolean loadDataFromFile(String datasetName, String fileFullPath) {
		Boolean succesfullyLoaded = true;
		try{
			Dataset dataset = TDBFactory.createDataset(datasetName);
			dataset.begin(ReadWrite.WRITE);
			TDBLoader.loadModel(dataset.getDefaultModel(),fileFullPath);
			
			dataset.commit();
			dataset.end();
			dataset.close();
		}catch(Exception e){
			e.printStackTrace();
			succesfullyLoaded = false;
		}
		return succesfullyLoaded;
		
	}

}

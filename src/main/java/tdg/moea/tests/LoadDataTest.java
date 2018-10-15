package tdg.moea.tests;

import javax.sound.sampled.LineUnavailableException;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.TDBLoader;

import com.wcohen.ss.Jaro;
import com.wcohen.ss.JaroWinkler;
import com.wcohen.ss.JaroWinklerTFIDF;
import com.wcohen.ss.SoftTFIDF;

import tdg.link_discovery.framework.tools.data_loader.ILoader;
import tdg.link_discovery.framework.tools.data_loader.JenaTDBLoader;
import tdg.link_discovery.middleware.utils.Utils;



public class LoadDataTest {

	public static void main(String[] args) throws LineUnavailableException {
		/*
		ILoader loader = new JenaTDBLoader();
		loader.loadDataFromFile("./tdb-data/bbc", "/Users/andrea/Desktop/liafar/bbc-dbpedia/BBC/bbc.nt");
		*/

		/*p
		String directory = "/Users/andrea_arriaga_5/Desktop/all-geonames/all-geonames.nt";
		Set<String> fileNames = ConverterRDFtoNT.readFilesFromDirectory(directory);
		int counter = 0;
		for(String fileName:fileNames){
			Model model = dataset.getDefaultModel();
			try{
				TDBLoader.loadModel(model,directory.concat(fileName)); 
				counter++;
				System.out.println(fileName);
			}catch(Exception e){
				System.out.println("Error with "+fileName);
				e.printStackTrace();
				
			}
		}
		System.out.println("Correctly processed: "+counter);
		*/
	
	}

}

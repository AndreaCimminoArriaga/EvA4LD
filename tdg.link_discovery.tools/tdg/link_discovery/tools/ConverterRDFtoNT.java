package tdg.link_discovery.tools;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class ConverterRDFtoNT {

	public static void main(String[] args) throws FileNotFoundException {
		//String directoryInput = "/Users/andrea_arriaga_5/Desktop/ESWC2017-experiments/telegraphis-dbpedia/telegraphis/";
		//String directoryOutput = "/Users/andrea_arriaga_5/Desktop/ESWC2017-experiments/telegraphis-dbpedia/converted/";
	
		
			String directoryInput = "/Users/andrea/Desktop/dump_files/input";
			String directoryOutput = "/Users/andrea/Desktop/dump_files/output";
			
			if(!directoryInput.endsWith("/"))
				directoryInput+="/";
			
			if(!directoryOutput.endsWith("/"))
				directoryOutput+="/";
			/*OutputStream fileOutput = new FileOutputStream("/Users/andrea_arriaga_5/Desktop/all-geonames/23150.nt");
			Model model = ModelFactory.createDefaultModel() ;
			model.read("/Users/andrea_arriaga_5/Desktop/all-geonames/23150.rdf") ;
			RDFDataMgr.write(fileOutput, model, Lang.NTRIPLES) ;*/
			
			Set<String> filesWithErrors = new HashSet<String>();
			for(String name:readFilesFromDirectory(directoryInput)){
				String fileName = directoryInput.concat(name);
				String outputName = directoryOutput.concat(name).concat(".nt");
				File file = new File(outputName);
				try {
					System.out.println("Adding: "+name);
					OutputStream fileOutput = new FileOutputStream(file);
					Model model = ModelFactory.createDefaultModel() ;
					model.read(fileName) ;
					RDFDataMgr.write(fileOutput, model, Lang.NTRIPLES) ;
					
					
				} catch (Exception e) {
					System.out.println("Error with "+fileName);
					filesWithErrors.add(fileName);
					e.printStackTrace();
				
				}
				
			}
			System.out.println("Files with errors:"+filesWithErrors);
			System.out.println("NUmber of files with errors:"+ filesWithErrors.size());
			System.out.println("Done!");
		
		
	}
	
	
	
	public static Set<String> readFilesFromDirectory(String directory){
		File folder = new File(directory);
		Set<String> filesNames = new HashSet<String>();
		File[] listOfFiles = folder.listFiles();
		
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		    	  String fileName = listOfFiles[i].getName();
		    	  if(!fileName.startsWith("."))
		    		  filesNames.add(fileName);
		      } 
		    }
		  return filesNames;
	}

}

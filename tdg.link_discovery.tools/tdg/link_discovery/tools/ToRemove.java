package tdg.link_discovery.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

public class ToRemove {

	public static Set<String> doiIris = Sets.newHashSet();
	
	public static void main(String args[]) {
		String dblpFile = "/Users/andrea/Desktop/doi/doi50/dblp.rdf";
		String doiFile = "/Users/andrea/Desktop/doi/doi50/doi50.txt";
		loadInMemoryDoiIris(doiFile);
		System.out.println("Doi iris loaded: "+doiIris.size());
		saveDBLPIris(dblpFile);
		
		
	}
	
	public static void saveDBLPIris(String file) {
		StringBuffer linesToWrite = new StringBuffer();
		int linesCounter=  0;
		try {
		// Open the file
		FileInputStream fstream = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;

		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   {
		  // Print the content on the console
		  if(!strLine.isEmpty()) {
			  String iri = strLine.replace(">", "").replace("<", "").split(" ")[0].trim();
			
			  if(doiIris.contains(iri)) {
				  System.out.println(strLine);
				  linesToWrite.append(strLine).append("\n");
				  linesCounter++;
				  if(linesCounter > 10000000) {
					  System.out.println("Writting chunk");
					  System.out.println(linesToWrite);
					  Files.write(Paths.get("/Users/andrea/Desktop/doi/doi50/dblpl3s-reduced.nt"), linesToWrite.toString().getBytes(), StandardOpenOption.WRITE);
					  linesToWrite = new StringBuffer();
					  linesCounter = 0;
				  }
			  }
		  }
		}

		if(linesCounter > 0) {
			  System.out.println("Writting chunk");
			  Files.write(Paths.get("./dblpl3s-reduced.nt"), linesToWrite.toString().getBytes(), StandardOpenOption.APPEND);
			  linesToWrite = new StringBuffer();
			  linesCounter = 0;
		  }
		
		//Close the input stream
		br.close();
		}catch(Exception e) {
			
		}
		
	
	}
	
	
	public static void loadInMemoryDoiIris(String file) {
		try {
		// Open the file
		FileInputStream fstream = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;

		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   {
		  // Print the content on the console
		  if(!strLine.isEmpty())
			  doiIris.add(strLine.trim());
		}

		//Close the input stream
		br.close();
		}catch(Exception e) {
			
		}
	}
	
	
	
}

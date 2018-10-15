package tdb.link_discovery.tools.cica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.google.common.collect.Lists;

import tdg.link_discovery.middleware.utils.StreamUtils;

public class CicaErrorRetriever {

	private static List<String> files;
	private static String fileToSearch;
	
	public static void main(String[] args) {
		String mainDir = "./";//args[0]; //directory from where to start looking for the file
		fileToSearch = "errores_tails.txt";//args[1]; // file that contains potential errors
		files = Lists.newArrayList();
		recursiveNavigation(new File(mainDir));
		checkifErrorsExist();
		
		
	}

	private static void checkifErrorsExist() {
		for(String file:files) {
			List<String> lines = Lists.newArrayList();
			try {
				if( (new File(file).exists()) ){
					FileInputStream fstream = new FileInputStream(file);
					BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
					String strLine;
					while ((strLine = br.readLine()) != null)   {
						if(!strLine.isEmpty())
							lines.add(strLine);
					}
					br.close();
				}else {
					System.out.println(file+" ... not created yet");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		
			if(lines.size()==0 || lines.isEmpty()) {
				System.out.println(file+" ... OK");
			}else {
				System.out.println(file+" ... ERROR");
			}
		}
	}
	
	
	private static void recursiveNavigation(File newFile) {
		String file = newFile.getAbsolutePath();
		if(newFile.isDirectory()) {
			
			if(file.contains("/")) {
				
				StreamUtils.asStream(newFile.listFiles()).forEach(subFile -> recursiveNavigation(subFile));
			}else {
				StreamUtils.asStream(newFile.listFiles()).forEach(subFile -> recursiveNavigation(subFile));
			}
		}
		
		if(file.endsWith(fileToSearch)) {
			files.add(file);
		}
	}
}

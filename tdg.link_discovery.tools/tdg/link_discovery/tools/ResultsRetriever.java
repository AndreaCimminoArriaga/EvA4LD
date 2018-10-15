package tdg.link_discovery.tools;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.moeaframework.util.io.FileUtils;

import com.google.common.collect.Lists;

import tdg.link_discovery.middleware.utils.FilesUtils;
import tdg.link_discovery.middleware.utils.StreamUtils;
import tdg.link_discovery.middleware.utils.Utils;

public class ResultsRetriever {

	private static List<String> files;
	
	public static void main(String[] args) {
		files = Lists.newArrayList();
		String inputDirectory = args[0];
		String outputDirecoty = args[1];
		recursiveNavigation(new File(Utils.getAbsoluteSystemPath(inputDirectory)));
		String outputDir = Utils.getAbsoluteSystemPath(outputDirecoty);
		for(String file:files) {
			String outputDirTmp = outputDir;
			String dir = file.substring(file.indexOf("results"), file.length());
			if(dir.contains("/")) {
				outputDirTmp = outputDirTmp+"/"+dir;
			}else {
				outputDirTmp = outputDirTmp+"\\"+dir;
			}
			try {
				if(outputDirTmp.contains("/")) {
					String directory = outputDirTmp.substring(0, outputDirTmp.lastIndexOf("/")+1);
					String fileName = file.substring(file.lastIndexOf("/"), file.length());
					System.out.println("");
					createDirectories(directory);
					FileUtils.copy(new File(file), new File(directory+"/"+fileName));
				}else {
					String directory = outputDirTmp.substring(0, outputDirTmp.lastIndexOf("\\")+1);
					String fileName = file.substring(file.lastIndexOf("\\"), file.length());
					createDirectories(directory);
					System.out.println(">"+directory);
					System.out.println(">*"+fileName);
					FileUtils.copy(new File(file), new File(directory+"\\"+fileName));
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void createDirectories(String directory) {
		Boolean winCharacter = false;
		if(directory.contains("\\")) {
			directory = directory.replace("\\", "/");
			winCharacter = true;
		}
		if(directory.contains("/")) {
			List<String> path = StreamUtils.asStream(directory.split("/")).collect(Collectors.toList());
			StringBuffer accumulator = new StringBuffer();
			for(String dir:path) {
				if(winCharacter) {
					accumulator.append(dir).append("\\");
				}else {
					accumulator.append(dir).append("/");
				}
				File fileAccumulator = new File(accumulator.toString());
				if(!fileAccumulator.exists())
					fileAccumulator.mkdirs();
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
		
		if(file.endsWith(".txt")) {
			files.add(file);
		}
	}

}

package tdg.link_discovery.middleware.utils;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import org.apache.jena.ext.com.google.common.collect.Lists;


public class ClassesUtils {

	public static List<String> listClassesFromPackage(String packagePath){
		List<String> clazzes = Lists.newArrayList();
		try{
			String path = "";
			if(packagePath.contains("/"))
				path =  "./"+packagePath.replace(".", "/");
			if(packagePath.contains("\\"))
				path =  ".\\"+packagePath.replace(".", "\\");
			Enumeration<URL> roots = ClassesUtils.class.getClassLoader().getResources(path);
			while(roots.hasMoreElements()){
				File root = new File(roots.nextElement().getPath());
				for (File file : root.listFiles()) {
					if (file.isDirectory()) {
					   //handle a directory
					} else {
					    String name = file.getName();
					    name = name.replace(".class","");
					    name = name.replace(".jar","");
					    clazzes.add(name);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return clazzes;
	}
	
	
	public static String findClassPackageByName(String clazzName){
		String clazzString = clazzName+".class"; // or maybe .jar
		String currentPath = ".";
		String path = recursiveFindClassByName(clazzString, currentPath);
		if(path.contains("/"))
			path=path.replace("./", "").replace("/", ".");
		if(path.contains("\\"))
			path=path.replace(".\\", "").replace("\\", ".");
		return path;
	}
	
	
	private static String recursiveFindClassByName(String clazzName, String currentPath){
		StringBuffer result = new StringBuffer();
		try{
			Enumeration<URL> roots = ClassesUtils.class.getClassLoader().getResources(currentPath);
			while(roots.hasMoreElements()){
				File root = new File(roots.nextElement().getPath());
				for (File file : root.listFiles()) {
					if (file.isDirectory()) {
						//handle a directory
						StringBuffer newPath = new StringBuffer();
						newPath.append(currentPath).append("/").append(file.getName());
						result.append(recursiveFindClassByName(clazzName, newPath.toString()));
					} else {
					    String name = file.getName();
					    if(name.equals(clazzName)){
					    	 name = name.replace(".class", "");
					    	result.append(currentPath).append("/").append(name);
					    	// this is the class we are looking for
					    }
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return result.toString();
	}
}

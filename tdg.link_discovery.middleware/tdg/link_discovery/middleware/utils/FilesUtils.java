package tdg.link_discovery.middleware.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;

public class FilesUtils {
	
	public static Boolean fileExist(String fileName, String directory){
		return true;
	}
	
	public static void deleteDirectory(File folder) {
		try {
			FileUtils.deleteDirectory(folder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void copyFolder(File src, File dest) {
		
		try {
			if (src.isDirectory()) {
				if (!dest.exists()) {
					dest.mkdir();
				}
				String files[] = src.list();
				for (String file : files) {
					File srcFile = new File(src, file);
					File destFile = new File(dest, file);
					// recursive copy
					copyFolder(srcFile, destFile);
				}
			} else {
				
				InputStream in = new FileInputStream(src);
				OutputStream out = new FileOutputStream(dest);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
				in.close();
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteDirectoryRecurively(File f) {
		try {
			if (f.isDirectory())
				StreamUtils.asStream(f.listFiles()).forEach(c -> deleteDirectoryRecurively(c));
			if (!f.delete())
				throw new FileNotFoundException("Failed to delete file: " + f);
		} catch (Exception e) {

		}
	}
	
}

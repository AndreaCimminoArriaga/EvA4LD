package tdg.link_discovery.middleware.log;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import org.apache.jena.ext.com.google.common.collect.Lists;

import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.utils.Utils;

public class Logger {

	
	private StringBuffer logLines;
	private Integer counterLines;
	private String outputFileDirectoy;
	private Integer cacheLinesSize;
	
	
	
	public Logger(String outputFileDirectoy, Integer chacheLinesSize) {
		this.outputFileDirectoy = outputFileDirectoy;
		this.cacheLinesSize = chacheLinesSize;
		this.logLines = new StringBuffer();
		this.counterLines = 0;
		refreshLog();
	}
	
	
	
	private String getCurrentTime() {
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(cal.getTime());
	}
	
	private void refreshLog() {
		logLines = new StringBuffer();
		counterLines = 0;
	}
	
	public void addLogLine(String clazz, String newLine) {
		StringBuffer log = new StringBuffer();
		log.append(getCurrentTime()).append(" clazz:").append(clazz).append(" - ").append(newLine).append("\n");
		logLines.append(log.toString());
		counterLines++;
		if(counterLines >= this.cacheLinesSize) {
			// write the whole log into file
			if(!outputFileDirectoy.isEmpty()) {
				if(!writeCurrentCachedLines()) { // in case concurrent exception was threw try once again
					try {
						Thread.sleep(500);
					} catch (Exception e) {
						e.printStackTrace();
					}
					writeCurrentCachedLines();
				}
				refreshLog();
			}
		}
	}
	
	public Boolean writeCurrentCachedLines() {
		// Check if file exists, in case directory tree does not create it
		Boolean correctlyWrited = false;
		try{
			File file = new File(outputFileDirectoy);
			if(!file.exists())
				file.createNewFile();
			if(logLines!= null && !logLines.toString().isEmpty()) {
				 Files.write(Paths.get(outputFileDirectoy), logLines.toString().getBytes(), StandardOpenOption.APPEND);
				 correctlyWrited = true;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return correctlyWrited;
	}

	public Integer getCacheLinesSize() {
		return cacheLinesSize;
	}

	public void setCacheLinesSize(Integer cacheLinesSize) {
		this.cacheLinesSize = cacheLinesSize;
	}
	
	
	

}

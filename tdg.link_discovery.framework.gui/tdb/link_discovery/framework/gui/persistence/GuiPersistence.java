package tdb.link_discovery.framework.gui.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class GuiPersistence {

	public static HikariDataSource datasource;
	
	public GuiPersistence(String guiDatabaseFile) {
		datasource = createInitHikari(guiDatabaseFile);
	}
	
	private static HikariDataSource createInitHikari(String guiDatabaseFile) {
		HikariConfig config;
		HikariDataSource datasource;
		StringBuffer urlString;
		
		if(guiDatabaseFile==null || guiDatabaseFile.isEmpty())
			throw new IllegalArgumentException("Provided h2 database file cannot be null or empty");
			
		urlString = new StringBuffer();
		urlString.append("jdbc:h2:file:").append(guiDatabaseFile).append(";MULTI_THREADED=1;CACHE_SIZE=512;");
		
		config = new HikariConfig();
		config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
		config.setConnectionTestQuery("VALUES 1");
		config.addDataSourceProperty("URL", urlString.toString());
		
		datasource = new HikariDataSource(config);
		datasource.setMaximumPoolSize(200);
		datasource.setReadOnly(false);
		datasource.setConnectionTimeout(3600000);
		
		return datasource;
	}
	
	public void initializeDefaultSchema() {
		
	}
	
	public void injectDefaultData() {
		
	}
	
}

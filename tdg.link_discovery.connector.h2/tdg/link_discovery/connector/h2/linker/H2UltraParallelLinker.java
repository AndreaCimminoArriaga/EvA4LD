package tdg.link_discovery.connector.h2.linker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdf.model.Model;
import org.h2.jdbc.JdbcSQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import tdg.link_discovery.framework.engine.evaluator.linker.ILinker;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.framework.learner.executor.LinkSpecificationExecutor;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.StreamUtils;

public class H2UltraParallelLinker implements ILinker {

	private String sourceTable;
	private String targetTable;
	private String sourceDatabase;
	private String targetDatabase;
	private Boolean oneDatabase = false;
	private CopyOnWriteArraySet<Tuple<String, String>> instancesLinkedSet;
	private static Set<Tuple<String,String>> sampleInstances;
	private String outputFile;
	
	// H2 parallelism
	private static final HikariDataSource HIKARI_POOL_CONNECTOR = createInitHikari();
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(50);
	
	
	public H2UltraParallelLinker(String sourceDatabase, String targetDatabase) {
		this.sourceTable = "";
		this.targetTable = "";
		this.outputFile = "";
		this.sourceDatabase = sourceDatabase;
		this.targetDatabase = targetDatabase;
		oneDatabase = sourceDatabase.equals(targetDatabase);
		sampleInstances = new CopyOnWriteArraySet<Tuple<String,String>>();
	}
	
	public H2UltraParallelLinker(String database) {
		this.sourceTable = "";
		this.targetTable = "";
		this.outputFile = "";
		this.sourceDatabase = database;
		this.targetDatabase = database;
		oneDatabase = true;
		sampleInstances = new CopyOnWriteArraySet<Tuple<String,String>>();
	}
	
	
	/*
	 * Link whole datasets
	 */

	@Override
	public void linkDatasets(Tuple<String, String> queries, String outputFile) {
		resgisterFunctions("CREATE ALIAS IF NOT EXISTS JaroSimilarity FOR \"tdg.link_discovery.connector.h2.procedures.Procedures.jaroSimilarity\"");
		resgisterFunctions("CREATE ALIAS IF NOT EXISTS Maximum FOR \"tdg.link_discovery.connector.h2.procedures.Procedures.max\"");
		this.outputFile = outputFile;
		instancesLinkedSet = new CopyOnWriteArraySet<Tuple<String,String>>();
		
		if(nonEmptyQueries(queries)){
			link(null, null, queries);
		}
		// TODO: saveResults();
		System.out.println("Found " + instancesLinkedSet.size()+ " between datasets");
	}
	

	private void resgisterFunctions(String function) {
		try {
			
			Connection connection = HIKARI_POOL_CONNECTOR.getConnection();
			connection.setAutoCommit(true);
			PreparedStatement result  = connection.prepareStatement(function);
			result.setFetchSize(100);
			result.executeUpdate();
	
			
			result.close();
			connection.close();	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	
	/*
	 * Link only sample instances
	 */

	@Override
	public void linkInstances(Tuple<String,String> queries){
		outputFile = "";

		instancesLinkedSet = new CopyOnWriteArraySet<Tuple<String,String>>();
		if(nonEmptyQueries(queries) && !sampleInstances.isEmpty()){
			sampleInstances.stream().parallel().forEach(pair-> linkPairOfInstances(pair,queries));
		}
	}
	
	private boolean linkPairOfInstances(Tuple<String,String> instancesToLink, Tuple<String,String> queries){
		String sourceInstance = instancesToLink.getFirstElement();
		String targetInstance = instancesToLink.getSecondElement();
		link(sourceInstance, targetInstance, queries);
		return true;
	}
	
	
	/*
	 * Query applicator to database
	 */
	
	private void link(String sourceInstance, String targetInstance, Tuple<String, String> queries) {
		// Adapt queries to sample instances
		Tuple<String,String> newQueries = queries;
		if(sourceInstance!=null && targetInstance!=null) {
			// if sourceInstance and targetInstance are not null, add the clauses:
			//		"WHERE source.id = sourceInstance" to queries.getFirstElement()
			//		"WHERE target.id = targetInstance" to queries.getSecondElement()
		}
		executeSourceQuery(newQueries.getFirstElement(), newQueries.getSecondElement());
	}
	
	
	private void executeSourceQuery(String sourceQueryElements, String targetQueryElements) {
		Tuple<String,String> queryElements =  extractQueryAndQueryExecution(sourceQueryElements);
		String query = queryElements.getFirstElement();
		String execution = queryElements.getSecondElement();
		Set<String> sourceAttributes = extractAttributes(query);
		
		execution = execution.replace("agg:Max", "Maximum");
		execution = execution.replace("str:", "").replaceAll("\\(∂", "(\"restaurantsZ1\".\"");
		System.out.println("-> "+execution);		
		execution = execution.replaceAll("∂,", "\",").replaceAll(",ß", ",\"restaurantsZ2\".\"").replace("ß", "\"");
		
		query = 	"SELECT \"restaurantsZ1\".\"id\", \"restaurantsZ2\".\"id\", \"restaurantsZ1\".\"http://schema.org/name\", \"restaurantsZ2\".\"http://schema.org/name\" \n" + 
				"  FROM \"restaurantsZ1\", \"restaurantsZ2\" \n" + 
				" WHERE 0<"+execution;
		
		try{
			Connection connection = HIKARI_POOL_CONNECTOR.getConnection();
			connection.setAutoCommit(false);
			PreparedStatement result  = connection.prepareStatement(query);
			result.setFetchSize(10000);
			ResultSet rs =  result.executeQuery();
			int i = 0;
			while (rs.next()){
				i++;
				System.out.println(rs.toString());
				System.out.println(">"+i);
				
			/*	
			   String sourceId = rs.getString("id");
			   // Replace execution's attributes with their related values
				for(String attribute: sourceAttributes) {
					String relatedValue=rs.getString(attribute);
					execution = execution.replace(attribute, relatedValue);
				}
				System.out.println("X");
				// Launch target query
				//handleTargetQuery(targetQueryElements, execution, sourceId);
			    /*
			   executor.execute(new Runnable() {
			        @Override
			        public void run() {
			        		
			        		try {
								this.finalize();
							} catch (Throwable e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			        }
			    });
			    */
				
		
			}
			
			// closing everything
			rs.close();
			result.close();
			connection.close();	
		}  catch(JdbcSQLException e) {
			System.out.println("Non blocking error, current query: "+sourceQueryElements+" could not be executed");
			e.printStackTrace();
		} catch( SQLException e) {
			   e.printStackTrace();
			}
	}
	


	private void handleTargetQuery(String targetQueryElements, String execution, String sourceId) {
		Tuple<String,String> queryElements =  extractQueryAndQueryExecution(targetQueryElements);
		String query = queryElements.getFirstElement();
		Set<String> targetAttributes = extractAttributes(query);
		try{
			Connection connection = HIKARI_POOL_CONNECTOR.getConnection();
			connection.setAutoCommit(false);
			PreparedStatement result  = connection.prepareStatement(query);
			result.setFetchSize(10000);
			ResultSet rs =  result.executeQuery();
			
			while (rs.next()){
				String targetId = rs.getString("id");
			    // Replace execution's attributes with their related values
				for(String attribute: targetAttributes) {
					String relatedValue= rs.getString(attribute);
					execution = execution.replace(attribute, relatedValue);
				}
				Double score = 0.0;
				
				try {
					execution = execution.replace("agg:Max", "Maximum");
					execution = execution.replace("str:", "").replace("∂", "\"").replace("ß", "\"").replace("\"", "\"\"");
					System.out.println(execution);
					try {
						
						Connection connection2 = HIKARI_POOL_CONNECTOR.getConnection();
						
						PreparedStatement result2  = connection.prepareStatement("SELECT "+execution);
						result2.setFetchSize(100000);
						ResultSet link =  result.executeQuery();
						System.out.println(link);
						
						result.close();
						connection.close();	
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					/*
					long startTime0 = System.nanoTime();
					LinkSpecificationExecutor specExecutor = new LinkSpecificationExecutor(execution);
					score = EXECUTOR_SERVICE.submit(specExecutor).get();
					long endTime0 = System.nanoTime();
					long duration0 = (endTime0 - startTime0);
					*/
					if(score> 0.0)
						this.instancesLinkedSet.add(new Tuple<String,String>(sourceId, targetId));
					break;
				}catch(Exception e) {
						
				}
				
			}
				// closing everything
				rs.close();
				result.close();
				connection.close();	
			}  catch(JdbcSQLException e) {
				System.out.println("Non blocking error, current query: "+targetQueryElements+" could not be executed");
				e.printStackTrace();
			} catch( SQLException e) {
				   e.printStackTrace();
				}
	}

	
	
	private Set<String> extractAttributes(String query) {
		Set<String> attributes = Sets.newHashSet();
		String[] attributesArray = query.substring(query.indexOf("SELECT")+6, query.indexOf("FROM")).replace("\"", "").replace(",", "").trim().split(" ");
		StreamUtils.asStream(attributesArray).forEach(attr -> attributes.add(attr));
		return attributes;
	}

	// Adds a prefix and a suffix to a given string
	private String wrapString(String value, String separator) {
		StringBuffer newValue = new StringBuffer();
		newValue.append(separator);
		newValue.append(value);
		newValue.append(separator);
		return newValue.toString();
	}
	
	// Splits from the query the execution and the SQL statement
	private Tuple<String,String> extractQueryAndQueryExecution(String query){
		String[] elements = query.split("--");
		return new Tuple<String,String>(elements[0].trim(), elements[1].trim());
	}
	
	/*
	 * Check input queries correctness for 'linkInstances' and 'linkDatasets'
	 */
	
	private Boolean nonEmptyQuery(String query){
		Boolean isCorrect = false;
		String [] queryElements = query.split("--");
		if(queryElements.length==2 && !queryElements[0].isEmpty() && !queryElements[1].isEmpty())
			isCorrect = true;
		return isCorrect;
	}
	
	private Boolean nonEmptyQueries(Tuple<String,String> queries){
		return nonEmptyQuery(queries.getFirstElement()) && nonEmptyQuery(queries.getSecondElement());
	}
	
	/*
	 * Getters & Setters
	 */

	@Override
	public Set<Tuple<String, String>> getInstancesLinked() {
		return instancesLinkedSet;
	}
	
	@Override
	public void setDatasetSource(String datasetSource) {
		this.sourceTable = fixDatasetsName(datasetSource);
	}

	@Override
	public void setDatasetTarget(String datasetTarget) {
		this.targetTable = fixDatasetsName(datasetTarget);
	}
	
	private String fixDatasetsName(String datasetName) {
		String newDatasetName = datasetName;
		if(datasetName.contains("/"))
			newDatasetName = datasetName.substring(datasetName.lastIndexOf("/")+1, datasetName.length());
		return newDatasetName;
	}
	
	@Override
	public void setInstances(Set<Tuple<String, String>> instances) {
		sampleInstances = instances;
	}

	@Override
	@Deprecated
	public void setDatasetSource(Model datasetSource) {
		// empty
	}

	@Override
	@Deprecated
	public void setDatasetTarget(Model datasetTarget) {
		// empty
	}

	/*
	 * Hikary initialization
	 */
	private static HikariDataSource createInitHikari() {
		HikariConfig config = new HikariConfig();
		config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
		config.setConnectionTestQuery("VALUES 1");
		config.addDataSourceProperty("URL", "jdbc:h2:file:/Users/andrea/Desktop/h2;MULTI_THREADED=1;CACHE_SIZE=2048;");
		HikariDataSource ds = new HikariDataSource(config);
		ds.setMaximumPoolSize(50);
		ds.setReadOnly(false);
		return ds;
	}
	
}

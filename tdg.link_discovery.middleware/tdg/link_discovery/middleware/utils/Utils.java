package tdg.link_discovery.middleware.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jena.ext.com.google.common.collect.Sets;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Avg;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Max;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Min;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;


public class Utils {
	
	public static String getCurrentTime() {
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH_mm_ss");
        return sdf.format(cal.getTime());
	}
	
	
	public static Object getRandomElement(Set<? extends Object> set){
		Integer randomIndex = getRandomInteger(set.size()-1, 0);
		Object result = null;
		Integer counter = 0;
		for(Object object:set){
			if(counter.equals(randomIndex)){
				result = object;  
			}
			counter++;
		}
		return result;
	}
	
	
	public static Integer getRandomInteger(Integer maximum, Integer minimum){
		int randomNum = ThreadLocalRandom.current().nextInt(minimum, maximum + 1);
		return randomNum;
	}
	
	 public static Double roundDecimal(Double value, Integer numberOfDigitsAfterDecimalPoint) {
		 if(value.isInfinite() || value.isNaN())
			 value = 0.0;
		
		 BigDecimal bigDecimal = new BigDecimal(value);
		 bigDecimal = bigDecimal.setScale(numberOfDigitsAfterDecimalPoint, BigDecimal.ROUND_HALF_UP);
		 return bigDecimal.doubleValue();
	 }
	 
	 public static Double getMean(List<Double> numbers) {
			 Avg avg = new Avg();
			 return Utils.roundDecimal(avg.applyAggregation(numbers), FrameworkConfiguration.DECIMAL_PRECISION);
	}
		
	public static double getVariance( List<Double> numbers){
		   double mean = getMean(numbers);
		   double temp = 0;
		   for(double a :numbers)
		       temp += (a-mean)*(a-mean);
		   return Utils.roundDecimal(temp/(numbers.size()-1), FrameworkConfiguration.DECIMAL_PRECISION);
	}
		
	// Population Standard Deviation
	public static double getStdDev(List<Double> numbers){
		Double mean = getMean(numbers);
		Double summary =0.0;
		for(Double number:numbers) {
			summary += Math.pow((number - mean), 2);
		}
		return Utils.roundDecimal(Math.sqrt(summary/ numbers.size()), FrameworkConfiguration.DECIMAL_PRECISION);
	}
	 
	 public static Set<String> splitBySpace(String element){
		 Set<String> result = Sets.newHashSet();
		 String[] elements = element.split(" ");
		 for(String token:elements){
			 result.add(token);
		 }
		 return result;
	 }
	 
	 public static void saveCSVofData(String algorithm, String file, List<Double> scores, Integer maxEvaluations){
		
			Integer mod = (int) Math.ceil((scores.size()*1.0)/maxEvaluations);
			int iteration = 1;
			for(int index=0; index<scores.size(); index+=mod){
				Integer indexToRetrieve = index+mod;
				if(indexToRetrieve>scores.size())
					indexToRetrieve = scores.size();
				
				List<Double> subScores = scores.subList(index, indexToRetrieve);
				saveStatistics(algorithm, file, subScores, iteration);
				iteration++;
			}
	 }
	 
	 public static void saveStatistics(String algorithm, String file, List<Double> scores, Integer iteration){
		 StringBuffer newLine = new StringBuffer();
		 Avg avg = new Avg();
		 Max max = new Max();
		 Min min = new Min();
		 
		 newLine.append(algorithm).append(",").append(iteration).append(",")
		 			.append(min.applyAggregation(scores)).append(",")
		 			.append(avg.applyAggregation(scores)).append(",")
		 			.append(max.applyAggregation(scores)).append("\n");
		 try {
			    Files.write(Paths.get(file), newLine.toString().getBytes(), StandardOpenOption.APPEND);
			}catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
	 }
	 
	 public static void appendLineInCSV(String file, String newLine){
		 	try {
		 		if(!file.isEmpty()) {
			 		Path path = Paths.get(getAbsoluteSystemPath(file));
			 		if (Files.exists(path)) {
			 			Files.write(path, newLine.toString().getBytes(), StandardOpenOption.APPEND);
			 		}else{
			 			Files.write(path, newLine.toString().getBytes(), StandardOpenOption.CREATE);
			 		}
		 		}
			}catch (IOException e) {
			   e.printStackTrace();
			}
		}

	 public static String getAbsoluteSystemPath(String file) {
		 String absoluteDir = Paths.get(file).toAbsolutePath().toString();
		 if(absoluteDir.contains("./"))
			 absoluteDir= absoluteDir.replace("./","");
		 if(absoluteDir.contains(".\\"))
			 absoluteDir= absoluteDir.replace(".\\","");
		 return absoluteDir;
	 }
}

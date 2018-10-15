package tdg.moea.test.advanced;

import java.io.BufferedReader;
import java.io.FileReader;

public class CrossDataTest {

	public static void main(String[] args) {
		String training_file = args[0];//"/Users/cimmino/Desktop/rules_from_list/restaurants_rules.csv";
		String production_file = args[1];//"/Users/cimmino/Desktop/production-scenarios/production-restaurants.csv";
		
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(training_file));
			Boolean firstLine = true;
		    String line;
		    while ((line = br.readLine()) != null) {
		    		if(firstLine) {
		    			firstLine = false;
		    			System.out.println(line+"max_it;it;P_e;R_e;F_e");
		    		}else {
		    			// System.out.println(line);
		    			 String[] data = line.split(";");
		    			 String algorithm = data[0];
		    			 String setup = data[2];
		    			 String rule = data[3];
		    			 String enhancedData = retrieveData(production_file, algorithm, setup, rule);
		    			 if(!enhancedData.isEmpty()) {
		    				 line = line.concat(";").concat(enhancedData);
		    				 System.out.println(line);
		
		    			 }else {
		    				 System.out.println("ERROR WITH: "+line);
		    				 break;
		    			 }
		    			
		    		}
		    }
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static String retrieveData(String production_file, String algorithm, String setup, String rule) {
		StringBuilder data = new StringBuilder();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(production_file));
			Boolean firstLine = true;
		    String line;
		    while ((line = br.readLine()) != null) {
		    		if(firstLine) {
		    			firstLine = false;
		    		}else {
		    			String[] productionData = line.split(";");
		    			String ruleProduction = productionData[5];
		    			String P_e = productionData[19];
		    			String R_e = productionData[20];
		    			String F_e = productionData[21];
		    			String it_MAX = productionData[3];
		    			String it = productionData[4];
		    			if(ruleProduction.equals(rule) || rule.equals(ruleProduction)) {
			    			 data.append(it_MAX).append(";");
			    			 data.append(it).append(";");
			    			 data.append(P_e).append(";");
			    			 data.append(R_e).append(";");
			    			 data.append(F_e);
		    			}
		    			
		    		}
		    }
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return data.toString();
	}

}

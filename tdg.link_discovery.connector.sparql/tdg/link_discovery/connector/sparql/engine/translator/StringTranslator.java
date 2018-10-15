package tdg.link_discovery.connector.sparql.engine.translator;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.engine.translator.ITranslator;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.objects.Tuple;

public class StringTranslator implements ITranslator{

	@Override
	public Tuple<String,String> translate(ISpecification<?> specification, IEnvironment environment) {
		return new Tuple<String,String>(specification.toString(),"");
	}

	@Override
	public Object translate(ISpecification<?> specification) {
		return new Tuple<String,String>(specification.toString(),"");
	}
	
	
	private String retrieveRule(String rawRule) {
		String rule = rawRule.trim();
		Pattern p = Pattern.compile("http:[^,\\)]+");
		Matcher matcher = p.matcher(rule);
		int start = 0;
		Boolean even = true;
		int pointer = 0;
		StringBuffer finalRule = new StringBuffer();
		String lastToAppend = "";
		while (matcher.find(start)) {
			String value = matcher.group();
			String newValue = "";
			if(even) {
				newValue = FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER+value+FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER;
				even = false;
			}else {
				newValue =  FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER+value+FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER;
				even = true;
			}
			rule = rule.substring(pointer);
			pointer = 0;
			
			int breakpoint = rule.indexOf(value);
			lastToAppend = rule.substring(breakpoint+value.length(), rule.length());
			finalRule.append(rule.substring(pointer, breakpoint)).append(newValue);
			pointer = breakpoint+value.length();
			
			start = matcher.start() + 1;
		}
		finalRule.append(lastToAppend);
		return finalRule.toString();
	}


	
}

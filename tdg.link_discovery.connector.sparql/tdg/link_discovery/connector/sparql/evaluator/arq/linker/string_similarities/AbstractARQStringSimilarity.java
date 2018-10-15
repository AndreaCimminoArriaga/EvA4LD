package tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase3;
import com.google.common.collect.Lists;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.LowercaseTransformation;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.RemoveSymbolsTransformation;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.StemTransformation;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.StripUriPrefixTransformation;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.binary.TejadaTransformation;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.objects.comparators.DoubleNaturalComparator;
import tdg.link_discovery.middleware.utils.Utils;


public abstract class AbstractARQStringSimilarity extends FunctionBase3 implements IARQStringSimilarity {
	
	protected String name;
	protected Integer decimalPrecision;
	
	public AbstractARQStringSimilarity(String name){
		StringBuffer str = new StringBuffer();
		str.append(SPARQLFactory.prefixJenaFunctionsStrings).append(name);
		this.name = str.toString();
		decimalPrecision = FrameworkConfiguration.DECIMAL_PRECISION;
	}
	
	@Override
	public String getName() {
		return name;
	}
	

	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractARQStringSimilarity other = (AbstractARQStringSimilarity) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public NodeValue exec(NodeValue v1, NodeValue v2, NodeValue v3) {
		String value1 = v1.getString();
		String value2 = v2.getString();
		
		Double threshold = v3.getDouble();
		Double score = 0.0;
		try {
			score = similarity(value1, value2, threshold);
		}catch(Exception e){
			System.out.println("Error with metric '"+this.getName()+"' in ARQStringSimilarity handled, linker will keep working");
		}
		NodeValue resultantNode = NodeValue.makeDouble(score);
		return resultantNode;
	
	}
	



	@Override
	public Double similarity(String value1, String value2, Double threshold){
		String value1Transformed = applyTransformations(value1);
		String value2Transformed = applyTransformations(value2);
		
		/*Tuple<String,String> values = applyBinaryTransformations(value1, value2);
		value1 = values.getFirstElement();
		value2 = values.getSecondElement();*/
		
		// If attributes have no tokenization 'tokenizeStrings' returns an unary-list
		List<String> values1 = tokenizeStrings(value1Transformed);
		List<String> values2 = tokenizeStrings(value2Transformed);
		// Call specific string metric included in the extended class
		Double score = compareStrings(values1, values2);
		
		// Normalizing score
		if(score ==  1.0){
			score = 1.0;
		}else if(score == threshold){
			score = 0.01;
		}else if(score > threshold){
			score = (score-threshold) / (1-threshold);
		}else{
			score = (score/threshold)-1;
		}
		//System.out.println(value1+" "+value2+" "+score);
		//if(score>0)
		//	System.out.println(">"+value1+" "+value2+" "+score);
		score = Utils.roundDecimal(score, SPARQLFactory.DECIMAL_PRECISION);
		return score;
	}
	
	private Tuple<String, String> applyBinaryTransformations(String value1, String value2) {
		Tuple<String,String> result = new Tuple<String,String>(value1, value2);
		if(FrameworkConfiguration.APPLY_STRING_TRANSFORMATIONS_BLOCK){
			TejadaTransformation tejada = new TejadaTransformation();
			result = tejada.applyBinaryTransformation(value1, value2);
		}
		return result;
	}

	private String applyTransformations(String string) {
		String str1 = string;
		if(FrameworkConfiguration.APPLY_STRING_TRANSFORMATIONS_BLOCK){
			RemoveSymbolsTransformation transformation1 = new RemoveSymbolsTransformation();
			StripUriPrefixTransformation transformation2 = new StripUriPrefixTransformation();
			LowercaseTransformation transformation3 = new LowercaseTransformation();
			StemTransformation transformation4 = new StemTransformation();
			str1 = transformation1.applyUnaryTransformation(string);
			str1 = transformation2.applyUnaryTransformation(str1);
			str1 = transformation3.applyUnaryTransformation(str1);
			str1 = transformation4.applyUnaryTransformation(str1);
		}
		return str1;
	}
	
	public static List<String> tokenizeStrings(String string){
		List<String> tokens = Lists.newArrayList();
		
		if(SPARQLFactory.hasTokenization(string)){
			Tuple<String,String> trokenizationAndValue = SPARQLFactory.extractTokenization(string);
			// Retrieve String to tokenize
			String stringToTokenize = trokenizationAndValue.getSecondElement(); 
			// Retrieve tokenization characters 
			String tokenizeCharacters = trokenizationAndValue.getFirstElement().replace(SPARQLFactory.TOKENIZATION_TOKEN, "");
			tokenizeCharacters = tokenizeCharacters.replaceAll("'\\]\\['", "");
			tokenizeCharacters = tokenizeCharacters.substring(2, tokenizeCharacters.length()-2);
			// Apply tokenization
			StringBuilder regex = new StringBuilder();
			regex.append("[").append(tokenizeCharacters).append("]+");
			String[] tokensArray = stringToTokenize.split(regex.toString());
			tokens.addAll(Arrays.asList(tokensArray));
		}else{
			tokens.add(string);
		}
		return tokens;
	}
	


	@Override
	public Double compareStrings(List<String> element1, List<String> element2) {
		List<List<String>> stringPairs = Lists.cartesianProduct(element1, element2);
		Double maxScore = stringPairs.stream()
								.map(pair -> compareStrings(pair.get(0), pair.get(1)))
								.max(new DoubleNaturalComparator()).get();
		return maxScore;
	}


	
	

	
}

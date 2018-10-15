package tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.binary;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.codec.language.RefinedSoundex;
import org.tartarus.snowball.ext.PorterStemmer;

import tdg.link_discovery.middleware.objects.Tuple;

import com.google.common.collect.Lists;



public class TejadaTransformation extends AbstactARQBinaryTransformation{

	public TejadaTransformation() {
		super("TejadaTransformation");
	}
	

	@Override
	public Tuple<String,String> applyBinaryTransformation(String element1, String element2) {
		Tuple<String,String> results = execute(element1, element2);
		if(results.getFirstElement().isEmpty() || results.getSecondElement().isEmpty())
			results = new Tuple<String,String>(element1, element2);
		return results;
	}


	public Tuple<String,String> execute(String argument1, String argument2){
		List<String> tokens1 = Lists.newArrayList();
		tokens1.addAll(Arrays.asList(tokenize(argument1)));
		List<String> tokens2 = Lists.newArrayList();
		tokens2.addAll(Arrays.asList(tokenize(argument2)));
		List<String> charsToRemove = Lists.newArrayList();
		
		for(String token1:tokens1){
			String tokenAux1 = token1.toLowerCase();
			for(String token2:tokens2){
				String tokenAux2 = token2.toLowerCase();
				Boolean areEqual = equality(tokenAux1, tokenAux2);
				if(areEqual){
					charsToRemove.add(token2);
					//System.out.println(token1+" - EQUALITY - "+token2);
					break;
				}
				Boolean prefixL = prefixLeft(tokenAux1, tokenAux2);
				Boolean prefixR = prefixRight(tokenAux1, tokenAux2);
				if(prefixL){
					charsToRemove.add(token1);
					charsToRemove.add(token2);
					//System.out.println(token1+" - is PREFIX OF - "+token2);
					argument1 = argument1.replaceFirst(token1, token2);
					break;
				}
				if(prefixR){
					charsToRemove.add(token1);
					charsToRemove.add(token2);
					//System.out.println(token1+" - contains prefix - "+token2);
					argument2 = argument2.replaceFirst(token2, token1);
					break;
				}
				Boolean initialL = initialLeft(tokenAux1, tokenAux2);
				Boolean initialR = initialRight(tokenAux1, tokenAux2);
				if(initialL){
					charsToRemove.add(token1);
					charsToRemove.add(token2);
					//System.out.println(token1+" - is initial of - "+token2);
					argument1.replace(token1, token2);
					break;
				}
				if(initialR){
					charsToRemove.add(token1);
					charsToRemove.add(token2);
					//System.out.println(token1+" - has initial - "+token2);
					argument2.replace(token2, token1);
					break;
				}
			}

			tokens2.removeAll(charsToRemove);
			if(tokens2.isEmpty())
				break;
		}
		
		tokens1.removeAll(charsToRemove);
		for(String token2:tokens2){
			if(!token2.isEmpty()){
				//System.out.println("Drop - "+token2);
			}
		}
		
		for(String token1:tokens1){
			if(!token1.isEmpty()){
				//System.out.println("Drop - "+token1);
			}
		}
		
		return new Tuple<String,String>(argument1, argument2);
	}

	//Equality
			public static Boolean equality(String argument1, String argument2){
				return argument1.equals(argument2);
			}	
			
			//Steaming
			public static Boolean steamingLeft(String argument1, String argument2){
				String steam1 = stemString(argument1);
				return equality(steam1, argument2);
			}
			public static Boolean steamingRight(String argument1, String argument2){
				return steamingLeft(argument2, argument1);
			}
			
			//Soundex
			public static Boolean soundex(String argument1, String argument2){
				RefinedSoundex refinedS = new RefinedSoundex();
				Boolean sameSoundex = false;
				String sound1 = refinedS.soundex(argument1);
				String sound2 = refinedS.soundex(argument2);
				sameSoundex = equality(sound1, sound2);
				return sameSoundex;
			}
			
			//TODO: Abbreviation transformation
			
			//Initial
			public static Boolean initialLeft(String argument1, String argument2){
				Boolean isInitial = false;
				if(argument1.length() == 1 && argument2.length()>=1)
					isInitial = equality(argument1,argument2.substring(0,1));
				return isInitial;
			}
			public static Boolean initialRight(String argument1, String argument2){
				return initialLeft(argument2, argument1);
			}
			
			//Prefix
			public static Boolean prefixLeft(String argument1, String argument2){
				return argument2.startsWith(argument1) && !argument1.isEmpty() && !argument2.isEmpty();
			}
			public static Boolean prefixRight(String argument1, String argument2){
				return prefixLeft(argument2, argument1) && !argument1.isEmpty() && !argument2.isEmpty();
			}
			
			//Suffix
			public static Boolean suffixLeft(String argument1, String argument2){
				return argument2.endsWith(argument1);
			}
			public static Boolean suffixRight(String argument1, String argument2){
				return suffixLeft(argument2, argument1);
			}
			
			//Subset
			public static Boolean subsetLeft(String argument1, String argument2){
				Boolean isSubset = true;
				for(char character: argument1.toCharArray()){
					String charString = String.valueOf(character); 
					isSubset &= argument2.contains(charString);
				}
				return isSubset;
			}
			public static Boolean subsetRight(String argument1, String argument2){
				return subsetLeft(argument2, argument1);
			}
			
			//Acronym
			public static Boolean acronymLeft(String argument1, String argument2){
				String[] tokens = tokenize(argument2);
				char[] initials = argument1.toCharArray();
				Boolean isAcronym = tokens.length == initials.length;
				
				if(isAcronym){
					for(int i=0; i<tokens.length; i++){
						String charString = String.valueOf(initials[i]); 
						String token = tokens[i];
						isAcronym &= token.substring(0,1).equals(charString);
					}
				}
				return isAcronym;
			}
			public static Boolean acronymRight(String argument1, String argument2){
				return acronymLeft(argument2, argument1);
			}
			
			//Drop
			public static Boolean dropRight(String argument1, String argument2){
				return !argument1.equals(argument2);
			}
			
			//Drop TODO:CHECK
			public static Boolean dropLeft(String argument1, String argument2){
				return !argument1.equals(argument2);
			}
			
			/*
			 * Primary transformations
			 */
			
			private final static String SPLIT_TOKENS_BY = "[^a-zA-Z0-9]";
			public static String[] tokenize(String argument1){
				return argument1.split(SPLIT_TOKENS_BY);
			}
			
			public static String stemString(String argument1) {
			    PorterStemmer stemmer = new PorterStemmer();
			    stemmer.setCurrent(argument1);
				stemmer.stem();
				String result = stemmer.getCurrent();
			    return result;
			}





	





}

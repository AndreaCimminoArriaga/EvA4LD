package tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;

public class TokenizeTransformation extends AbstactARQUnaryTransformation{

	protected String tokenizationChars;
	
	public TokenizeTransformation() {
		super("TokenizeTransformation");
		this.tokenizationChars = " ";
	}
	
	public TokenizeTransformation(String tokenizationChars) {
		super("TokenizeTransformation");
		this.tokenizationChars = tokenizationChars;
	}
	
	@Override
	public String applyUnaryTransformation(String element) {
		StringBuilder newElement = new StringBuilder();
		newElement.append(SPARQLFactory.TOKENIZATION_TOKEN);
		newElement.append("['").append(tokenizationChars).append("']");
		newElement.append(element);
		return newElement.toString();
	}

	public String getTokenizationChars() {
		return tokenizationChars;
	}
	
	// The tokenization special chars must be provided using \\ like in a regex, e.g., \\[
	public void setTokenizationChars(String tokenizationChars) {
		this.tokenizationChars = tokenizationChars;
	}
	


	
}

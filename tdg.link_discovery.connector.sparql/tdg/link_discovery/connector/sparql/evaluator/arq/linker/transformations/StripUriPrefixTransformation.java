package tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations;

public class StripUriPrefixTransformation extends AbstactARQUnaryTransformation{

	protected String tokenizationChars;
	
	public StripUriPrefixTransformation() {
		super("TokenizeTransformation");
		this.tokenizationChars = " ";
	}
	
	public StripUriPrefixTransformation(String tokenizationChars) {
		super("TokenizeTransformation");
		this.tokenizationChars = tokenizationChars;
	}
	
	
	@Override
	public String applyUnaryTransformation(String element) {
		return element.replaceAll("http\\:[a-zA-Z0-9/\\.\\:\\-\\_]+/", "");
	}
	


	
}

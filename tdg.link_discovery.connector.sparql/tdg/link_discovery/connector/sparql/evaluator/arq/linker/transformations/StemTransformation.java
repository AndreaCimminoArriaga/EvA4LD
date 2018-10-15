package tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations;

import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.EnglishStemmer;

public class StemTransformation extends AbstactARQUnaryTransformation{

	public StemTransformation() {
		super("StemTransformation");
	}
	
	
	@Override
	public String applyUnaryTransformation(String element) {
		SnowballProgram snow = new EnglishStemmer();
		snow.setCurrent(element);
		snow.stem();
		return snow.getCurrent();
	}

}

package tdg.link_discovery.tools;


public class ExampleGeneratorMain {

	public static void main(String[] args) {
	
			String goldFile = "./experiments/gold-stds/restaurant1-restaurant2-gold.nt";//String.valueOf(args[0]);
			ExampleGenerator gen = new ExampleGenerator(goldFile, 1.0);
			gen.writeExamplesIntoFile("./experiments/100p-samples/restaurants-samples-100p-t.nt");
			

	}

}
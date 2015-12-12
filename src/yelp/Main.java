package yelp;


public class Main {

	public static void main(String[] args) throws Exception {
		
		System.out.println("Creating MongoDB Index...");
		MongoIndexGenerator mig = new MongoIndexGenerator();
		mig.generateIndex();
		
		System.out.println("/nCreating POS JSON file...");
		POSJSONCreator pjc = new POSJSONCreator();
		pjc.jsonCreaor();
		
		System.out.println("/nCreating ARFF file...");
		ArffFileCreator afc = new ArffFileCreator();
		afc.arffFileCreator();
		
		System.out.println("Process finished!!");
	}

}

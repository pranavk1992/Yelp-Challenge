package yelp;

public class Task2 {

	public static void main(String[] args) throws Exception {
		
		System.out.println("Doing Sentiment Analysis...");
		SentimentAnalyzer sa = new SentimentAnalyzer();
		sa.analyzer();
		
		System.out.println("\nEvaluating sentiments and fetching reasons...");
		Reason r = new Reason();
		r.reason();
		
		System.out.println("\nProcess Finished!!");

	}

}

package yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class sentimentAnalyzer {
		
		static StanfordCoreNLP pipeline;
	    // setting the properties of sentiment 
		public static void init() {
		Properties props = new Properties();
		props.setProperty("annotators", /*" pos, lemma, ner,dcoref,*/"tokenize, ssplit, parse,  sentiment");
		pipeline = new StanfordCoreNLP(props);
		}
		
		// Finding the sentiment of the sentences of the review
		public static String sentimentFinder(String single_sentence) {
			
			String sentiments = "";
			int sentiment_part = 0;
			if (single_sentence != null && single_sentence.length() > 0) {
				int greatest = 0;
				Annotation annotation = pipeline.process(single_sentence);
				
				for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
					sentiments = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
					
					Tree tree = sentence.get(SentimentAnnotatedTree.class);
					int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
					String text_part = sentence.toString();
					if (text_part.length() > greatest) {
						sentiment_part = sentiment;
						greatest = text_part.length();
					}

				}
			}
			
			return sentiments;
		}

		//Splitting the review into sentences
		public static ArrayList<String> sentenceBreaker(String review) {
			BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
			iterator.setText(review);
			ArrayList<String> sent_list = new ArrayList<String>();
			int start = iterator.first();
			for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
				
				sent_list.add(review.substring(start, end));
			}			
			return sent_list;
		}
		
		//Writing to JSON file
		public static void writeToFile(JSONObject json) throws IOException {
			System.out.println(json);
			FileWriter fw = new FileWriter("V://Search_Project//verypositive",true);//In case of very positive sentiment score
			//FileWriter fw = new FileWriter("V://Search_Project//verynegative",true);//In case of very negative sentiment score
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(json.toString());
			bw.write("\n");
			bw.close();
		}

		public static void main(String[] args) throws Exception {
			
			String line = "";
			/*hash map will have key as business_id and 
			 *value will hold sentiment as key and the count of sentences
		      that has various sentiment scores
			 */
			HashMap<String, HashMap<String, Integer>> senti_score = new HashMap<String, HashMap<String, Integer>>(); 
			HashMap<String,String> id_text = new HashMap<String,String>();
			
			init();
			BufferedReader br = new BufferedReader(new FileReader("V://Search_Project//threecount.json"));
			
			while ((line = br.readLine()) != null) {
				 JSONParser parser = new JSONParser();
				 JSONObject obj1 = (JSONObject) parser.parse(line);
				 String business_id = (String) obj1.get("business_id");
				
				 JSONObject obj=(JSONObject)JSONValue.parse(line); 
				 JSONArray arr=(JSONArray)obj.get("reviews");
				 if(arr.size()>0) {
				 String text1 = (String) arr.get(0);
				 	if(text1!=null){
						 ArrayList<String> sentences = new ArrayList<String>();
						 sentences.addAll(sentenceBreaker(text1));
						 //System.out.println(sentences);
				
					 	for (String str : sentences) {
							String sentiment = sentimentFinder(str);
							String neg_very = "Very positive";
							String result = "";
							   if(sentiment.equals(neg_very)){
								   //System.out.println(" "+ sentiment);
								   result += str;
								   //System.out.println(" "+ result);
									if (id_text.containsKey(business_id)) {							
										
											id_text.put(business_id,result);
									}
									else {
					
										id_text.put(business_id,result);
									}
						        }
												
					       }
				 	}
			}
				JSONObject json = new JSONObject();
				for (String key : id_text.keySet()) {
					//System.out.println(key + " : " + id_text.get(key));	
					json.put("business_id",key);
					json.put("reviews",id_text.get(key));
					writeToFile(json);
				}		
			
		}
	}
}
package yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.plaf.synth.SynthSplitPaneUI;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.Hash;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.ArrayUtils;



public class Reason {
	
	//This function returns the reasons without using bigrams
	//The return HashMap contains the reason phrase and the number of times it was found
	//The result HashMap is sorted in descending and can be iterated over to get the top 'n' phrases 
	public HashMap<String, Integer> getReasonSentence(String text)
	{	
		Reason obj = new Reason();
		//Local tagger downloaded from the Stanford POS tagger
		String path = "C:/Users/Pranav/Downloads/stanford-postagger-2015-04-20/stanford-postagger-2015-04-20/models/english-left3words-distsim.tagger";
		text = text.toLowerCase();
		MaxentTagger tagger = new MaxentTagger(path);
		
		Reader reader = new StringReader(text);
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);
		List<String> sentenceList = new ArrayList<String>();
		HashMap<String, Integer> result = new HashMap<>(); 
		
		//Parse the review sentence by sentence
		for(List<HasWord> sentence : dp)
		{
			String senteceString = Sentence.listToString(sentence);
			sentenceList.add(senteceString.toString());
		}
		
		//Create a list of nouns and nouns of adjectives
		for(String s : sentenceList)
		{
			List<String> adjectives = new ArrayList<>();
			List<String> nouns = new ArrayList<>();
			String[] test = {};
			
			String tagged = tagger.tagString(s);
			String[] split = tagged.split(" ");
			
			for(int i=0; i<split.length; i++)
			{
				//Check for adjectives and add it to the adjective list
				if(split[i].contains("_JJ"))
				{
					split[i] = split[i].replace("_JJR", "");					
					split[i] = split[i].replace("_JJS", "");
					split[i] = split[i].replace("_JJ", "");
					adjectives.add(split[i]);					
				}
				if(split[i].contains("_NN"))
				{
					//Check for nouns and add it to the noun list
					split[i] = split[i].replace("_NNPS", "");
					split[i] = split[i].replace("_NNS", "");
					split[i] = split[i].replace("_NNP", "");
					split[i] = split[i].replace("_NN", "");
					nouns.add(split[i]);					
				}			
			}			
					
			Iterator<String> itAdj = adjectives.iterator();
			Iterator<String> itNoun = nouns.iterator();
			
			//Try to get the noun adjective pairs
			while(itAdj.hasNext() && itNoun.hasNext())
			{
				String adj = itAdj.next();
				String noun = itNoun.next();
				String resultString = adj + " " + noun;
				if(result.containsKey(resultString))
				{
					int value = result.get(resultString);
					value++;
					result.put(resultString, value);
				}
				else
				{
					result.put(resultString, 1);
				}				
			}
		}
		result = obj.removeWords(result);
		result = obj.sortByValues(result);
		return result;
	}
	
	
	
	
	//Get the reason using bigrams
	//Parse the sentence using bigrams and try to find the noun adjective pairs
	public HashMap<String, Integer> getReason(String text)	
	{
		Reason obj = new Reason();
		String path = "C:/Users/Pranav/Downloads/stanford-postagger-2015-04-20/stanford-postagger-2015-04-20/models/english-left3words-distsim.tagger";
		text = text.toLowerCase();
		text = text.replaceAll("[^a-zA-Z']", " ");
		HashMap<String, Integer> result = new HashMap<>();
		
		
		MaxentTagger tagger = new MaxentTagger(path);
		String tagged = tagger.tagString(text);		
		String[] split = tagged.split(" ");		
		
		for(int i=0; i<split.length-1; i++)
		{			
			if(split[i].contains("_JJ"))
			{				
				if(split[i+1].contains("_NN"))
				{										
					split[i] = split[i].replace("_JJR", "");					
					split[i] = split[i].replace("_JJS", "");
					split[i] = split[i].replace("_JJ", "");
					split[i+1] = split[i+1].replace("_NNPS", "");
					split[i+1] = split[i+1].replace("_NNS", "");
					split[i+1] = split[i+1].replace("_NNP", "");
					split[i+1] = split[i+1].replace("_NN", "");
				
					String reason = split[i] + " " + split[i+1];
					result = obj.add(result, reason);
				}				
			}
		}
		
		result = obj.removeWords(result);
		result = obj.sortByValues(result);
		return result;
	}
	
	
	/*
	There are some words which do not help identify the reasons
	This function removes the results from the final result hashmap if it contains any of these words
	Returns the hashmap after removing the required entries
	 */
	public HashMap<String, Integer> removeWords(HashMap<String, Integer> result) throws IllegalStateException
	{
		String[] removeWords1 = {"place", "look", "home", "time", "only", "customer", "guest", "mom", "dad", "job", "next", "other", "gas", "thing", "similar", "bus", "anybody", "lot", "way", "addition", "omg"};		
		String[] removeWords2 = {"pittsburgh", "charlotte", "urbana-champaign", "phoenix", "vegas", "madison", "montreal", "waterloo", "karlsruhe", "edinburgh"};
		
		List<String> keyList = new ArrayList<>();
		Iterator<String> it = result.keySet().iterator();
		
		while(it.hasNext())
		{
			String key = it.next();
			for(int i=0; i<removeWords1.length; i++)
			{
				if(key.contains(removeWords1[i]))
				{				
					keyList.add(key);					
				}				
			}
			for(int j=0; j<removeWords2.length; j++)
			{
				if(key.contains(removeWords2[j]))
				{
					keyList.add(key);
				}
			}
		}
		
		for(String s : keyList)
		{
			result.remove(s);
		}
		
		return result;
	}
	
	
	/*
	 Sorts the hashmap on values and returns the sorted hashmap
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Integer> sortByValues(HashMap<String, Integer> noun) 
	{ 
	       List<Object> list = new LinkedList<Object>(noun.entrySet());	       
	       
	       Collections.sort(list, new Comparator<Object>() 
	       {
	            public int compare(Object o1, Object o2) 
	            {
	               return ((Comparable) ((Map.Entry) (o2)).getValue())
	                  .compareTo(((Map.Entry) (o1)).getValue());
	            }
	       });

	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       //System.out.println(sortedHashMap);
	       
	       return sortedHashMap;
	  }
	
		
	/*
	This function is used to add the nouns to the hashmap
	The input is the string (noun) and the hashmap itself
	The function checks for existing nouns in the hashmap and adds it or increments the counter
	After adding the noun, the hashmap is returned
	 */	
	public HashMap<String, Integer> add(HashMap<String, Integer> noun, String s)
	{
		if(noun.containsKey(s))
		{
			int value = noun.get(s);
			value++;
			noun.put(s, value);
		}
		else
		{
			noun.put(s, 1);
		}
		
		return noun;
	}
	
	
	//Read the json file to get the text
	//Use this text to call the get reason function
	public void readJSON(String filePath) throws IOException, ParseException
	{
		Reason obj = new Reason();
		HashMap<String, Integer> result = new HashMap<>();
		FileReader fr = new FileReader(filePath);		
		BufferedReader br = new BufferedReader(fr);
		HashMap<String, String> map = new HashMap();
		
		String line = "";
		while((line = br.readLine()) != null)
		{			
			JSONParser parser = new JSONParser();	
			JSONObject jsonObject = (JSONObject) parser.parse(line);
		
			String businessId = (String) jsonObject.get("business_id");
			String reviews = (String) jsonObject.get("reviews");			
			
			result = obj.getReason(reviews);			
			obj.displayResult(result, businessId);			
		}
	}	
	
	
	/*
	 Temporary function to create the json file in a different format for further parsing
	 */
	public void createJSON(String filePath) throws IOException, ParseException
	{
		Reason obj = new Reason();
		
		FileReader fr = new FileReader(filePath);		
		BufferedReader br = new BufferedReader(fr);
		HashMap<String, String> map = new HashMap();
		
		String line = "";
		while((line = br.readLine()) != null)
		{			
			JSONParser parser = new JSONParser();	
			JSONObject jsonObject = (JSONObject) parser.parse(line);
		
			String businessId = (String) jsonObject.get("business_id");
			String reviews = (String) jsonObject.get("reviews");			
			
			if(map.containsKey(businessId))
			{
				String reviewText = map.get(businessId);
				reviewText += reviews;
				map.put(businessId, reviewText);
			}
			else
			{
				map.put(businessId, reviews);
			}
						
		}
		for(Map.Entry<String, String> entry: map.entrySet())
		{
			String key = entry.getKey();			
			String value = entry.getValue();
			JSONObject json = new JSONObject();
			json.put("business_id", key);
			json.put("reviews", value);
			obj.writeToFile(json, "E:\\negative.json");					
		}
		br.close();		
	}
	
	
	/*
	 Function to write the output into a file in a json format
	 The jaon format is {business_id:"", reasons:"[]"}
	 */
	public void writeToFile(JSONObject json, String filePath) throws IOException
	{		
		FileWriter fw = new FileWriter(filePath, true);
		BufferedWriter bw = new BufferedWriter(fw);		
		bw.write(json.toJSONString());
		bw.write("\n");
		bw.close();
	}
	
	
	/*
	 *Displays the results in the console
	 */
	public void displayResult(HashMap<String, Integer> result, String businessId) throws IOException
	{
		Reason obj = new Reason();
		JSONObject json = new JSONObject();
		List<String> reasons = new ArrayList();
		int i=1;
		System.out.println("Business: " + businessId);
		System.out.println("Reasons for the sentiment:");
		for(Map.Entry<String, Integer> entry : result.entrySet())
		{
			if(i == 6)
			{
				break;
			}
			String key = entry.getKey();
			System.out.println(i + ": " + key);
			reasons.add(key);
			i++;
		}
		json.put("Business_Id", businessId);
		json.put("Reasons", reasons);
		
		//Write the results into a local file
		obj.writeToFile(json, "C:\\Users\\Pranav\\Desktop\\finalResult5.json");
		
	}
	
	
	public static void main(String[] args) throws IOException, ParseException
	{		
		Reason obj = new Reason();	
		
		//obj.createJSON("C:/Users/Pranav/Downloads/verynegative");
		obj.readJSON("E:\\negative.json");
		HashMap<String, Integer> result = new HashMap<>();
		String text = "Cold cheap beer. Great place. Good bar food. Good service. \n\nLooking for a great Pittsburgh style fish sandwich, this is the place to go. The breading is light, fish is more than plentiful and a good side of home cut fries. \n\nGood grilled chicken salads or steak.  Soup of day is homemade and lots of specials. Great place for lunch or bar snacks and beer.";
		String text1 = "They confuse SALT with SAUCE, they give you the wrong orders, the food is very mediocre, greasy, just like the staff's customer service.";
		
		//result = obj.getReason(text1);
		
		//Get the reasons
		result = obj.getReason(text1);
		obj.displayResult(result, "");	
		
	}
}


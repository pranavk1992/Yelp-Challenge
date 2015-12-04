package yelp;

import java.io.BufferedWriter;
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

import javax.swing.plaf.synth.SynthSplitPaneUI;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import org.json.simple.JSONObject;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;



public class Reason {
	
	
	public HashMap<String, Integer> getReasonSentence(String text)
	{		
		String path = "C:/Users/Pranav/Downloads/stanford-postagger-2015-04-20/stanford-postagger-2015-04-20/models/english-left3words-distsim.tagger";
		text = text.toLowerCase();
		MaxentTagger tagger = new MaxentTagger(path);
		//text = text.replaceAll("[^a-zA-Z']", " ");
		Reader reader = new StringReader(text);
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);
		List<String> sentenceList = new ArrayList<String>();
		HashMap<String, Integer> result = new HashMap<>(); 
		
		for(List<HasWord> sentence : dp)
		{
			String senteceString = Sentence.listToString(sentence);
			sentenceList.add(senteceString.toString());
		}
		
		
		for(String s : sentenceList)
		{
			List<String> adjectives = new ArrayList<>();
			List<String> nouns = new ArrayList<>();
			String[] test = {};
			
			String tagged = tagger.tagString(s);
			String[] split = tagged.split(" ");
			
			for(int i=0; i<split.length; i++)
			{
				if(split[i].contains("_JJ"))
				{
					split[i] = split[i].replace("_JJR", "");					
					split[i] = split[i].replace("_JJS", "");
					split[i] = split[i].replace("_JJ", "");
					adjectives.add(split[i]);					
				}
				if(split[i].contains("_NN"))
				{
					split[i] = split[i].replace("_NNPS", "");
					split[i] = split[i].replace("_NNS", "");
					split[i] = split[i].replace("_NNP", "");
					split[i] = split[i].replace("_NN", "");
					nouns.add(split[i]);					
				}
			}
			//System.out.println("----------------------------------------------------");
			//System.out.println("New sentence: " + s);
						
			Iterator<String> itAdj = adjectives.iterator();
			Iterator<String> itNoun = nouns.iterator();
			
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
		return result;
	}
	
	
	public HashMap<String, Integer> getReason(String text)	
	{
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
					if(result.containsKey(reason))
					{
						int value = result.get(reason);
						value++;
						result.put(reason, value);
					}
					else
					{
						result.put(reason, 1);
					}
				}				
			}
		}		
		return result;
	}
	
	public HashMap<String, Integer> removeWords(HashMap<String, Integer> result)
	{
		String[] removeWords = {"place", "look", "job", "next", "other", "gas", "thing", "home"};		
		
		Iterator<String> it = result.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			for(int i=0; i<removeWords.length; i++)
				if(key.contains(removeWords[i]))
				{
					it.remove();
				}
		}		
		
		return result;
	}
	
	
	public HashMap<String, Integer> tag(String text)
	{
		Reason p = new Reason();
		String path = "C:/Users/Pranav/Downloads/stanford-postagger-2015-04-20/stanford-postagger-2015-04-20/models/english-left3words-distsim.tagger";
		text = text.toLowerCase();
		text = text.replaceAll("[^a-zA-Z']", " ");
		
		MaxentTagger tagger = new MaxentTagger(path);	
		String tagged = tagger.tagString(text);
		
		String[] split = tagged.split(" ");
		HashMap<String, Integer> noun = new HashMap<>();
		
		for(String s : split)
		{
			if(s.contains("_NNPS"))
			{
				s = s.replace("_NNPS", "");
				noun = p.add(noun, s);
				//System.out.println(s);
			}
			else if(s.contains("_NNP"))
			{
				s = s.replace("_NNP", "");
				noun = p.add(noun, s);
				//System.out.println(s);
			}
			else if(s.contains("_NNS"))
			{
				s = s.replace("_NNS", "");
				noun = p.add(noun, s);
				//System.out.println(s);
			}
			else if(s.contains("_NN"))
			{
				s = s.replace("_NN", "");
				noun = p.add(noun, s);
				//System.out.println(s);
			}		
		}
		noun = p.sortByValues(noun);
		tagger = null;
		return noun;
	}
	
	
	private static HashMap sortByValues(HashMap noun) 
	{ 
	       List list = new LinkedList(noun.entrySet());
	       
	       Collections.sort(list, new Comparator() 
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
	
	
	public ArrayList<String> getNouns(HashMap<String, Integer> noun)
	{
		ArrayList<String> nounsList = new ArrayList<>();
		int i = 0;
		for(Map.Entry<String, Integer> entry : noun.entrySet())
		{
			if(i == 3)
			{
				break;
			}
			String s = entry.getKey();
			nounsList.add(s);
			i++;
		}		
		return nounsList;
	}
	
	
	public void writeToFile(JSONObject json) throws IOException
	{			
		System.out.println(json);
		FileWriter fw = new FileWriter("E:\\json3.json", true);
		BufferedWriter bw = new BufferedWriter(fw);		
		bw.write(json.toJSONString());
		bw.write("\n");
		bw.close();
	}
	
	public static void main(String[] args) throws IOException
	{		
		Reason pos = new Reason();
		
		HashMap<String, Integer> result = new HashMap<>();
		String text = "Cold cheap beer. Great place. Good bar food. Good service. \n\nLooking for a great Pittsburgh style fish sandwich, this is the place to go. The breading is light, fish is more than plentiful and a good side of home cut fries. \n\nGood grilled chicken salads or steak.  Soup of day is homemade and lots of specials. Great place for lunch or bar snacks and beer.";
		String text1 = "Terrible service.  Food unremarkable.  Waiter disappeared for 45 minutes to serve larger group due to staffing mismanagement.  Saved his tip by discounting meal after I complained.  All and all, a very crude and unpleasant dining experience for me and my guests.  Not to be repeated, never again!";
		
		//result = pos.getReason(text1);
		
		//Get the reasons
		result = pos.getReasonSentence(text1);
		
		//Remove the words which do not help us
		result = pos.removeWords(result);
		
		//Sort the results
		result = pos.sortByValues(result);
		
		System.out.println(result);
		
//		MongoClient mongoClient = new MongoClient();
//	    DB db = mongoClient.getDB("yelp");
//	    DBCollection collections = db.getCollection("results");
//	    DBCursor cursor = collections.find();
//	    
//	    
//	    DBObject document = collections.findOne();
//	    collections.remove(document);
//	    System.out.println("deleted");
//	    
//	    int counter = 0;
//	    
//	    while(cursor.hasNext())
//	    {
//	    	counter++;
//	    	//Read the review text from Mongo
//	    	DBObject testobject = collections.findOne();
//	    	String businessid= (String)testobject.get("business_id");
//	    	BasicDBList categories = (BasicDBList) testobject.get("categories");
//	    	BasicDBList reviews = (BasicDBList) testobject.get("reviews");
//	    	BasicDBList tips = (BasicDBList) testobject.get("tips");	    	
//	    	
//	    	
//	    	//Read list of categories from MongoDB
//	    	String category = "";		
//	    	ArrayList<String> categoryList = new ArrayList<>();		
//	    	for (Object r : categories )
//	    	{
//	    		categoryList.add(r.toString());
//	    	}
//	    	
//	    	//Read the reviews from MongoDB
//	    	String review = "";
//	    	for (Object r : reviews)
//	    	{
//	    		review += r.toString();
//	    	}
//	    	
//	    	//Read the tips from MongoDB
//	    	String tip = "";
//	    	for (Object r : tips)
//	    	{
//	    		tip += r.toString();
//	    	}
//        
//	    	String text = review + tip;
//	    	
//	    	ArrayList<String> nounsList = new ArrayList<>();		
//	    	//Get the list of nouns with their count from the text
//	    	HashMap<String, Integer> noun = new HashMap<>();		
//	    	noun = pos.tag(text);
//		
//	    	//Get the top 3 nouns from the noun hashmap
//	    	nounsList = pos.getNouns(noun);		
//		
//		
//		
//	    	//Insert the nouns for all the corresponding categories
//	    	for(String c : categoryList)
//	    	{
//	    		JSONObject json = new JSONObject();
//	    		json.put("category", c);
//	    		json.put("nouns", nounsList);
//	    		
//			
//	    		//Write the json object to a file
//	    		pos.writeToFile(json);
//	    		json.remove(category);
//	    		json = null;
//	    	}	    	
//	    	System.out.println("Counter: " + counter);
//	    	collections.remove(testobject);
//	    }
	}
}


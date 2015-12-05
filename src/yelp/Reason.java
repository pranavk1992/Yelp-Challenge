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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;



public class Reason {
	
	
	public HashMap<String, Integer> getReasonSentence(String text)
	{	
		Reason obj = new Reason();
		
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
		result = obj.removeWords(result);
		result = obj.sortByValues(result);
		return result;
	}
	
	
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
		return result;
	}
	
	public HashMap<String, Integer> removeWords(HashMap<String, Integer> result)
	{
		String[] removeWords = {"place", "look", "job", "next", "other", "gas", "thing", "home", "pittsburgh", "time", "only"};		
		
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
	
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Integer> sortByValues(HashMap<String, Integer> noun) 
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
	
	
	public void readJSON(String filePath) throws IOException, ParseException
	{
		System.out.println("read json");
		FileReader fr = new FileReader(filePath);		
		BufferedReader br = new BufferedReader(fr);
		
		String line = "";
		while((line = br.readLine()) != null)
		{			
			JSONParser parser = new JSONParser();	
			JSONObject jsonObject = (JSONObject) parser.parse(line);
		
			String businessId = (String) jsonObject.get("business_id");
			//String reviews = (String) jsonObject.get("reviews");
			System.out.println("businessID: " + businessId);
			//System.out.println("reviews: " + reviews);
			//Call the getReasonSentence here with review text as the input parameter
			
		}
		br.close();		
	}
	
	
	public void displayResult(HashMap<String, Integer> result)
	{
		int i=1;
		System.out.println("Reasons for the sentiment:");
		for(Map.Entry<String, Integer> entry : result.entrySet())
		{
			if(i == 6)
			{
				break;
			}
			String key = entry.getKey();
			System.out.println(i + ": " + key);
			i++;
		}			
	}
	
	
	public static void main(String[] args) throws IOException, ParseException
	{		
		Reason obj = new Reason();	
		
		//obj.readJSON("C:/Users/Pranav/Desktop/test.json");
		
		HashMap<String, Integer> result = new HashMap<>();
		String text = "Cold cheap beer. Great place. Good bar food. Good service. \n\nLooking for a great Pittsburgh style fish sandwich, this is the place to go. The breading is light, fish is more than plentiful and a good side of home cut fries. \n\nGood grilled chicken salads or steak.  Soup of day is homemade and lots of specials. Great place for lunch or bar snacks and beer.";
		String text1 = "I went to Vegas recently for a mini-getaway, and being the crazy ramen aficionados that we are, my boyfriend and I HAD to come here and try this place during our trip.\n\nWe both ordered the Tonkotsu-Shoyu ramen with extra chashu and hard-boiled (more like deliciously and perfectly soft-boiled!) egg.  The broth was absolutely delicious--just the right amount of flavor and rich pork fatty goodness to make it taste perfect!  Even though it was 103 degrees outside, I didn't care.  I ate and drank the entire bowl clean.  Gluttony at its finest.  Woo!  I was extremely impressed with the quality and texture of their chashu.  It was marinated just right and they really did cook it for hours to get it to really melt in my mouth!  The moment I bit into a piece, I didn't even really have to chew at all...it immediately disintegrated into orgasmic morsels of ecstasy and rocked a party in my mouth!  The egg was perfection...just the way all ramen \"hard-boiled\" (I still think they should call them \"soft-boiled\"...it just suits the texture better!) eggs should be.  The yolk was nice and and gooey and soft.  It complimented the ramen broth so well!  Needless to say, I wolfed down the entire bowl in about 10 minutes or less. hehe ^_^  \n\nWe also got an order of the regular fried rice, and the only thing I can say is that it is THE BEST Japanese fried rice I have EVER had in my life thus far (in the States).  The rice was so extremely flavorful and light (because too much oil is a no-no).  Sprinkled with those little slices of heaven known as their chashu and plenty of pickled ginger, it tasted PHENOMENAL.  That was the 2nd party that rocked my mouth in one sitting!  Boy, what an awesomely dining experience!\n\nExquisite ramen.  Good price.  Great service.  Will most DEFINITELY have to visit again whenever we're in the area.\n\nOh, and my brother (who doesn't eat pork) also came with us.  He ordered the kimchi (or spicy?) fried rice without the pork and REALLY enjoyed his food.  He was even full off just that one single plate, so their portions are very reasonable, considering the elephant-sized appetite that he has.";
		
		//result = obj.getReason(text1);
		
		//Get the reasons
		result = obj.getReasonSentence(text1);
		obj.displayResult(result);	
		
	}
}


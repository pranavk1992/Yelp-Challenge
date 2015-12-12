package yelp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class POSJSONCreator {
	
	
	/*
	This function tags the input string text using the Stanford POS Tagger
	The output is a HashMap<String, Integer> where the string is the noun and integer is the count
	Use the add function to add the noun to the hashmap
	*/	
	public HashMap<String, Integer> tag(String text)
	{
		POSJSONCreator p = new POSJSONCreator();
		//This is the path to the tagger downloaded from Stanford POS Tagger
		String path = Constants.TAGGER_FILE_PATH;
		
		//The text to be tagged is stripped and cleaned
		text = text.toLowerCase();
		text = text.replaceAll("[^a-zA-Z']", " ");
		
		MaxentTagger tagger = new MaxentTagger(path);	
		String tagged = tagger.tagString(text);
		
		String[] split = tagged.split(" ");
		HashMap<String, Integer> noun = new HashMap<>();
		
		//The tags NNPS, NNP, NNS, and NN denote the singular/plural noun/proper nouns
		//Theese tags are replaced with an empty string so that only the word remains
		//'menu_NN' becomes 'menu' 
		for(String s : split)
		{
			if(s.contains("_NNPS"))
			{
				s = s.replace("_NNPS", "");
				noun = p.add(noun, s);				
			}
			else if(s.contains("_NNP"))
			{
				s = s.replace("_NNP", "");
				noun = p.add(noun, s);				
			}
			else if(s.contains("_NNS"))
			{
				s = s.replace("_NNS", "");
				noun = p.add(noun, s);				
			}
			else if(s.contains("_NN"))
			{
				s = s.replace("_NN", "");
				noun = p.add(noun, s);				
			}		
		}
		tagger = null;
		
		//Sort the HashMap by values and return
		noun = p.sortByValues(noun);		
		return noun;
	}
	
	/*
	This function takes the hashmap as in input and sorts it based on value
	The sorted hashmap is returned	 
	*/
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
	
	
	/*
	This function is used to add the nouns to the hashmap
	The input is the string (noun) and the hashmap itself
	The function checks for existing nouns in the hashmap and adds it or increments the counter
	After adding the noun, the hashmap is returned
	 */	
	public HashMap<String, Integer> add(HashMap<String, Integer> noun, String s)
	{
		//If the noun exists, update the counter
		if(noun.containsKey(s))
		{
			int value = noun.get(s);
			value++;
			noun.put(s, value);
		}
		//If the noun doesn't exist, the noun is added with a counter 1
		else
		{
			noun.put(s, 1);
		}
		//Return the hashmap
		return noun;
	}
	
	
	/*
	This function takes the final noun hashmap as the input
	The function iterates over the hashmap and gets the top 3 nouns from the hashmap
	A list containing the top 3 nouns is returned 
	*/
	public ArrayList<String> getNouns(HashMap<String, Integer> noun)
	{
		ArrayList<String> nounsList = new ArrayList<>();
		int i = 0;
		for(Map.Entry<String, Integer> entry : noun.entrySet())
		{
			//To get the top 3
			//Change this to any number as per requirement
			if(i == 3)
			{
				break;
			}
			String s = entry.getKey();
			nounsList.add(s);
			i++;
		}		
		//This is the list that contains the list of the required number of nouns
		return nounsList;
	}
	
	/*
	This function takes in oen json object as input and writes it to the output file
	This output file will then be used for further tasks
	 */
	public void writeToFile(JSONObject json) throws IOException
	{			
		System.out.println(json);
		//Local filepath for the output file
		FileWriter fw = new FileWriter("E:\\json3.json", true);
		BufferedWriter bw = new BufferedWriter(fw);		
		bw.write(json.toJSONString());
		bw.write("\n");
		bw.close();
	}
	
	
	public void jsonCreator() throws IOException
	{		
		POSJSONCreator pos = new POSJSONCreator();
		
		//Read the data from MongoDB		
		MongoClient mongoClient = new MongoClient();
	    DB db = mongoClient.getDB("yelp");
	    DBCollection collections = db.getCollection("results");
	    DBCursor cursor = collections.find();
	    
	    //Deleting one document as I had to run the code multiple times due to memory constraints
	    //Deleting the last one helped continue from the point I left last time
	    DBObject document = collections.findOne();
	    collections.remove(document);
	    System.out.println("deleted");
	    
	    int counter = 0;	    
	    
	    while(cursor.hasNext())
	    {
	    	counter++;
	    	//Read the review text from Mongo
	    	DBObject testobject = collections.findOne();
	    	String businessid= (String)testobject.get("business_id");
	    	BasicDBList categories = (BasicDBList) testobject.get("categories");
	    	BasicDBList reviews = (BasicDBList) testobject.get("reviews");
	    	BasicDBList tips = (BasicDBList) testobject.get("tips");	    	
	    	
	    	
	    	//Read list of categories from MongoDB
	    	String category = "";		
	    	ArrayList<String> categoryList = new ArrayList<>();		
	    	for (Object r : categories )
	    	{
	    		categoryList.add(r.toString());
	    	}
	    	
	    	//Read the reviews from MongoDB
	    	String review = "";
	    	int reviewCounter = 0;
	    	for (Object r : reviews)
	    	{
	    		review += r.toString();
	    		reviewCounter++;
	    	}	    	
	    	
	    	//Read the tips from MongoDB
	    	String tip = "";
	    	for (Object r : tips)
	    	{
	    		tip += r.toString();
	    	}
	    		
	    	String text = review + tip;
	    	
	    	ArrayList<String> nounsList = new ArrayList<>();		
	    	//Get the list of nouns with their count from the text
	    	HashMap<String, Integer> noun = new HashMap<>();		
	    	noun = pos.tag(text);
		
	    	//Get the top 3 nouns from the noun hashmap
	    	nounsList = pos.getNouns(noun);		
				
	    	//Insert the nouns for all the corresponding categories
	    	for(String c : categoryList)
	    	{
	    		JSONObject json = new JSONObject();
	    		json.put("category", c);
	    		json.put("nouns", nounsList);
	    		
			
	    		//Write the json object to a file
	    		pos.writeToFile(json);
	    		json.remove(category);
	    		json = null;
	    	}	    	
	    	//Remove the last entry from the mongo index
	    	//This helps save memory and continue without having to keep a counter if the program runs out of memory
	    	collections.remove(testobject);	    	
	    }
	}
}

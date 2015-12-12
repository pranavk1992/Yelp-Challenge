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

	public HashMap<String, Integer> tag(String text) {
		POSJSONCreator p = new POSJSONCreator();
		String path = "C:/Users/Pranav/Downloads/stanford-postagger-2015-04-20/stanford-postagger-2015-04-20/models/english-left3words-distsim.tagger";
		text = text.toLowerCase();
		text = text.replaceAll("[^a-zA-Z']", " ");

		MaxentTagger tagger = new MaxentTagger(path);
		String tagged = tagger.tagString(text);

		String[] split = tagged.split(" ");
		HashMap<String, Integer> noun = new HashMap<>();

		for (String s : split) {
			if (s.contains("_NNPS")) {
				s = s.replace("_NNPS", "");
				noun = p.add(noun, s);
			} else if (s.contains("_NNP")) {
				s = s.replace("_NNP", "");
				noun = p.add(noun, s);
			} else if (s.contains("_NNS")) {
				s = s.replace("_NNS", "");
				noun = p.add(noun, s);
			} else if (s.contains("_NN")) {
				s = s.replace("_NN", "");
				noun = p.add(noun, s);
			}
		}
		noun = p.sortByValues(noun);
		tagger = null;
		return noun;
	}

	private static HashMap sortByValues(HashMap noun) {
		List list = new LinkedList(noun.entrySet());

		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
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

		return sortedHashMap;
	}

	public HashMap<String, Integer> add(HashMap<String, Integer> noun, String s) {
		if (noun.containsKey(s)) {
			int value = noun.get(s);
			value++;
			noun.put(s, value);
		} else {
			noun.put(s, 1);
		}

		return noun;
	}

	public ArrayList<String> getNouns(HashMap<String, Integer> noun) {
		ArrayList<String> nounsList = new ArrayList<>();
		int i = 0;
		for (Map.Entry<String, Integer> entry : noun.entrySet()) {
			if (i == 3) {
				break;
			}
			String s = entry.getKey();
			nounsList.add(s);
			i++;
		}
		return nounsList;
	}

	public void writeToFile(JSONObject json) throws IOException {
		System.out.println(json);
		FileWriter fw = new FileWriter("E:\\json3.json", true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(json.toJSONString());
		bw.write("\n");
		bw.close();
	}

	public void jsonCreaor() throws IOException {
		POSJSONCreator pos = new POSJSONCreator();

		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB("yelp");
		DBCollection collections = db.getCollection("results");
		DBCursor cursor = collections.find();

		DBObject document = collections.findOne();
		collections.remove(document);

		int counter = 0;

		while (cursor.hasNext()) {
			counter++;
			// Read the review text from Mongo
			DBObject testobject = collections.findOne();
			String businessid = (String) testobject.get("business_id");
			BasicDBList categories = (BasicDBList) testobject.get("categories");
			BasicDBList reviews = (BasicDBList) testobject.get("reviews");
			BasicDBList tips = (BasicDBList) testobject.get("tips");

			// Read list of categories from MongoDB
			String category = "";
			ArrayList<String> categoryList = new ArrayList<>();
			for (Object r : categories) {
				categoryList.add(r.toString());
			}

			// Read the reviews from MongoDB
			String review = "";
			int reviewCounter = 0;
			for (Object r : reviews) {
				review += r.toString();
				reviewCounter++;
			}
			int c = 0;
			if (reviewCounter > 100) {
				c++;
			}

			// Read the tips from MongoDB
			String tip = "";
			// for (Object r : tips)
			// {
			// tip += r.toString();
			// }

			// String text = review + tip;

			// ArrayList<String> nounsList = new ArrayList<>();
			// Get the list of nouns with their count from the text
			// HashMap<String, Integer> noun = new HashMap<>();
			// noun = pos.tag(text);

			// Get the top 3 nouns from the noun hashmap
			// nounsList = pos.getNouns(noun);

			// Insert the nouns for all the corresponding categories
			// for(String c : categoryList)
			// {
			// JSONObject json = new JSONObject();
			// json.put("category", c);
			// json.put("nouns", nounsList);

			// Write the json object to a file
			// pos.writeToFile(json);
			// json.remove(category);
			// json = null;
			// }
			// System.out.println("Counter: " + counter);
			collections.remove(testobject);

		}
	}
}

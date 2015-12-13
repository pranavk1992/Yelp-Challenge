package yelp;

import java.io.IOException;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * This code is used to check if the MongoDB index has been made properly.
 * 
 * @author Anudhriti Katanguri
 *
 */
public class Result {
	public static void main(String[] args) throws IOException {
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB("yelp");
		DBCollection collections = db.getCollection("results");
		DBCursor cursor = collections.find();
		while (cursor.hasNext()) {
			DBObject testobject = cursor.next();
			String businessid = (String) testobject.get("business_id");
			BasicDBList categories = (BasicDBList) testobject.get("categories");
			BasicDBList reviews = (BasicDBList) testobject.get("reviews");
			BasicDBList tips = (BasicDBList) testobject.get("tips");
			String category = "";
			for (Object r : categories) {
				category += r.toString() + " ";
			}
			String review = "";
			for (Object r : reviews) {
				review += r.toString() + " ";
			}
			String tip = "";
			for (Object r : tips) {
				tip += r.toString() + " ";
			}

			System.out.println("Business ID: " + businessid);
			System.out.println("Categories: " + category);
			System.out.println("Reviews: " + reviews);
			System.out.println("Tips: " + tips);

		}

	}
}

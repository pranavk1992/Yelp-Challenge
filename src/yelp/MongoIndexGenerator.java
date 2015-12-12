package yelp;

import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.parser.ParseException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MongoIndexGenerator {

	public void generateIndex() throws IOException, ParseException {
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB("yelp");
		
		DBCollection result = db.getCollection("results");
		DBCollection business = db.getCollection("business");
		DBCollection review = db.getCollection("review");
		DBCollection tips = db.getCollection("tip");

		DBObject business_query = new BasicDBObject();
		
		business_query.put("_id", 0);
		business_query.put("business_id", 1);
		business_query.put("categories", 1);
		
		DBCursor businessCursor = business.find(new BasicDBObject(),business_query);
		
		while (businessCursor.hasNext()) {

			DBObject businessObject = businessCursor.next();
			DBObject business_document = new BasicDBObject(businessObject.toMap());
			String businessid = (String) businessObject.get("business_id");
			BasicDBObject tips_query = new BasicDBObject();
			// Getting review results for particular business_id
			tips_query.put("business_id", businessid);
			DBCursor tipsCursor = tips.find(tips_query);
			ArrayList<String> tips_list = new ArrayList<String>();
			while (tipsCursor.hasNext()) {
				DBObject tipsObject = tipsCursor.next();
				DBObject tips_document = new BasicDBObject(tipsObject.toMap());
				String tips_text = (String) tipsObject.get("text");
				tips_list.add(tips_text);
			}
			business_document.put("tips", tips_list);
			BasicDBObject review_query = new BasicDBObject();
			review_query.put("business_id", businessid);
			DBCursor reviewsCursor = review.find(review_query);
			ArrayList<String> reviews_list = new ArrayList<String>();
			while (reviewsCursor.hasNext()) {
				DBObject reviewsObject = reviewsCursor.next();
				DBObject reviews_document = new BasicDBObject(reviewsObject.toMap());
				String reviews_text = (String) reviewsObject.get("text");
				reviews_list.add(reviews_text);
			}
			business_document.put("reviews", reviews_list);
			result.insert(business_document);
		}	
	}
}

package yelp;

import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import yelp.business.tipsJSON;

public class business {	
	
	
	public class tipsJSON{
		public String business_id;
		public String tipText;
	}
	
	
	public class businessJSON {
		public String business_id;
		public String city;
		public List<String> categoryList;
	}
	
	
	
		
	public HashMap<String, Object> convertToStringTips(String filepath) throws IOException, ParseException
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		FileReader fr = new FileReader(filepath);
		BufferedReader br = new BufferedReader(fr);
		StringBuilder sb = new StringBuilder();
		
		String line;
		while((line = br.readLine()) != null)
		{
			sb.append(line);
			map = createHashTips(line, map);
			sb.append("\n");			
		}
		fr.close();
		br.close();		
				
		return map;
	}
	
	public HashMap<String, Object> convertToStringBusiness(String filepath) throws IOException, ParseException
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		FileReader fr = new FileReader(filepath);
		BufferedReader br = new BufferedReader(fr);
		StringBuilder sb = new StringBuilder();
		
		String line;
		while((line = br.readLine()) != null)
		{
			sb.append(line);
			map = createHashBusiness(line, map);
			sb.append("\n");			
		}
		fr.close();
		br.close();		
		return map;				
	}
	
	public HashMap<String, Object> createHashTips(String line, HashMap<String, Object> map) throws IOException, ParseException
	{		
		business businessObject = new business();
		business.tipsJSON data = businessObject.new tipsJSON();
				
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(line);
		JSONObject jsonObject = (JSONObject) obj;
		
		String id = (String) jsonObject.get("business_id");
		String tipText =  (String) jsonObject.get("text");		
		
		if(map.containsKey(id))
		{
			data = (tipsJSON) map.get(id);
			data.tipText += tipText;
		}
		
		else
		{
			data.business_id = id;
			data.tipText = tipText;
		}		
		
		map.put(id, data);
		data = null;
		return map;
	}
	
	
	public HashMap<String, Object> createHashBusiness(String line, HashMap<String, Object> map) throws IOException, ParseException
	{		
		business businessObject = new business();
		business.businessJSON data= businessObject.new businessJSON();
		
		List<String> categoryList = new ArrayList<String>();
		
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(line);
		JSONObject jsonObject = (JSONObject) obj;
		
		String id = (String) jsonObject.get("business_id");
		String city = (String) jsonObject.get("city");		
		
		JSONArray categories =  (JSONArray) jsonObject.get("categories");		
		for(int i=0; i<categories.size(); i++)
		{
			String category = (String)categories.get(i);			
			categoryList.add(category);
		}
		
		data.business_id = id;
		data.city = city;
		data.categoryList = categoryList;		
		
		map.put(id, data);
		data = null;
		return map;
	}
	
	
	public static void main(String[] args) throws IOException, ParseException
	{			
		HashMap<String, Object> mapBusiness = new HashMap<>();
		HashMap<String, Object> mapTips = new HashMap<>();	
		 
		business obj = new business();
		
		business.businessJSON valueBusiness = obj.new businessJSON();
		business.tipsJSON valueTips = obj.new tipsJSON();
		
		String filepathBusiness = "D:/Lectures/Fall 15/Search/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_business.json";
		String filepathTips = "D:/Lectures/Fall 15/Search/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_tip.json";
		
		mapBusiness = obj.convertToStringBusiness(filepathBusiness);
		mapTips = obj.convertToStringTips(filepathTips);
		
		int counterBusiness = 0;
		for(Map.Entry<String, Object> entry: mapBusiness.entrySet())
		{
			String key = entry.getKey();
			valueBusiness = (businessJSON) entry.getValue();		
			//System.out.println("ID: "+ valueBusiness.business_id + " City: " + valueBusiness.city + " Categories: " + valueBusiness.categoryList);
			//System.out.println(valueBusiness.city);
			counterBusiness++;
		}
		System.out.println("Number of records in Business: " + mapBusiness.size());
		
		int counterTips = 0;
		for(Map.Entry<String, Object> entry:mapTips.entrySet())
		{
			//String key = entry.getKey();
			//valueTips = (tipsJSON) entry.getValue();
			//System.out.println("ID:" + valueTips.business_id + " Text: " + valueTips.tipText);
			counterTips++;
		}
		System.out.println("Number of records in Tips: " + mapTips.size());
	}
}

/*
 * 
 */

package yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ArffFileCreator {

	public void fileCreator(File f, BufferedWriter arffWriter, BufferedWriter csvWriter)
			throws IOException {

		BufferedReader br1 = new BufferedReader(new FileReader(f));

		String s = "";
		HashMap<String, Integer> wordStatus = new HashMap<String, Integer>();
		HashMap<String, Integer> categoryMap = new HashMap<String, Integer>();
		String[] words = new String[3];
		String word, sub, category;
		int counter = 0, categoryCount = 0, cat;

		while ((s = br1.readLine()) != null) {
			sub = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
			if (sub.length() > 0) {
				words = sub.split(",");
				for (int i = 0; i < words.length; i++) {
					word = words[i].substring(words[i].indexOf("\"") + 1,
							words[i].lastIndexOf("\""));
					if (word.length() < 3)
						continue;
					if (word.contains("'")) {
						word = word.replace("'", "\\'");
						word = "\'" + word + "\'";
					}
					else
						word = "\'" + word + "\'";
					if (!wordStatus.containsKey(word) && word.length() > 1) {
						wordStatus.put(word, 0);
					}
				}
			}
		}

		System.out.println(wordStatus.size());

		// Writing relation name in both arff files
		arffWriter.write("@relation reviewtips\n\n");

		// Writing attributes in both arff files
		for (String att : wordStatus.keySet()) {
			arffWriter.write("@attribute " + att + " {0,1}\n");
		}
		
		BufferedReader br2 = new BufferedReader(new FileReader(f));
		
		while ((s = br2.readLine()) != null) {
			
			sub = s.substring(s.indexOf(":\":") + 4);

			// Takes out category from json file
			category = sub.substring(0, sub.indexOf("\""));
			
			/*
			 * Checks if a category is present in category map If yes, then it
			 * doesn't put it again otherwise it does.
			 */
			if (!categoryMap.containsKey(category)) {
				categoryCount++;
				categoryMap.put(category, categoryCount);
				csvWriter.write(category+","+categoryCount+"\n");
			}
		}
		
		String categoryType = "{";
		
		for (Map.Entry<String, Integer> entry : categoryMap.entrySet())
			categoryType += entry.getValue()+",";

		
		categoryType += categoryType.substring(0, categoryType.lastIndexOf(","))+"}";
		
		arffWriter.write("@attribute 'category' "+categoryType+"\n");
		arffWriter.write("\n@data\n");	

		System.out.println("Populating hashmap and creating file...");
		
		BufferedReader br3 = new BufferedReader(new FileReader(f));

		while ((s = br3.readLine()) != null) {

			// Just to check how many rows have been added to file
			if (counter % 10000 == 0)
				System.out.println(counter + " data rows written");
			counter++;

			sub = s.substring(s.indexOf(":\":") + 4);

			// Takes out category from json file
			category = sub.substring(0, sub.indexOf("\""));

			// Temporary map: To store map of words for each category
			HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
			tempMap.putAll(wordStatus);

			/*
			 * Extracts each noun word from the line and updates the word map
			 * accordingly. So, for each category there will be only 3 words
			 * which will be 1 others will be 0
			 */
			sub = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
			if (sub.length() > 0) {
				words = sub.split(",");
				for (int i = 0; i < words.length; i++) {
					word = words[i].substring(words[i].indexOf("\"") + 1,
							words[i].lastIndexOf("\""));
					if (word.length() < 3)
						continue;
					if (word.contains("'")) {
						word = word.replace("'", "\\'");
						word = "\'" + word + "\'";
					}
					tempMap.put(word, 1);
				}
			}

			/*
			 * Puts each category and associated values into the file
			 */
			for (Map.Entry<String, Integer> entry : tempMap.entrySet()) {
				arffWriter.write(entry.getValue() + ",");
			}
			arffWriter.write(categoryMap.get(category) + "\n");
		}

		br1.close();
		br2.close();
	}

	public static void main(String[] args) throws IOException {

		File f = new File(
				"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\pos.json");
		BufferedWriter arffWriter = new BufferedWriter(
				new FileWriter(
						new File(
								"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\reviewtipscategory.arff")));
		BufferedWriter csvWriter = new BufferedWriter(
				new FileWriter(
						new File(
								"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\categoryMap.csv")));

		ArffFileCreator fc = new ArffFileCreator();
		fc.fileCreator(f, arffWriter, csvWriter);
		System.out.println("Done!!");
		arffWriter.close();
		csvWriter.close();
	}
}
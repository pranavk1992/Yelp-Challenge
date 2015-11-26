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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.activation.DataHandler;

public class ArffFileCreator {

	public void fileCreator(File f, BufferedWriter arffWriter)
			throws IOException {

		BufferedReader br1 = new BufferedReader(new FileReader(f));

		String s = "";
		HashMap<String, Integer> wordStatus = new HashMap<String, Integer>();
		HashMap<String, HashMap<String, Integer>> dataMap = new HashMap<String, HashMap<String, Integer>>();
		String[] words = new String[3];
		String word, sub, category;

		while ((s = br1.readLine()) != null) {
			sub = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
			if (sub.length() > 0) {
				words = sub.split(",");
				for (int i = 0; i < words.length; i++) {
					word = words[i].substring(words[i].indexOf("\"") + 1,
							words[i].lastIndexOf("\""));
					if (!wordStatus.containsKey(word) && word.length() > 1) {
						wordStatus.put(word, 0);
					}
				}
			}
		}

		BufferedReader br2 = new BufferedReader(new FileReader(f));

		System.out
				.println("Populating hashmap and creating file...");

		while ((s = br2.readLine()) != null) {
			sub = s.substring(s.indexOf(":\":") + 4);
			category = sub.substring(0, sub.indexOf("\""));

			if (!dataMap.containsKey(category)) {
				HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
				tempMap.putAll(wordStatus);
				dataMap.put(category, tempMap);
			} else {
				sub = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
				if (sub.length() > 0) {
					words = sub.split(",");
					for (int i = 0; i < words.length; i++) {
						word = words[i].substring(words[i].indexOf("\"") + 1,
								words[i].lastIndexOf("\""));
						dataMap.get(category).put(word, 1);
					}
				}
			}
		}

		// Writing relation name in both arff files
		arffWriter.write("@relation review&tips\n\n");

		// Writing attributes in both arff files
		for (String att : wordStatus.keySet()) {
			arffWriter.write("@attribute " + att + " integer\n");
		}

		arffWriter.write("\n@data\n\n");

		for (Map.Entry<String, HashMap<String, Integer>> entry : dataMap.entrySet())
		{
		    for(Map.Entry<String, Integer> term : entry.getValue().entrySet()) {
		    	arffWriter.write(term.getValue()+",");
		    }
		    arffWriter.write(entry.getKey()+"\n");
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

		ArffFileCreator fc = new ArffFileCreator();
		fc.fileCreator(f, arffWriter);
		System.out.println("Done!!");
		arffWriter.close();
	}

}

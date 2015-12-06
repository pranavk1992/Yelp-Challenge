/*
 * 
 */

package yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class ArffFileCreator {

	public void fileCreator(File f, File arff, BufferedWriter arffWriter,
			BufferedWriter csvWriter) throws Exception {

		FastVector attributes, attributeType, categoryT;
		Instances data;

		BufferedReader br1 = new BufferedReader(new FileReader(f));

		String s = "";
		ArrayList<String> wordStatus = new ArrayList<String>();
		HashMap<String, Integer> categoryMap = new HashMap<String, Integer>();
		String[] words = new String[3];
		String word, sub, category;
		int counter = 0, categoryCount = 0;

		attributes = new FastVector();
		attributeType = new FastVector();
		attributeType.addElement("0");
		attributeType.addElement("1");

		while ((s = br1.readLine()) != null) {
			sub = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
			if (sub.length() > 0) {
				words = sub.split(",");
				for (int i = 0; i < words.length; i++) {
					word = words[i].substring(words[i].indexOf("\"") + 1,
							words[i].lastIndexOf("\""));
					if (!wordStatus.contains(word) && word.length() > 1) {
						wordStatus.add(word);
						attributes
								.addElement(new Attribute(word, attributeType));
					}
				}
			}
		}

		System.out.println(wordStatus.size());
		BufferedReader br2 = new BufferedReader(new FileReader(f));
		categoryT = new FastVector();

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
				csvWriter.write(category + "," + categoryCount + "\n");
				categoryT.addElement(Integer.toString(categoryCount));				
			}
		}

		attributes.addElement(new Attribute("category", categoryT));

		data = new Instances("TipsReviewsCategories", attributes, 0);

		System.out.println("Populating hashmap and creating file...");

		BufferedReader br3 = new BufferedReader(new FileReader(f));

		while ((s = br3.readLine()) != null) {

			double[] vals = new double[data.numAttributes()];

			for (int i = 0; i < vals.length; i++)
				vals[i] = attributeType.indexOf("0");

			// Just to check how many rows have been added to file
			if (counter % 500 == 0)
				System.out.println(counter + " data rows written");

			if (counter == 20000) {
				arffWriter.write(data.toString());
				arffWriter.close();
//				IncrementalClassifier.classify(data);
				System.exit(0);
			}

			sub = s.substring(s.indexOf(":\":") + 4);
			
			// Takes out category from json file
			category = sub.substring(0, sub.indexOf("\""));

			/*
			 * Extracts each noun word from the line and updates the word map
			 * accordingly. So, for each category there will be only 3 words
			 * which will be 1 others will be 0
			 */
			try {
				sub = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
				if (sub.length() > 0) {
					words = sub.split(",");
					for (int i = 0; i < words.length; i++) {
						word = words[i].substring(words[i].indexOf("\"") + 1,
								words[i].lastIndexOf("\""));
						if(wordStatus.indexOf(word) < vals.length && word.length() > 1)
							vals[wordStatus.indexOf(word)] = attributeType
								.indexOf("1");
					}
					vals[vals.length-1] = categoryMap.get(category)-1;					
					data.add(new Instance(1.0, vals));
					counter++;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		br1.close();
		br2.close();
	}

	public static void main(String[] args) throws Exception {

		File f = new File(
				"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\pos.json");

		File arff = new File(
				"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\reviewtipscategory.arff");

		BufferedWriter arffWriter = new BufferedWriter(new FileWriter(arff));
		BufferedWriter csvWriter = new BufferedWriter(
				new FileWriter(
						new File(
								"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\categoryMap.csv")));

		ArffFileCreator fc = new ArffFileCreator();
		fc.fileCreator(f, arff, arffWriter, csvWriter);
		System.out.println("Done!!");
		arffWriter.close();
		csvWriter.close();
	}
}
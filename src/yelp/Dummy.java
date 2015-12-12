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
import java.util.Map;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class Dummy {

	public void fileCreator(File f, File arff, BufferedWriter arffWriter,
			BufferedWriter csvWriter) throws Exception {

		FastVector attributes, attributeType, categoryT;
		Instances data;

		String s = "";
		ArrayList<String> wordStatus = new ArrayList<String>();
		ArrayList<String> wordList = new ArrayList<String>();
		HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
		HashMap<String, Integer> categoryMap = new HashMap<String, Integer>();
		HashMap<Integer, Integer> rowCount = new HashMap<Integer, Integer>();
		HashMap<String, Integer> reviewCount = new HashMap<String, Integer>();
		String[] words = new String[3];
		String word, sub, sub1, category;
		int readCounter = 0, categoryCount = 0, writeCounter = 0, allin = 1, instanceCounter = 0;

		attributes = new FastVector();
		attributeType = new FastVector();
		attributeType.addElement("0");
		attributeType.addElement("1");

		/*
		 * Counting the number of reviews for each unique category. Since the
		 * number of reviews for each category range from 1 to 21000, we want to
		 * ensure that to create a good training model, categories with review
		 * count more than 100 should be considered. This will help in
		 * preventing outliers.
		 */
		BufferedReader br00 = new BufferedReader(new FileReader(f));
		while ((s = br00.readLine()) != null) {
			sub = s.substring(s.indexOf(":\":") + 4);

			// Takes out category from json file
			category = sub.substring(0, sub.indexOf("\""));

			if (!reviewCount.containsKey(category))
				reviewCount.put(category, 1);
			else {
				int val = reviewCount.get(category);
				val++;
				reviewCount.put(category, val);
			}
		}
		br00.close();

		/*
		 * In this part of code, we calculate the frequency of word in the
		 * review set. If a word has occurred in 15 or more reviews and is a
		 * part of category with more than 100 reviews, we consider it.
		 * Otherwise it is ignored. This ensures reduced feature space for
		 * creating an efficient model.
		 */
		BufferedReader br01 = new BufferedReader(new FileReader(f));
		while ((s = br01.readLine()) != null) {
			sub = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
			sub1 = s.substring(s.indexOf(":\":") + 4);
			category = sub1.substring(0, sub1.indexOf("\""));
			if (reviewCount.get(category) >= 100) {
				if (sub.length() > 0) {
					words = sub.split(",");
					for (int i = 0; i < words.length; i++) {
						word = words[i].substring(words[i].indexOf("\"") + 1,
								words[i].lastIndexOf("\""));
						if (!wordCount.containsKey(word) && word.length() > 1) {
							wordCount.put(word, 1);
						} else if (wordCount.containsKey(word)
								&& word.length() > 1) {
							int val = wordCount.get(word);
							val++;
							wordCount.put(word, val);
						}
					}
				}
			}
		}
		br01.close();
		
//		for (Map.Entry<String, Integer> entry : wordCount.entrySet())
//			System.out.println(entry.getKey() + "," + entry.getValue());
//
//		System.exit(0);

		/*
		 * Taking out words, which are our features for training model, from the
		 * pos file and adding them as attributes in the arff file. Now since
		 * there were 11137 unique words to start with, we reduced the feature
		 * space by including words which have occurred in 15 or more reviews.
		 * This brought the count down to 2216 attributes for 11400 data rows.
		 */
		BufferedReader br1 = new BufferedReader(new FileReader(f));
		while ((s = br1.readLine()) != null) {
			sub = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
			sub1 = s.substring(s.indexOf(":\":") + 4);
			category = sub1.substring(0, sub1.indexOf("\""));
			if (reviewCount.get(category) >= 100) {
				if (sub.length() > 0) {
					words = sub.split(",");
					for (int i = 0; i < words.length; i++) {
						word = words[i].substring(words[i].indexOf("\"") + 1,
								words[i].lastIndexOf("\""));
						if (!wordStatus.contains(word) && wordCount.containsKey(word) && wordCount.get(word) >= 15) {
							wordStatus.add(word);
							attributes.addElement(new Attribute(word,
									attributeType));
						}
					}
				}
			}
		}
		br1.close();

		// for (Map.Entry<String, Integer> entry : wordCount.entrySet())
		// System.out.println(entry.getKey() + "," + entry.getValue());
		//
		// System.exit(0);

		System.out.println("There are " + wordStatus.size() + " attributes!!");

		/*
		 * This part of the code is responsible for creating a category map.
		 * Since our model takes in nominal values for categories, we mapped
		 * each category to a number.
		 */
		BufferedReader br2 = new BufferedReader(new FileReader(f));
		categoryT = new FastVector();

		while ((s = br2.readLine()) != null) {
			sub = s.substring(s.indexOf(":\":") + 4);
			category = sub.substring(0, sub.indexOf("\""));

			/*
			 * Checks if a category is present in category map. If yes, then it
			 * doesn't put it again otherwise it does.
			 */
			if (!categoryMap.containsKey(category)
					&& reviewCount.get(category) >= 250) {
				categoryCount++;
				categoryMap.put(category, categoryCount);
				rowCount.put(categoryCount, 0);
				csvWriter.write(category + "," + categoryCount + "\n");
				categoryT.addElement(Integer.toString(categoryCount));
			}
		}
		attributes.addElement(new Attribute("category", categoryT));
		csvWriter.close();
		br2.close();		

		System.out.println("There are " + categoryMap.size() + " categories!!");

		data = new Instances("TipsReviewsCategories", attributes, 0);

		System.out.println("Populating file....");

		BufferedReader br3 = new BufferedReader(new FileReader(f));

		while ((s = br3.readLine()) != null) {

			double[] vals = new double[data.numAttributes()];

			for (int i = 0; i < vals.length; i++)
				vals[i] = attributeType.indexOf("0");

			readCounter++;
			
			if(readCounter % 10000 == 0)
				System.out.println(readCounter+" data rows read!!");

			sub = s.substring(s.indexOf(":\":") + 4);

			// Takes out category from json file
			category = sub.substring(0, sub.indexOf("\""));

			if (categoryMap.containsKey(category)) {

				/*
				 * Checks if the number of rows for a category exceeds 50 or
				 * not. If it does, it skips the row. Otherwise, it adds 1 to
				 * the rowCount map and writeCounter.
				 */
				if (rowCount.get(categoryMap.get(category)) >= 250)
					continue;
				else {
					int val = rowCount.get(categoryMap.get(category));
					val++;
					rowCount.put(categoryMap.get(category), val);
					writeCounter++;
				}

				// Just to check how many rows have been added to file
				if (writeCounter % 1000 == 0)
					System.out.println(writeCounter + " data rows written");

				/*
				 * Extracts each noun word from the line and updates the word
				 * map accordingly. So, for each category there will be only 3
				 * words which will be 1 others will be 0
				 */
				try {
					sub = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
					if (sub.length() > 0) {
						words = sub.split(",");
						for (int i = 0; i < words.length; i++) {
							word = words[i].substring(
									words[i].indexOf("\"") + 1,
									words[i].lastIndexOf("\""));
							if (wordCount.containsKey(word) && wordCount.get(word) < 15) {
								allin = 0;
								continue;
							}
							if (wordStatus.indexOf(word) < vals.length
									&& word.length() > 1)
								vals[wordStatus.indexOf(word)] = attributeType
										.indexOf("1");
						}
						vals[vals.length - 1] = categoryMap.get(category) - 1;
						if (allin == 1) {
							instanceCounter++;
							data.add(new Instance(1.0, vals));
						}							
						else
							allin = 1;
					}
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(instanceCounter+" instances recorded!!");
		
		arffWriter.write(data.toString());		
		br3.close();
	}

	public static void main(String[] args) throws Exception {

		File f = new File(
				"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\pos1.json");

		File arff = new File(
				"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\reviewtipscategory.arff");

		BufferedWriter arffWriter = new BufferedWriter(new FileWriter(arff));
		BufferedWriter csvWriter = new BufferedWriter(
				new FileWriter(
						new File(
								"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\categoryMap.csv")));

		Dummy fc = new Dummy();
		fc.fileCreator(f, arff, arffWriter, csvWriter);
		System.out.println("Done!!");
		arffWriter.close();
		csvWriter.close();
	}
}
package yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * 
 * @author Amritanshu Joshi
 *
 */
public class ArffFileCreator {

	/**
	 * 
	 * @param pos
	 *            The pos json file which helps in creating the arff file.
	 * @param arffWriter
	 *            The writer for the arff file which helps in creating the model
	 *            and evaluate it.
	 * @param csvWriter
	 *            The writer for category map file
	 * @throws Exception
	 */
	public void fileCreator(File pos, BufferedWriter arffWriter,
			BufferedWriter csvWriter) throws Exception {

		/*
		 * FastVector type is used to create an arff file. It is used for
		 * creating the attributes of the file.
		 */
		FastVector attributes, attributeType, categoryT;
		Instances data;

		String s = "";
		HashMap<String, HashMap<String, Integer>> initialWordCategoryMap = new HashMap<String, HashMap<String, Integer>>();
		String[] words = new String[3];
		String word, sub, sub1, category;
		int categoryCount = 0, instanceCounter = 0;

		attributes = new FastVector();
		attributeType = new FastVector();
		attributeType.addElement("0");
		attributeType.addElement("1");

		/*
		 * For each unique category from the POS json file, the associated words
		 * are kept in a hashmap. This is the initial parsing of the json file.
		 * The words are kept along with the count for each word in the json
		 * file.
		 */
		BufferedReader br1 = new BufferedReader(new FileReader(pos));
		while ((s = br1.readLine()) != null) {
			sub = s.substring(s.indexOf(":\":") + 4);
			// Takes out category from json file
			category = sub.substring(0, sub.indexOf("\""));

			sub1 = s.substring(s.indexOf("[") + 1, s.indexOf("]"));

			if (sub1.length() > 0) {
				words = sub1.split(",");
			}

			if (!initialWordCategoryMap.containsKey(category)) {
				HashMap<String, Integer> t = new HashMap<String, Integer>();
				for (int i = 0; i < words.length; i++) {
					word = words[i].substring(words[i].indexOf("\"") + 1,
							words[i].lastIndexOf("\""));
					t.put(word, 1);
				}
				initialWordCategoryMap.put(category, t);
			} else {
				HashMap<String, Integer> t = new HashMap<String, Integer>();
				t = initialWordCategoryMap.get(category);
				for (int i = 0; i < words.length; i++) {
					word = words[i].substring(words[i].indexOf("\"") + 1,
							words[i].lastIndexOf("\""));
					if (t.containsKey(word)) {
						int val = t.get(word);
						val++;
						t.put(word, val);
					} else
						t.put(word, 1);
				}
			}
		}
		br1.close();

		/*
		 * The hashmap created above is sorted according to the frequency of
		 * each word recorded in the hashmap.
		 */
		for (Map.Entry<String, HashMap<String, Integer>> entry : initialWordCategoryMap
				.entrySet()) {
			String c = entry.getKey();
			HashMap<String, Integer> y = entry.getValue();
			y = sortByValues(y);
			initialWordCategoryMap.put(c, y);
		}

		/*
		 * Now from the sorted map, top n words are extracted and put into
		 * another hashmap to be extracted later.
		 */
		HashMap<String, ArrayList<String>> finalWordCategoryMap = new HashMap<String, ArrayList<String>>();
		for (Map.Entry<String, HashMap<String, Integer>> entry : initialWordCategoryMap
				.entrySet()) {
			String c = entry.getKey();
			HashMap<String, Integer> x = entry.getValue();
			ArrayList<String> al = new ArrayList<String>();
			int i = 0;
			for (Map.Entry<String, Integer> entryx : x.entrySet()) {
				if (i == 2)
					break;
				al.add(entryx.getKey());
				i++;
			}

			finalWordCategoryMap.put(c, al);
		}

		ArrayList<String> categoryList = new ArrayList<String>();
		ArrayList<String> vocabulary = new ArrayList<String>();

		/*
		 * The vocabulary list(features) are created next which will be the list
		 * of features for the final arff file.
		 */
		for (Map.Entry<String, ArrayList<String>> entry : finalWordCategoryMap
				.entrySet()) {
			categoryList.add(entry.getKey());
			for (String voc : entry.getValue()) {
				if (!vocabulary.contains(voc) && voc.length() > 2) {
					vocabulary.add(voc);
					attributes.addElement(new Attribute(voc, attributeType));
				}
			}
		}

		System.out.println("There are " + vocabulary.size() + " attributes");

		categoryT = new FastVector();
		HashMap<String, Integer> catMapping = new HashMap<String, Integer>();

		for (String c : categoryList) {
			categoryCount++;
			csvWriter.write(c + "," + categoryCount + "\n");
			catMapping.put(c, categoryCount);
			categoryT.addElement(Integer.toString(categoryCount));
		}
		attributes.addElement(new Attribute("category", categoryT));
		csvWriter.close();

		System.out.println("There are " + categoryList.size() + " categories");

		data = new Instances("TipsReviewsCategories", attributes, 0);

		System.out.println("Populating file....");

		/*
		 * The pos json is read again and this time the data rows are generated
		 * using the vocabulary list and the file. These data instances will be
		 * the ones which will train the model for our evaluation.
		 */
		BufferedReader br2 = new BufferedReader(new FileReader(pos));
		while ((s = br2.readLine()) != null) {

			double[] vals = new double[data.numAttributes()];

			for (int i = 0; i < vals.length; i++)
				vals[i] = attributeType.indexOf("0");

			sub = s.substring(s.indexOf(":\":") + 4);

			// Takes out category from json file
			category = sub.substring(0, sub.indexOf("\""));

			if (categoryList.contains(category)) {
				try {
					sub = s.substring(s.indexOf("[") + 1, s.indexOf("]"));
					if (sub.length() > 0) {
						words = sub.split(",");
						for (int i = 0; i < words.length; i++) {
							word = words[i].substring(
									words[i].indexOf("\"") + 1,
									words[i].lastIndexOf("\""));
							if (vocabulary.contains(word)) {
								if (vocabulary.indexOf(word) < vals.length)
									vals[vocabulary.indexOf(word)] = attributeType
											.indexOf("1");
							}
						}
						vals[vals.length - 1] = catMapping.get(category) - 1;
						int flag = 0;
						for (int i = 0; i < vals.length; i++) {
							if (vals[i] != 0)
								flag++;
						}
						if (flag >= 2) {
							instanceCounter++;
							data.add(new Instance(1.0, vals));
						}
					}
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(instanceCounter + " instances recorded!!");

		arffWriter.write(data.toString());
		br2.close();
	}

	/**
	 * This function sorts an input hashmap based on its values.
	 * 
	 * @param noun
	 *            : the hashmap which is to be sorted.
	 * @return returns the sorted hashmap
	 */
	public HashMap<String, Integer> sortByValues(HashMap<String, Integer> noun) {
		List<Object> list = new LinkedList<Object>(noun.entrySet());

		Collections.sort(list, new Comparator<Object>() {
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

	/**
	 * This function creates files and calls the fileCreator(...) function to
	 * create files.
	 * 
	 * @throws Exception
	 */
	public void arffFileCreator() throws Exception {

		File pos = new File(Constants.POS_FILE_PATH);
		File arff = new File(Constants.ARFF_FILE_PATH);
		File csv = new File(Constants.CATEGORY_MAP_PATH);

		BufferedWriter arffWriter = new BufferedWriter(new FileWriter(arff));
		BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csv));

		ArffFileCreator fc = new ArffFileCreator();
		fc.fileCreator(pos, arffWriter, csvWriter);

		arffWriter.close();
		csvWriter.close();
	}
}
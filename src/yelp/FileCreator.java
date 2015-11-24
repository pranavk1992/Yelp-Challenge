package yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class FileCreator {

	public void trainCreator(String row) {

	}

	public void testCreator(String row) {

	}

	public void rowSelector(File f, BufferedWriter trW, BufferedWriter teW)
			throws IOException {
		
		BufferedReader br1 = new BufferedReader(new FileReader(f));

		String s = "";
		int rows = 0;

		while ((s = br1.readLine()) != null) {
			rows++;
		}

		int trainSize = (int) (0.60 * rows);
		int testSize = rows - trainSize;

		int trainCount = 0, testCount = 0;
		
		BufferedReader br2 = new BufferedReader(new FileReader(f));
		ArrayList<String> nounHeaders = new ArrayList<String>();
		String[] words = new String[3];
		String word;
		
		while ((s = br2.readLine()) != null) {
			words = s.substring(s.indexOf("[")+1, s.indexOf("]")).split(",");
			for(int i=0; i<3; i++) {
				word = words[i].substring(words[i].indexOf("\"")+1, words[i].lastIndexOf("\""));
				System.out.print(word+", ");
				if(!nounHeaders.contains(word))
					nounHeaders.add(word);
			}
			System.out.println();
		}
		
		System.out.println(nounHeaders.size());
	}

	public int coinToss() {
		return (int) (new Random().nextInt(2));
	}

	public static void main(String[] args) throws IOException {

		File f = new File(
				"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\pos.json");
		BufferedWriter trainWriter = new BufferedWriter(
				new FileWriter(
						new File(
								"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\train.csv")));
		BufferedWriter testWriter = new BufferedWriter(new FileWriter(new File(
				"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\test.csv")));

		FileCreator fc = new FileCreator();
		fc.rowSelector(f, trainWriter, testWriter);
		trainWriter.close();
		testWriter.close();
	}

}

package yelp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 * This example trains NaiveBayes incrementally on data obtained from the
 * ArffLoader.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class IncrementalClassifier {

	/**
	 * Expects an ARFF file as first argument (class attribute is assumed to be
	 * the last attribute).
	 *
	 * @param args
	 *            the commandline arguments
	 * @throws Exception
	 *             if something goes wrong
	 */
	public static void classify(Instances data) throws Exception {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\Classification.txt")));
		// load data
		ArffLoader loader = new ArffLoader();
		loader.setFile(new File(
				"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\reviewtipscategory.arff"));
		Instances structure = loader.getStructure();
		structure.setClassIndex(structure.numAttributes() - 1);

		System.out.println("Structure Loaded!! Started Training....");

		// train NaiveBayes
		NaiveBayesUpdateable nb = new NaiveBayesUpdateable();
		nb.buildClassifier(structure);
		Instance current;
		while ((current = loader.getNextInstance(structure)) != null)
			nb.updateClassifier(current);
		
		evaluate(nb, data);

		 System.out.println("Trained!! " + "Started Writing....");
		// output generated model
		// System.out.println(nb);
		 bw.write(nb.toString());
		 bw.close();

	}

	public static void evaluate(NaiveBayesUpdateable nb, Instances data)
			throws Exception {

		System.out.println("Evaluating...");
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				"D:\\Lectures\\Fall 15\\Search\\ProjectFiles\\Evaluation.txt")));
		data.setClassIndex(data.numAttributes()-1);
		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(nb, data, 10, new Random(1));
		System.out.println("Writing Evaluation...");
		System.out.println(eval.toSummaryString("\nResults\n=====\n", true));
		bw.write(eval.fMeasure(1) + " " + eval.precision(1) + " "
				+ eval.recall(1));
		bw.close();

	}
}
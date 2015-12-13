Yelp Dataset Challenge
============

Project work for ILS-Z534(Information Retrieval) Course.

## Team

* [Amritanshu Joshi](https://github.com/amritanshujoshi) (amrijosh@indiana.edu)
* Dhriti Katanguri 
* Jae Eun Kun 
* Pranav Kulkarni 
* Vishaka Brij

## Description

The project comprised of 2 tasks:

* Task 1: **Categorize** business based on text from reviews and tips.
* Task 2: **Extract sentiment** from reviews and tips for each business and find out the **reason** for the sentiment.

This project was made using Core Java, MongoDB and Weka was used for evaluation of Machine Language models.


### Code Instructions

The entire project is divided into two parts for the two tasks achieved in this project. The code for task 1 can be run by executing the **Task1** java file. It'll run the following files in order:

* **MongoIndexGenerator**: Written by **Anudhriti Katanguri**. This java code aims at creating an index for the data from the Yelp dataset. It creates a collection using reviews, tips, business id and category from the various json files provided by Yelp.
* **POSJSONCreator**: Written by **Pranav Kulkarni**. This code reads the mongoDB index and collection and creates a pos.json file in src/yelp/files folder. This file contains all the businesses with respective categories and top 3 nouns from the review+text data for that business. Since this code uses POS Tagging to get nouns from the data, this code may take a large amount of time to complete.
* **ArffFileCreator**: Written by **Amritanshu Joshi**. This code parses the pos.json file to create an Atribute Relation File Format(arff) file to be read in by WEKA and evaluated. The top 3 nouns from each category are collected as features and each business is written as a data instance for this file. The resulting file sits in src/yelp/files folder and is named reviewtipscategory.arff. This file is then used in WEKA explorer to train the model and evaluate the algorithm.

For task 2 the **Task2** java file is run and it flows in the following manner:

* **SentimentAnalysis**: Written by **Vishaka Brij**. This code reads in a specially created json file from the mongoDB index, named threecount.json from src/yelp/files folder. A sentiment analysis is performed on this file and a sentiment.json file is created which contains either a very positive or very negative sentiment.
* **Reason**: Written by **Pranav Kulkarni**. This code uses the sentiment.json file from the above step and finds out the reason for a sentiment. For eg. it generates output like "Delicious Pizza" or "Bad Service" which tells the user the reason for a sentiment. This sentiment is output to a file called reason_output.json win the src/yelp/files folder.

The evaluation for task 1 was carried out by **Jae Eun Kum** using the WEKA Explorer interface. A java file by the name of **Classifier** was also written by Jae which could carry out that operation of classification and evaluation in the code.

**Results** java file was written by **Anudhriti Katanguri** to find out if the indexes have properly been made in MongoDB.

**Constants** java file was written by **Amritanshu Joshi** to hold the path strings for all the files used or created in the code.

package edu.asu.irs13;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class SearchFiles {

	public static void main(String args[]) throws Exception{

		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		String str1 = "";
		long read1=System.currentTimeMillis();

		/************* Reading Preprocessed IDF from idf.txt******************/

		BufferedReader idfLog;
		idfLog = new BufferedReader (new FileReader ("idf.txt"));
		String dataRow2 = idfLog.readLine();

		//Initializing HashMap to retrieve idfLogMap (This HashMap will contain IDF values which will be used in project).
		HashMap<String,Double> idfLogMap=new HashMap<String,Double>();

		//Entering key as document number and value as IDF after Log (25054/doc frequency of term).
		while (dataRow2 != null){
			String[] dataArray2 = dataRow2.split(" ");

			//Contains terms and their IDF values.
			idfLogMap.put(dataArray2[0],Double.parseDouble(dataArray2[1]));

			// Read next line of data.
			dataRow2 = idfLog.readLine(); 
		}

		//Closing write operation to confirm read operation.
		idfLog.close();

		/**************** Reading from preprocessed document modulus of TF from documentModulusTf.txt***********/
		BufferedReader readModulusTf;
		readModulusTf = new BufferedReader (new FileReader ("documentModulusTf.txt"));
		String dataRow1 = readModulusTf.readLine();

		//Initializing HashMap to retrieve documentModulus(This has all the values of document modulus TF).
		HashMap<Integer,Double> documentModulusTf=new HashMap<Integer,Double>();

		//Entering key as document number and value as document modulus (1/|d|).
		while (dataRow1 != null){
			String[] dataArray1 = dataRow1.split(" ");

			//Contains square-root of document modulus in that is read from file documentModulus.txt
			documentModulusTf.put(Integer.parseInt(dataArray1[0]),Double.parseDouble(dataArray1[1]));

			// Read next line of data.
			dataRow1 = readModulusTf.readLine(); 
		}

		//Closing write operation to confirm read operation.
		readModulusTf.close();

		/**************** Reading from preprocessed document modulus of IDF from documentModulusIdf.txt***********/
		Term te = new Term("contents", "title");
		
		// You can also quickly find out the number of documents that have term t
		System.out.println("Number of documents with the word 'brute' is: " + r.docFreq(te));
		//System.out.println(idfLogMap.get("html"));
		
		BufferedReader readModulusIdf;
		readModulusIdf = new BufferedReader (new FileReader ("documentModulusIdf.txt"));
		String dataRow11 = readModulusIdf.readLine();

		//Initializing HashMap to retrieve documentModulus.
		HashMap<Integer,Double> documentModulusIdf=new HashMap<Integer,Double>();

		//Entering key as document number and value as document modulus (1/|d|).
		while (dataRow11 != null){
			String[] dataArray1 = dataRow11.split(" ");

			//Contains square-root of document modulus in that is read from file documentModulus.txt
			documentModulusIdf.put(Integer.parseInt(dataArray1[0]),Double.parseDouble(dataArray1[1]));

			// Read next line of data.
			dataRow11 = readModulusIdf.readLine(); 
		}
		long read2=System.currentTimeMillis();
		System.out.println("Time taken to read: "+(read2-read1)+"milliseconds ");
		//Closing write operation to confirm read operation.
		readModulusIdf.close();

		//Snippet implemented from Sample Code given for the project.
		Scanner sc = new Scanner(System.in);
		String str = "";
		System.out.print("query> ");

		while(!(str = sc.nextLine()).equals("quit")){
			str1="";

			//User input choice of method between TF and TF-IDF technique.
			Scanner sc1 = new Scanner(System.in);
			System.out.print("Press 1 for TF and 2 for TF-IDF> ");
			str1=sc1.nextLine();

			// Start of computation time for query. 
			long startQuery=System.currentTimeMillis();

			/*********Counting occurance of a term in query*********/

			//Initial HashMap namely termMap to store number of occurrences of each term from query.
			HashMap<String,Integer> termMap=new HashMap<String,Integer>();
			String[] terms = str.split("\\s+");
			for(String word : terms){	

				//entering value 1 for the first occurrence of term from query.
				if(termMap.containsKey(word)==false){
					termMap.put(word, 1);
				}
				else{

					//value of term in query increments after every subsequent occurrence.
					termMap.put(word, (termMap.get(word)+1));
				}
			}
			/********Calculating |q| *********/

			//Calculating Square of term frequency of all term in query i.e. (t1)^2+(t2)^2....
			double querySquare=0;
			for(Map.Entry<String, Integer> querySquareIterator: termMap.entrySet()){
				int eachQueryTermFrequency=querySquareIterator.getValue();

				//query square without dividing by querymax i.e. max term frequency of a term in query corpus.
				querySquare+=Math.pow(((double)eachQueryTermFrequency), 2);			

			}

			//Calculating 1/sq. root of querySquare to obtain 1/|q|. 
			double queryModulus=(1/Math.sqrt(querySquare));
			HashMap<Integer,Double> dotProductMap=new HashMap<Integer,Double>();

			for(Map.Entry<String, Integer> termFrequencyIterator: termMap.entrySet()){
				String wordTerm=termFrequencyIterator.getKey();
				int wordFrequency=termFrequencyIterator.getValue();
				Term term = new Term("contents", wordTerm);
				TermDocs tdocs = r.termDocs(term);
				//for each document having a term ti.
				while(tdocs.next()){
					double dotProduct = 0;

					/*
					 * Calculating with only TF.
					 */
					if(str1.equals("1")){

						//calculating without dividing by max frequency of term per document and max term frequency per query.(ti*di)
						dotProduct=((double)wordFrequency)*((double)(tdocs.freq()));
					}

					/*
					 * Calculating With TF-IDF
					 */
					else if(str1.equals("2")){
						//Calculating without max term frequency from query and max term frequency from document.(t*d*idf)
						dotProduct=((double)wordFrequency)*((double)(tdocs.freq()))*(idfLogMap.get(wordTerm));
					}
					else{
						System.out.println("wrong choice");
						System.exit(0);
					}
					if((dotProduct!=(double)0)){
						if(dotProductMap.containsKey(tdocs.doc())==false){
							dotProductMap.put(tdocs.doc(), (double)0);
						}

						/*
						 * After all dot product(q*d) of one term is calculated for all documents then dot product
						 *of next term is calculated for all documents and is appended to existing dot product(q *d) of
						 *the previously calculated terms.
						 */
						dotProductMap.put(tdocs.doc(), (dotProductMap.get(tdocs.doc())+dotProduct));
					}
				}
			}
			//Initializing HashMap for calculating final TF similarity.
			//dot product* query modulus*document modulus.
			HashMap<Integer,Double> similarityMap=new HashMap<Integer,Double>();
			for(Entry<Integer, Double> dotProductIterator: dotProductMap.entrySet()){
				/*
				 * In case of TF only
				 * 
				 */

				if(str1.equals("1")){

					similarityMap.put(dotProductIterator.getKey(),(dotProductIterator.getValue()*(queryModulus)*(documentModulusTf.get(dotProductIterator.getKey()))));			
				}
				/*
				 * In case of IDF only.
				 */
				else if(str1.equals("2")){
					similarityMap.put(dotProductIterator.getKey(),(dotProductIterator.getValue()*(queryModulus)*(documentModulusIdf.get(dotProductIterator.getKey()))));			
				}
			}
			read2=System.currentTimeMillis();
			System.out.println("Time taken to compute result: "+(read2-startQuery)+"milliseconds ");
			/*
			 * Using list with map inside and sorting in descending order using comparator.
			 */
			List<Entry<Integer, Double>> entries = new ArrayList<Entry<Integer, Double>>(similarityMap.entrySet());
			Collections.sort(entries, new Comparator<Entry<Integer, Double>>() {
				public int compare(Entry<Integer, Double> e1, Entry<Integer, Double> e2) {
					return e2.getValue().compareTo(e1.getValue()); // Sorts descending.
				}
			});

			// Put entries back in an ordered map.
			Map<Integer, Double> orderedMap = new LinkedHashMap<Integer, Double>();
			for (Entry<Integer, Double> entry : entries) {
				orderedMap.put(entry.getKey(), entry.getValue());
			}
			for(Entry<Integer, Double> sortIterator: orderedMap.entrySet()){
				//Displaying sorted Documents with document number.
				System.out.println("["+sortIterator.getKey()+"]");
			}
			long endQuery=System.currentTimeMillis();
			System.out.println("Query Sort Time: "+(endQuery-read2)+" milliseconds");

			System.out.println("Query Search Time: "+(endQuery-startQuery)+" milliseconds");
			System.out.println();
			System.out.print("Next query> ");
		}
	}
}
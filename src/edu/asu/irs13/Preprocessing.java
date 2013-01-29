/*
 * PreProcessing class to Pre-Compute MaxFrequency of each Document
 * and Pre-compute sum of Squares of all terms in a document in documentModulus 
 */

package edu.asu.irs13;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class Preprocessing {
	public static void main(String[] args) throws Exception
	{	//Preprocessing Start time
		long start=System.currentTimeMillis();
		//Index Reader to retrieve complete index in "r".
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		//Initialising HashMap for storing maximum frequency and document modulus from files.
		HashMap<Integer,Integer> maxFrequency= new HashMap<Integer,Integer>();
		HashMap<Integer,Double> documentModulus= new HashMap<Integer,Double>();
		// retrieving all terms from index.
		TermEnum t = r.terms();
		//t.next() gives next term
		while(t.next()){
			TermDocs td = r.termDocs(t.term());
			//gives next document of that term
			while(td.next()){
				/*
				 * calculating if max frequency already has the document id "or" if present the wether max frequent
				 *  of document is less than term frequency of new term.
				 */
				if((maxFrequency.containsKey(td.doc())==false) || (maxFrequency.get(td.doc())<td.freq())){
					// if yes then replace max with new term frequency
					maxFrequency.put(td.doc(), td.freq());
				}
			}
		}
		// retrieving all terms from index.
		TermEnum t1 = r.terms();
		//t.next() gives next term
		while(t1.next()){
			TermDocs td1 = r.termDocs(t1.term());
			//gives next document of that term
			while(td1.next()){
				/*
				 * calculating  Document Modulus , if not present
				 *  then initial it to 0 in double.
				 */
				if(documentModulus.containsKey(td1.doc())==false){
					documentModulus.put(td1.doc(), (double) 0);
				}
				/*document modulus contains square of (termDoc value)/(maxTerm Value) of that instance.
				 * 
				 */
				documentModulus.put(td1.doc(), (documentModulus.get(td1.doc())+Math.pow(((double)(td1.freq()))/((double)maxFrequency.get(td1.doc())), 2)));
			}
		}
		/*
		 * write the computer hashmap of max frequency to text file maxFrequency.txt
		 * write the computer hashmap of document modulus to text file documentModulus.txt
		 */
		FileWriter maxFrequencyCSV;
		FileWriter documentModulusCSV;

		maxFrequencyCSV = new FileWriter("maxFrequency.txt");
		documentModulusCSV= new FileWriter("documentModulus.txt");
		/*
		 * Iterating for copying every key, value pair of hasmap.
		 */
		for(Map.Entry<Integer, Integer> maxEntry: maxFrequency.entrySet()){
			maxFrequencyCSV.write(maxEntry.getKey()+" "+maxEntry.getValue()+"\n");
		}
		/*
		 * Iterating for copying every key, value pair of hasmap.
		 */
		for(Entry<Integer, Double> maxEntry: documentModulus.entrySet()){
			documentModulusCSV.write(maxEntry.getKey()+" "+maxEntry.getValue()+"\n");
		}
		//close filewriter to finalize the copy action.
		maxFrequencyCSV.close();
		documentModulusCSV.close();
		//computing end time to check total preprocessing time.
		long end=System.currentTimeMillis();
		System.out.println("Time Taken in pre-processing: "+(end-start));

	}

}
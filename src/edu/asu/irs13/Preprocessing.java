/* PreProcessing class to Pre-Compute MaxFrequency of each Document
 * and Pre-compute sum of Squares of all terms in a document in documentModulus 
 */

package edu.asu.irs13;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class Preprocessing {
	public static void main(String[] args) throws Exception
	{	
		//Pre-processing Start time
		long start=System.currentTimeMillis();

		//Index Reader to retrieve complete index in "r".
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));

		/****************Calculating Inverse Document Frequency of each term**************/

		TermEnum t3 = r.terms();
		HashMap<String,Double> idf= new HashMap<String, Double>();
		while(t3.next())
		{
			String currentTerm=t3.term().text();
			Term te = new Term("contents",currentTerm );
			if(r.docFreq(te)!=0){
				idf.put(currentTerm, Math.log((double)25054/(double)r.docFreq(te)));
			}
		}

		/*
		 * Iterating for copying every key, value pair of HashMap to write IDF.
		 */
		FileWriter idfTXT;
		idfTXT = new FileWriter("idf.txt");

		for(Entry<String, Double> idfIterator: idf.entrySet()){
			idfTXT.write(idfIterator.getKey()+" "+idfIterator.getValue()+"\n");
		}

		//Close FileWriter to finalize the copy action.
		idfTXT.close();
		long end=System.currentTimeMillis();
		System.out.println("Time Taken in pre-processing IDF \nof all terms + time for File write operation: "+(end-start)+" milliseconds");

		/********************Calculating Document Modulus for TF  ************************************************************/

		start=System.currentTimeMillis();
		HashMap<Integer,Double> documentModulusTf= new HashMap<Integer,Double>();

		// Retrieving all terms from index.
		TermEnum t1 = r.terms();

		//t1.next() gives next term
		while(t1.next()){
			TermDocs td1 = r.termDocs(t1.term());

			//Gives next document of that term
			while(td1.next()){

				/*
				 * Calculating  Document Modulus , if not present
				 *  then initial it to 0 in double.
				 */
				if(documentModulusTf.containsKey(td1.doc())==false){
					documentModulusTf.put(td1.doc(), (double) 0);
				}
				/*
				 * Here we we append square of frequency of each term in document with the frequency square of other terms.
				 */
				documentModulusTf.put(td1.doc(), (documentModulusTf.get(td1.doc())+Math.pow(((double)(td1.freq())), 2)));
			}
		}
		FileWriter documentModulusTfTXT;
		documentModulusTfTXT= new FileWriter("documentModulusTf.txt");

		/*
		 * Iterating for copying every key, value pair of HashMmap.
		 * Here (1/(square root of document) is calculated and added to file.
		 */
		end=System.currentTimeMillis();
		System.out.println("\n\nTime Taken in pre-processing document modulus \nwith TF only + time for File write operation: "+(end-start)+" milliseconds");

		for(Entry<Integer, Double> modulusEntry: documentModulusTf.entrySet()){
			documentModulusTfTXT.write(modulusEntry.getKey()+" "+(1/(Math.sqrt(modulusEntry.getValue())))+"\n");
		}

		//Close FileWriter to finalize the copy action.
		documentModulusTfTXT.close();

		
		/********************Calculating Document Modulus for IDF************************************************************/
		TermEnum t = r.terms();
		Term te = new Term("contents", "body");
		
		// You can also quickly find out the number of documents that have term t
		System.out.println("Number of documents with the word 'brute' is: " + r.docFreq(te));
		start=System.currentTimeMillis();
		HashMap<Integer,Double> documentModulusIdf= new HashMap<Integer,Double>();

		// Retrieving all terms from index.
		TermEnum t11 = r.terms();

		//t11.next() gives next term
		while(t11.next()){
			TermDocs td11 = r.termDocs(t11.term());

			//Gives next document of that term
			while(td11.next()){

				/*
				 * Calculating  Document Modulus , if not present
				 *  then initial it to 0 in double.
				 */
				if(documentModulusIdf.containsKey(td11.doc())==false){
					documentModulusIdf.put(td11.doc(), (double) 0);
				}
				/*
				 * Here we we append square of frequency of each term * IDF of that term in document with the frequency square of other terms.
				 */				
				if(idf.get(t11.term().text()) != null){
					documentModulusIdf.put(td11.doc(), (documentModulusIdf.get(td11.doc())+Math.pow(((double)(td11.freq()*(idf.get(t11.term().text())))), 2)));

				}
				else{
					documentModulusIdf.put(td11.doc(), (documentModulusIdf.get(td11.doc())+Math.pow(((double)(td11.freq())), 2)));
				}
			}
		}
		end=System.currentTimeMillis();
		System.out.println("\n\nTime Taken in pre-processing document \nfor TF-IDF + time for File write operation: "+(end-start)+" milliseconds");

		FileWriter documentModulusIdfTXT;
		documentModulusIdfTXT= new FileWriter("documentModulusIdf.txt");

		/*
		 * Iterating for copying every key, value pair of HashMmap.
		 * Here (1/(square root of document) is calculated and added to file.
		 */
		for(Entry<Integer, Double> modulusEntry: documentModulusIdf.entrySet()){
			documentModulusIdfTXT.write(modulusEntry.getKey()+" "+(1/(Math.sqrt(modulusEntry.getValue())))+"\n");
		}
		//Close FileWriter to finalize the copy action.
		documentModulusIdfTXT.close();

		//Computing end time to check total pre-processing time.
			}
}
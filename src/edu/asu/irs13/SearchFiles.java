package edu.asu.irs13;

import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
//import org.apache.lucene.document.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class SearchFiles {

	public static void main(String args[]) throws Exception{
		//Index Reader to retrieve complete index in "r".
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
// buffered reader to read from maxFrequency.txt and documentModulus.txt
		BufferedReader readMaxFrequency;
		readMaxFrequency = new BufferedReader (new FileReader ("maxFrequency.txt"));
//
		String readDataByRow = readMaxFrequency.readLine();
		HashMap<Integer,Integer> maxFrequency=new HashMap<Integer,Integer>();
		while (readDataByRow != null){
			String[] dataArray = readDataByRow.split(" ");
			//System.out.print(dataArray[0] + "\t"+dataArray[1]); 


			//contains max frequency of document that is read from file maxFrequency.txt
			maxFrequency.put(Integer.parseInt(dataArray[0]),Integer.parseInt(dataArray[1]));
			//System.out.println();
			readDataByRow = readMaxFrequency.readLine(); // Read next line of data.
		}
		readMaxFrequency.close();	
		System.out.println("24870 "+maxFrequency.get(24870));

		BufferedReader readModulus;
		readModulus = new BufferedReader (new FileReader ("documentModulus.txt"));
		String dataRow1 = readModulus.readLine();
		HashMap<Integer,Double> documentModulus=new HashMap<Integer,Double>();
		while (dataRow1 != null){
			String[] dataArray1 = dataRow1.split(" ");
			//System.out.print(dataArray1[0] + "\t"+dataArray1[1]); 

			//contains square-root of document modulus in that is read from file documentModulus.txt
			documentModulus.put(Integer.parseInt(dataArray1[0]),Math.sqrt(Double.parseDouble(dataArray1[1])));
			//System.out.println();
			dataRow1 = readModulus.readLine(); // Read next line of data.
		}
		readMaxFrequency.close();	
		readModulus.close();
		//System.out.println("24870 "+maxFrequency.get(24870));
		//System.out.println("24870 "+documentModulus.get(24870));

		Scanner sc = new Scanner(System.in);
		String str = "";
		long startQuery=System.currentTimeMillis();
		System.out.print("query> ");
		HashMap<String,Integer> termMap=new HashMap<String,Integer>();
		//	ArrayList<String> array=new ArrayList<String>();
		while(!(str = sc.nextLine()).equals("quit")){
			String[] terms = str.split("\\s+");
			for(String word : terms){	

				if(termMap.containsKey(word)==false){
					//			array.add(word);
					termMap.put(word, 1);
				}
				else{
					termMap.put(word, (termMap.get(word)+1));
				}

			}
			System.out.println("the"+termMap.get("the"));
			System.out.println("g"+termMap.get("great"));
			int max=0;
			for(Map.Entry<String, Integer> maxEntry: termMap.entrySet()){
				int eachQueryTermFrequency=maxEntry.getValue();
				if(max<eachQueryTermFrequency){
					max=eachQueryTermFrequency;
				}
			}
			double querySquare=0;
			for(Map.Entry<String, Integer> maxEntry: termMap.entrySet()){
				int eachQueryTermFrequency=maxEntry.getValue();
				querySquare+=Math.pow(((double)eachQueryTermFrequency)/((double)max), 2);			
			}
			System.out.println("max "+max);
			double queryModulus=Math.sqrt(querySquare);
			System.out.println("queryModulus"+queryModulus);
			HashMap<Integer,Double> dotProductMap=new HashMap<Integer,Double>();

			for(Map.Entry<String, Integer> maxEntry: termMap.entrySet()){
				String wordTerm=maxEntry.getKey();
				int wordFrequency=maxEntry.getValue();
				Term term = new Term("contents", wordTerm);
				TermDocs tdocs = r.termDocs(term);
				while(tdocs.next())
				{

					double dotProduct=((double)wordFrequency/max)*((double)(tdocs.freq())/maxFrequency.get(tdocs.doc()));
					if((dotProduct!=(double)0)){
						if(dotProductMap.containsKey(tdocs.doc())==false){
							System.out.println("Dot Product"+dotProduct);

							dotProductMap.put(tdocs.doc(), (double)0);
						}
						dotProductMap.put(tdocs.doc(), (dotProductMap.get(tdocs.doc())+dotProduct));
					}
				}
			}
			HashMap<Integer,Double> similarityMap=new HashMap<Integer,Double>();

			for(Entry<Integer, Double> maxEntry: dotProductMap.entrySet()){
				similarityMap.put(maxEntry.getKey(),((maxEntry.getValue())*(1/(queryModulus))*(1/(documentModulus.get(maxEntry.getKey())))));			
			}
			for(Entry<Integer, Double> maxEntry: similarityMap.entrySet()){
				System.out.println("similarity of "+maxEntry.getKey()+" is "+maxEntry.getValue());
			}

			for(Entry<Integer, Double> maxEntry: sortIntegerMap(similarityMap).entrySet()){
				System.out.println(" correct similarity of "+maxEntry.getKey()+" is "+maxEntry.getValue());
			}

			for(Entry<Integer, Double> maxEntry: sortIntegerMap(similarityMap).entrySet()){
				String d_url = r.document(maxEntry.getKey()).getFieldable("path").stringValue().replace("%%", "/");
				System.out.println("["+maxEntry.getKey()+"] ["+maxEntry.getValue()+"] " + d_url);
				System.out.println("query> ");
			}
			long endQuery=System.currentTimeMillis();
			System.out.println("Query Run Time: "+(endQuery-startQuery));
			System.out.print("query> ");
		}
		long endProgram=System.currentTimeMillis();
		System.out.println("Program Run Time: "+(endProgram-StartProgram));
	}
	public static LinkedHashMap<Integer, Double> sortIntegerMap(Map<Integer, Double> unsortedMap){
		LinkedHashMap<Integer, Double> sortedMap=new LinkedHashMap<Integer, Double>();
		LinkedList<Double> valueList= new LinkedList<Double>(unsortedMap.values());
		Collections.sort(valueList,Collections.reverseOrder());
		for(int i=0;i<valueList.size();i++){
			for(Map.Entry<Integer, Double> map:unsortedMap.entrySet()){
				if(map.getValue()==valueList.get(i)){
					sortedMap.put(map.getKey(), map.getValue());
				}
			}
		}
		return sortedMap;
	}
}

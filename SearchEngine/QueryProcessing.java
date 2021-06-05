package SearchEngine;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.tartarus.snowball.ext.PorterStemmer;

import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.lang.Float;

import org.json.JSONObject;
import org.json.JSONArray;

public class QueryProcessing {
public static void main(String[] args) {
	
	
	String word="OBJECT";
	
	List<String>result=QueryProcess(word);
	for(int i=0;i<result.size();i++) {
		System.out.println(result.get(i));
	}
	
			
    }
	


		public static String stem(String word) {
			//remove any spaces in the word
			word= word.replaceAll("\\s", "");
			PorterStemmer stemedWord=new PorterStemmer();
			stemedWord.setCurrent(word);
			stemedWord.stem();
			System.out.println(stemedWord.getCurrent());
			return stemedWord.getCurrent().toLowerCase();
		}
		
		
		 public static List<String> QueryProcess(String word){
			//1- TODO: Stem the word
			 String StemmedWord=stem(word);
			 
			//2- TODO: configure connection with data base
			    String connectionString = "mongodb+srv://Noran:ci9L$h$Cp4_SVJr@cluster0.bktb5.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";
			    try(MongoClient client=MongoClients.create(connectionString)){

			//3- TODO: open data base
			    MongoDatabase db = client.getDatabase("test");
				MongoCollection<Document> invertedDoc = db.getCollection("invertedindex");
				MongoCollection<Document> crawlerDoc=db.getCollection("crawlerDocuments");
				
				
				
		  //4- TODO: Retrieve all the documents in the database that correspond to this stemmed word
		  //5- TODO: Calculate the priority of each document
				Document DBresult=invertedDoc.find(new Document("word_id",StemmedWord)).first();
				
				LinkedHashMap<String, Float> Documents = new LinkedHashMap<String, Float>();  //<key->url: value->priority>
				if(DBresult!=null) {
					JSONObject obj = new JSONObject(DBresult.toJson());
					JSONArray arr = obj.getJSONArray("doc");
					 
					//save the document:DF in LinkedHashMap 
					//get the word count of each document and calculate the Normalized TF *1000000 to help in sorting to nearest 6th dp
			        for (int i = 0; i < arr.length(); i++) {
			            String doc_id = arr.getJSONObject(i).getString("doc_id");
			            float DF = arr.getJSONObject(i).getInt("DF");
			            float DocWordCount=crawlerDoc.find(new Document("url",doc_id)).first().getInteger("wordCount");
			            float NormalizedTF=(DF/DocWordCount)*1000000;
			            Documents.put(doc_id,NormalizedTF);
			            System.out.println("doc_id: "+doc_id+"DF: "+DF+" wordCount: "+DocWordCount+"  NormalizedTF: "+NormalizedTF);
			        }
			 
		 }
				//6- TODO: Sort these documents in decreasing order of priority
		        //in order to know how to sort by value the linkedhashmap
		        //refer to  --> https://www.geeksforgeeks.org/java-program-to-sort-linkedhashmap-by-values/
		        List<Map.Entry<String, Float>>list=new ArrayList<Map.Entry<String,Float>>(Documents.entrySet());
		        
		        Collections.sort(list,new Comparator<Map.Entry<String, Float>>(){
		        		public int compare(Map.Entry<String, Float> doc1, Map.Entry<String, Float> doc2) {
		             	return Math.round(doc2.getValue()-doc1.getValue());}
		        		
			});
		        

		        //7- TODO: Finally take all the urls after being sorted by their priority in a list and return it
		        //neglecting any spammed page(normalized tf less than 50%)
				List<String> SortedUrls=new ArrayList<String>();
		        for (Map.Entry<String, Float> l : list) {
		        	if(l.getValue()<0.5*1000000) {SortedUrls.add(l.getKey());}
		        }

		      
		        
		        return SortedUrls;
		        
		        
		    	
			}
		 }	
}

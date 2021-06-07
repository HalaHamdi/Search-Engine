package SearchEngine;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.Reader;
import java.io.BufferedReader;

import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
import org.tartarus.snowball.ext.PorterStemmer;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.HashSet;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.model.UpdateOptions;


import static com.mongodb.client.model.Filters.*;


import static com.mongodb.client.model.Updates.*;

//Steps:
//Connect to the mongodb and get the mongo client
//for each document
//parse html
//get hyperlinks within this html
//tokenization
//remove stop words
//stem -> hashmap
//store in db


public class Indexer {
	
	//note: localpath at index i corresponds to the url at index i
	private static List<String> localPaths;
	private static List<String> urls;
	
	private static HashMap<String,HashMap<String,docContainer>> invIndex;
	
	//The uploading process is periodically done
	//In-order to distribute the load of the in-application memory and the # of transactions done to db
	//the count of documents afterwhich we'd upload the latest recently indexed docs to the database
	private static int uploadCount = 100;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		long start1 = System.nanoTime();
		
		try(MongoClient mongoClient = MongoClients.create("mongodb+srv://Noran:ci9L$h$Cp4_SVJr@cluster0.bktb5.mongodb.net/myFirstDatabase?retryWrites=true&w=majority")){
			MongoDatabase db = mongoClient.getDatabase("test");
			
			//get the crawled documents; their local paths and urls from the db
			getCrawledDocs(db);
	
			//The indexerDataStructure inside the java application
			invIndex = new HashMap<>();
			
			//read the stopwords from projects' attached files
			HashSet<String> stopWords = buildStopWords();
			
			int docsCount = localPaths.size();
			int lastUpdate = -1;
			for (int docNum=0;docNum<docsCount;docNum++) {
				
				String nohtmlText = extractTextFromHTML(new FileReader (localPaths.get(docNum)));
				//String nohtmlText = extractTextFromHTML(new FileReader ("./Downloads/ArduinoHome.html"));
				
				
				List<String> tokens = tokenize(nohtmlText);
				
				uploadTokenCountToDB(db, docNum, tokens.size());
				
				List<String> stopWordlessTokens = removeStopWords(tokens, stopWords);
				
				stemAndStore(stopWordlessTokens, docNum);
				
				//Time to upload the sofar indexed docs to the DB
				//TODO: To be implemented
			}
			
			//TODO: Checking to update the last batch of indexed docs so far
			long start2 = System.nanoTime();
			uploadToDB(db);
			long end = System.nanoTime();
			
			calcIDF(db,docsCount);
			
			
		}
		
	}
	
	public static void calcIDF(MongoDatabase db, int docsCount) {
		MongoCollection invColl = db.getCollection("invertedIndex");
		
		Consumer<Document> getTF = doc -> {
			double idf = java.lang.Math.log(docsCount / (double)doc.get("TF")); 
			Bson update = set("idf",idf);
			invColl.updateOne(doc, update);
		};
		
		//Applying that function for each document retrieved from the collection
		invColl.find().forEach(getTF);
	}
	
	public static void uploadTokenCountToDB(MongoDatabase db, int docNum, int tokenCount) {
		
		MongoCollection<Document>  docColl = db.getCollection("crawlerDocuments");
		Bson filter = eq("url",urls.get(docNum));
		Bson update = set ("wordCount",tokenCount);
		docColl.updateOne(filter, update);
	}
	
	public static void getCrawledDocs(MongoDatabase db){
		
		MongoCollection<Document> crawledDocCollection = db.getCollection("crawlerDocuments");
		localPaths = new ArrayList<>();
		urls = new ArrayList<>();
		//For a document: extract the value of its "localPath" and "url" and store them in their corresponding arrayList
		Consumer<Document> extractPath = doc -> {
			localPaths.add(doc.get("localPath").toString()); 
			urls.add(doc.get("url").toString());
		};
		
		//Applying that function for each document retrieved from the collection
		crawledDocCollection.find().forEach(extractPath);
		
	}
	
	public static String extractTextFromHTML(Reader reader) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    BufferedReader br = new BufferedReader(reader);
	    String line;
	    while ( (line=br.readLine()) != null) {
	    	sb.append(line);
	    }
	    
	    //had to specify the full class name, since conflict may occur with document found in BSON
	    org.jsoup.nodes.Document doc = Jsoup.parse(sb.toString());
	    String textOnly = doc.text();
	    
	    return textOnly;  
	    
	}
	
	public static List<String> tokenize(String unTokenizedText) throws IOException {
	    
		int strSize = unTokenizedText.length();
		StringBuilder extractedWord = new StringBuilder();
		
		List<String> tokens = new ArrayList<>();
		for(int i=0;i<strSize;i++) {
			
			char letter = unTokenizedText.charAt(i);
			
			if((letter >= 'a' && letter <= 'z') || (letter >= 'A' && letter <= 'Z') || (letter >= '0' && letter <= '9')) {
				i = extractToken(i,unTokenizedText,extractedWord);
				tokens.add(extractedWord.toString().toLowerCase());	
			}
		}
		
		return tokens; 
	}
	
	private static List<String> removeStopWords (List<String> withStopWords, HashSet<String> stopWords ){
		List<String> withoutStopWords = new ArrayList<String>();
		
		for(String word : withStopWords) {
			if(! stopWords.contains(word)) {
				withoutStopWords.add(word);
			}
		}
		return withoutStopWords;
	}
	
	private static int extractToken(int index, String str, StringBuilder extractedWord) {
		
		//extractToken starting from a given index in the str
		extractedWord.setLength(0);
		int maxLength = str.length();
		char letter = str.charAt(index);
		while(((letter >= 'a' && letter <= 'z') || (letter >= 'A' && letter <= 'Z') || (letter >= '0' && letter <= '9'))) { 
			
			extractedWord.append(letter);
			++index;
			if(index >= maxLength) { break; }
			letter = str.charAt(index);
		}
		return index-1;
	}

	public static void stemAndStore(List<String> tokens, int docNum) {
		PorterStemmer stemmer = new PorterStemmer();
		int tokenSize = tokens.size();
		
		for(int i = 0 ;i <tokenSize; i++) {
			
			//stem the token
			stemmer.setCurrent(tokens.get(i)); //set string you need to stem
			stemmer.stem();  //stem the word
			String stem = stemmer.getCurrent();//get the stemmed word
			
			//inserting the stemmed word and its position and the currently corresponding document in the hashmap DS
			HashMap<String,docContainer> docContainers = invIndex.get(stem);
			if(docContainers == null) {
				docContainers = new HashMap<String,docContainer>();
				invIndex.put(stem, docContainers);
			}
			docContainer docCont = docContainers.get(urls.get(docNum));
			if(docCont == null) {
				docCont = new docContainer();
				docContainers.put(urls.get(docNum), docCont);
			}
			docCont.appendToPositionList(i);	
			
		}	
	}
	
	public static HashSet<String> buildStopWords() throws FileNotFoundException, IOException{
		
		//get the stopword file path
		String stopwordFilePath = System.getProperty("user.dir") + "\\stopwords.txt";
		
		//read the stopwords' file
		BufferedReader br = new BufferedReader (new FileReader (stopwordFilePath));
		String stopWord;
		HashSet<String> stopWords = new HashSet<>();
		
		while((stopWord=br.readLine()) != null) {
			//for each word append it to the hashset
			stopWords.add(stopWord);
		}
		
		return stopWords;
	}
		
	public static void uploadToDB(MongoDatabase db) {
		MongoCollection<Document> invColl = db.getCollection("invertedindex");
		UpdateOptions options = new UpdateOptions().upsert(true);
		
		//upserting each word entry in the DB
		for(HashMap.Entry wordInApp : invIndex.entrySet()) {
			
			HashMap<String,docContainer> listOfDocumentInApp = (HashMap)wordInApp.getValue();
			List<Document> listOfDocumentInDB = new ArrayList<>();
			
			for(HashMap.Entry docInApp : listOfDocumentInApp.entrySet())
			{
				docContainer docInAppValue = (docContainer)docInApp.getValue();	
				Document docInDB = new Document("doc_id",docInApp.getKey()).
										append("DF", docInAppValue.getDF()).
										append("position", docInAppValue.getPositionList());
				listOfDocumentInDB.add(docInDB);
			}
			
			//the filter based on it we specify the word entry to be updated
			Bson filter = eq("word_id",wordInApp.getKey().toString());
			
			//the updates we want to apply on that to-be-updated word entry
			Bson update1 = inc("TF",listOfDocumentInApp.size());
			Bson update2 = push("doc",BasicDBObjectBuilder.start("$each", listOfDocumentInDB).get());
			Bson update3 = combine(update1,update2);
			invColl.updateOne(filter, update3, options);
		}	
	}
	
}
	






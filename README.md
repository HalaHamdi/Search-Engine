# Search-Engine
This is an Advanced Programming project handed to second year computer engineer students.

 <img src="https://github.com/Halahamdy22/Search-Engine/blob/master/Sniper1.png" width="300" alt="accessibility text">

```diff 
+ Crawler
```
---------------------------------
- The crawler must not visit the same URL more than once.
   - This include different URLs but the same content (Encoding content).
- The crawler can only crawl documents of specific types (HTML is sufficient for the project).
- The crawler must maintain its state so that it can, if interrupted, be started again to crawl the documents on the list without revisiting documents that have been previously downloaded.
   - interrupted by shutting down the laptop or the program --> DBMS.
- Provide a multithreaded crawler implementation where the user can control the number of threads before starting the crawler.
- Take Care of the choice of your seeds.
- When Crawler finishes one iteration by reaching stopping criteria, it restarts again, Frequency of crawling is an important part of a web crawler. Some sites will be visited more often than others. You have to set some criteria to the sites. In another words, during recrawl, you don’t have to repeat all the sites again.


```diff 
- Interface
```
---------------------------------
in this interface we use node js and ejs(embedded java script) as an interactive hosting mechanism.  
- Done
   - 2 HTML web pages
   - handling suggestions of words for a certain letters
   - backend for both
- Left
   - final connect between node js and html files(the .ejs fils dosen't define the globale variables)
   - logo


```diff 
! Indexer
```
---------------------------------
- Get the crawled document local paths from the DB
- Preprocess each crawled document, through:
   - Parse the document through it's content out of the html
   - Convert the content of the document into tokens, through removing punctuation and all symbols other than the english alphabet and numbers.
   - Calculate the tokens count and store it in the DB as the # of words contained within the current document
   - Filter the tokens through removing stop words from them
   - Stem each token using porter stemmer library
   - Store the stemmed token as a hash map key, where its value corresponds to the:
      - Hashmap
         -  its key is the document url containing that word
         -  its value is an object of type docContainer, which holds the DF of that token-doc relationship, also a list of the positions that token was mentioned in that doc 
- Upload the hashmap containing the stemmed words and its corresponding data into the DB
- Calculate the idf of each word then store and upload it onto the DB 


```diff 
- Query processor
```
---------------------------------

- The Query processing is what the backend of our page calls when the client requests pages of a certain word.
- In this project we only considered the client to search for a single word and not a phrase Search.
- The Query Processing stem the word the same way the indexer do , so relevant documents are retrieved.
- Then the Query Processing Sorts the documents in decreasing order of "Content relevant" and this is calculated using " Normalized Term Frequency" method.
- Not all the Documents in the MonogDB database are retrieved. The URLS that are recognized as spam are neglected.

# Search-Engine
This is an Advanced Programming project handed to second year computer engineer students.

## Crawler :
- The crawler must not visit the same URL more than once.
   - This include different URLs but the same content (Encoding content).
- The crawler can only crawl documents of specific types (HTML is sufficient for the project).
- The crawler must maintain its state so that it can, if interrupted, be started again to crawl the documents on the list without revisiting documents that have been previously downloaded.
   - interrupted by shutting down the laptop or the program --> DBMS.
- Some web administrators choose to exclude some pages from the search such as their web pages check for Robot.txt.
- Provide a multithreaded crawler implementation where the user can control the number of threads before starting the crawler.
- Take Care of the choice of your seeds.
- When Crawler finishes one iteration by reaching stopping criteria, it restarts again, Frequency of crawling is an important part of a web crawler. Some sites will be visited more often than others. You have to set some criteria to the sites. In another words, during recrawl, you don’t have to repeat all the sites again.
- No of Crawled pages is 5000 page (for the sake of the project).
- The crawler is independent program or process than the Indexer.

## Interface:
- Done
   - 2 HTML web pages 
   - handling suggestions of wordes for a certin letters 
   - backend for both
- Left
   - handling client server communication to get list of links as a result and list words as a suggestions
   - logo 
 
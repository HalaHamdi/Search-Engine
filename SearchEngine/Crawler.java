package SearchEngine;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.*;
//To Do
/* Robot Checker is responsible for check if the document is allowed to be downloaded*/
class robotCheck
{

}
/* This class is for file operations */
class SeedsFile {

    // TO DO ::  encoding the whole content of url then save (case of different links with the same page content).
    /*
     * data will be written to the file, saving the current state of the crawler
     */
    static File Seeds;
    static FileWriter fileWriter;
    static int Level = 6;            /* saving the count of the links crawled, initially with 6 since seed has 5 links  */
    static int current_line = 1;

    public SeedsFile()
    {
        FileCreate();
    }
    /* File creation :  static since all object from that class will deal with the same version*/
    private void FileCreate() {
        try {
            Seeds = new File("Seeds.txt");
            if (Seeds.createNewFile()) {
                System.out.println("\n Seeds file created successfully ..! ");
            } else {
                System.out.println("\n File is already existed");


            }
        }
        catch (IOException error) {
            System.out.println("\n An error occured while creating the file .. " + error.getMessage());
        }
    }
    String FileReader(int lineIndex) {
        String line ="";
        try
        {
            Stream<String> lines =  Files.lines(Paths.get(Seeds.getPath()));
            line  = lines.skip(current_line - 1).findFirst().orElse("");
        }
        catch (IOException error)
        {
            System.out.println("\n"+ error.getMessage());
        }


        return line;
    }
    /* File Writer Creation: locked in order not more than one thread open the file and writing at same location.*/
   synchronized void  FileWriter(String URL) {
       try{
           fileWriter = new FileWriter("Seeds.txt",true);   /* append here set true to avoid overlapping */
           fileWriter.write(URL + "\n");

       }
       catch(Exception error)
       { System.out.println(error.getMessage());}
       Level ++;

   }
   synchronized void setCurrentLine()
   {
       current_line ++;
   }
   void close(){
       try
       {
           if(fileWriter != null)
           {
               fileWriter.close();
           }
       }
       catch(Exception error)
       { System.out.println(error.getMessage());}
   }

}

/* Crawler */
public class Crawler implements Runnable {

    private static final int MAX_PAGES = 5000;                       /* Max number of pages can be downloaded.*/
    private static java.util.HashMap<String, Integer> LinksVisited = new java.util.HashMap<String, Integer>(); /* faster , key is the link
                                                                                                                  value is 01-->visited ,10 --> dynamic
                                                                                                                   ,11 --> dynamic and visited
                                                                                                               */
    private int ThreadID;
    SeedsFile seeds;

    public Crawler(int id, SeedsFile inputFile) {
        this.seeds = inputFile;
        this.ThreadID = id;
        System.out.println("\n Crawler with rank =  " + this.ThreadID + " created.");
    }

    @Override
    public void run() {

        /*this.seeds.FileWriter("www."+Thread.currentThread().getName()+".com");*/
        crawl(this.seeds.current_line);

    }

    private Document Request(String url) {
        try {
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();

            if (connection.response().statusCode() == 200) {
                System.out.println("\n *** crawler ID: " + this.ThreadID + "Received webpage at " + url);

                String title = document.title();
                System.out.println(title);
                return document;
            }

        } catch (IOException error) {

            System.out.println(error.getMessage());
        }
        return null;
    }

    private synchronized void crawl(int link) {
        while (true) {
            synchronized (this.seeds) {

                if (this.seeds.Level > MAX_PAGES) {
                    System.out.println("\n Crawling reached its maximum now, " + MAX_PAGES + "pages are avaliable now. ");
                } else {

                    String URL = this.seeds.FileReader(this.seeds.current_line);
                    if (URL.isEmpty()) break;
                    else {
                        this.seeds.setCurrentLine();
                        System.out.println("\n Thread num = " + Thread.currentThread().getName() + " now fetch link at line " + this.seeds.current_line + " link is " + URL);
                        if (Request(URL) != null) {

                        }

                    }


                }

            }
            try {
                Thread.sleep(5000);

            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + "is awaken");
            }

        }

    }
}
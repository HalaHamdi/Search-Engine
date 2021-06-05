package SearchEngine;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
/****************************************************************************/
/* Robot Checker is responsible for check if the document is allowed to be downloaded*/
class robotCheck {
    /* LinksVisited --> saving the host of the url and the disallows urls for this host */
    public static java.util.HashMap<String, ArrayList<String>> LinksVisited = new java.util.HashMap<String, ArrayList<String>>();
    /* string for the encoding content, int rank --> saving the links that is allowed to be downloaded and already downloaded */
    public static java.util.HashMap<String, Integer> linksAdded = new java.util.HashMap<String, Integer>();

    public static MongoClient mongoClient;
    /* empty constructor */
    public robotCheck() {
        mongoClient = MongoClients.create("mongodb+srv://Noran:ci9L$h$Cp4_SVJr@cluster0.bktb5.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
    }

    /* this class is the main class of the robots checker --> access the robots.txt of the host and save it */
    public boolean robotSafe(String link)  {

        URL url;
        String host;
        try {
             url = new URL(link);
             String protocol = url.getProtocol(); /* such as : http / https / ftp */
             host = url.getHost();         /* such as : if we have url : https:// www.geeksforgeeks.org --> host : www.geeksforgeeks.org */

            /* accessing robots.txt from the url */
            url = new URL(protocol + "://" + host + "/robots.txt");

        }
        catch (MalformedURLException error) {
            System.out.println("Error raised while trying to access robots.txt : " + error);
            return false;
        }
        /*** This part is responsible for accessing the robots.txt as string of commands ****/
        /*********/
        if (url != null) {
            String robotsCommands;
            try {
                InputStream urlRobotStream = url.openStream();

                byte b[] = new byte[1000];
                int numRead = urlRobotStream.read(b);
                robotsCommands = new String(b, 0, numRead);
                while (numRead != -1) {
                    numRead = urlRobotStream.read(b);
                    if (numRead != -1) {
                        String newCommands = new String(b, 0, numRead);
                        robotsCommands += newCommands;
                    }
                }
                urlRobotStream.close();
                /**System.out.println("String Commands is :" + robotsCommands); **/
            }
            catch (IOException e) {
                System.out.println("error while trying to extract disallows from the url "+url);
                return false; /* if there is no robots.txt file, it is okay to download the page */
            }
            if (robotsCommands.contains("Disallow")) /* if there are no "disallow" values, then they are not blocking anything.*/ {
                String[] split = robotsCommands.split("\n");
                String typeOfUserAgent = null;
                ArrayList<String> disallows = new ArrayList<String>();
                boolean userAgent = false;

                for (int i = 0; i < split.length; i++) {
                    String line = split[i].trim();
                    if (line.toLowerCase().startsWith("user-agent")) {
                        int start = line.indexOf(":") + 1;
                        int end = line.length();
                        typeOfUserAgent = line.substring(start, end).trim();
                        System.out.println(typeOfUserAgent);
                        if (typeOfUserAgent.equalsIgnoreCase("*")) {
                            userAgent = true;
                        } else userAgent = false;

                    } else if (line.toLowerCase().startsWith("disallow") && userAgent) {
                        if (typeOfUserAgent != null) {
                            int start = line.indexOf(":") + 1;
                            int end = line.length();
                            String s = line.substring(start, end).trim();
                            disallows.add(s);

                        }
                    }
                }
                LinksVisited.put(host, disallows);
                System.out.println("disalllllllllllllllllllllows are :" + disallows);

                /**********/
                return true;

            }
        }
        return true;
    }
    /* indicator is the host has been added in LinksVisited or not */
    boolean isAdded(String link)
    {
        if (LinksVisited.containsKey(link)) return true;
        else return false;
    }
    /* search if the current link (not necessary the host link , may be stackoverflow/users/teams ) */
    boolean Allowed(URL link)
    {
        ArrayList<String> disallows = LinksVisited.get(link.getHost().replace("www",""));
        if (disallows != null) {
            for (String pattern : disallows) {
                String regex = pattern;

                regex = regex.replaceAll("\\*", ".*");

                regex = ".*" + regex + ".*";
                Pattern p = Pattern.compile(regex);
                Matcher matcher = p.matcher(link.toString());
                if (matcher.matches()) return false;

            }
        }
        return true;
    }
    /* collector of the above functions , pass the link as an argument and process the link and download it if possible */
    boolean check(URL url)
    {
        try
        {
            /* handle if the same link already fetched before but with / or # or \ in the end */
            if(url.toString().endsWith("/") ||url.toString().endsWith("#")||url.toString().endsWith("\\") )
            {
                String s = url.toString().substring(0,url.toString().length() - 1);
                System.out.println(s);
                url = new URL(s);

            }
            String urlFile=url.getFile();
            /*/URI uri = url.toURI().normalize();*/
            if(isAdded(url.getHost().replace("www","")))
            {
                System.out.println("already checked");
                if(Allowed(url)) {
                    System.out.println("\n Allowed to download " + url);
                    if(!linksAdded.containsKey(urlFile)) {
                        linksAdded.put(urlFile, 0);
                        downloadPage(url);
                        InsertDB(url, urlFile);
                        System.out.println("\n page downloaded");
                        return true;
                    }
                    else
                    {
                        int oldValue = linksAdded.get(urlFile);
                        linksAdded.replace(urlFile,oldValue,oldValue+1);
                        UpdateDB(url, urlFile);
                        System.out.println("\n page existed , rank increased");
                        return false;
                    }
                }
                else {
                    System.out.println("\n not Allowed to download "+url);
                    return false;
                }
            }
            else
            {

                System.out.println("Not checked yet !! checking the link ....");
                if(robotSafe(url.toString())) {
                    if (Allowed(url)){
                        if (!linksAdded.containsKey(urlFile)) {
                            linksAdded.put(urlFile, 0);
                            downloadPage(url);
                            InsertDB(url, urlFile);
                            System.out.println("\n page downloaded");
                            return true;
                        } else {
                            int oldValue = linksAdded.get(urlFile);
                            linksAdded.replace(urlFile, oldValue, oldValue + 1);
                            UpdateDB(url, urlFile);
                            System.out.println("\n page existed , rank increased");
                            return false;
                        }
                    }
                }
            }
        }
        catch ( IOException exception)
        {
            System.out.println("\n Error while try to check for robots.txt "+ exception);
            return false;
        }
        return false;
    }

    boolean downloadPage(URL url)
    {
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(url.openStream()));

            Connection connection = Jsoup.connect(url.toString());
            Document document = connection.get();
            String documentTitle = document.title().trim().replaceAll("\\s","");
            documentTitle = documentTitle.replaceAll("www","");
            documentTitle = documentTitle.replaceAll(":","");
            documentTitle = documentTitle.replaceAll("https","");
            documentTitle = documentTitle.replaceAll("-","");
            documentTitle = documentTitle.replaceAll("/","");
            documentTitle = documentTitle.replaceAll("|","");
            BufferedWriter writer =
                    new BufferedWriter(new FileWriter("./Downloads/"+documentTitle+".html"));


            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
            }

            reader.close();
            writer.close();
            System.out.println("Successfully Downloaded.");
            InsertDB(url, documentTitle);
        }

        catch (IOException exception) {
            System.out.println("IOException raised " + exception);
            return false;
        }
        return true;
    }
    private void InsertDB(URL the_link, String URLName){

        int Rank = 0;
        if(linksAdded.get(URLName) != null)
            Rank = (Integer) linksAdded.get(URLName);
        String Path = "../Downloads/"+URLName + ".html";
        System.out.println("path : " + Path);
        System.out.println("Rank : " + Rank);
        MongoDatabase db = mongoClient.getDatabase("test");
        MongoCollection<org.bson.Document> crawlerDocCollection = db.getCollection("crawlerDocuments");
        org.bson.Document DocInsert = new org.bson.Document().append("url", the_link.toString())

                .append("localPath", Path)
                .append("Rank", Rank);
        crawlerDocCollection.insertOne(DocInsert);
    }

    private void UpdateDB(java.net.URL the_link, String URLName){
        int Rank = 0;
        if(linksAdded.get(URLName) != null)
            Rank = (Integer) linksAdded.get(URLName);
        MongoDatabase db;
        db = mongoClient.getDatabase("test");
        MongoCollection<org.bson.Document> crawlerDocCollection = db.getCollection("crawlerDocuments");

        Bson filter = eq("url",the_link.toString());
        Bson UpdateRank = set("Rank",Rank);
        crawlerDocCollection.updateOne(filter, UpdateRank);
    }
}
/*************************************************************************************/
/* This class is for file operations */
class SeedsFile {

    // TODO ::  encoding the whole content of url then save (case of different links with the same page content).
    // TODO: save the state of the crawler into the database --> Level
    static File Seeds,state;
    static FileWriter fileWriter;
    static int Level = 7;            /* saving the count of the links crawled, initially with 6 since seed has 5 links else get from the db */
    static int current_line ;


    public SeedsFile()
    {
        FileCreate();
        current_line = getState() ;
        System.out.println("Current line:"+current_line);
    }
    /* File creation :  static since all object from that class will deal with the same version*/
    private void FileCreate() {
        try {
            Seeds = new File("Seeds.txt");
            state = new File("state.txt");
            if (Seeds.createNewFile()||state.createNewFile()) {
                System.out.println("\n Seeds and state files created successfully ..! ");
            } else {
                System.out.println("\n Files is already existed");


            }
        }
        catch (IOException error) {
            System.out.println("\n An error occurred while creating the file .. " + error.getMessage());
        }
    }
    private void updateState() {
        try{

            fileWriter = new FileWriter("state.txt",false);   /* append here set true to avoid overlapping */
            fileWriter.write( Integer.toString(current_line+1));

        }
        catch(Exception error)
        { System.out.println(error.getMessage());}

        close();
    }
    static private int getState() {
        String line ="";
        try
        {
            Stream<String> lines =  Files.lines(Paths.get(state.getPath()));
            line  = lines.findFirst().orElse("");
        }
        catch (IOException error)
        {
            System.out.println("\n"+ error.getMessage());
        }


        return Integer.parseInt(line);

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

        this.Level++;
        close();
    }
    synchronized void setCurrentLine()
    {
        updateState();
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
/**********************************************************************/
/* Crawler */
public class Crawler implements Runnable {

    private static final int MAX_PAGES = 5000;        /* Max number of pages can be downloaded.*/
    robotCheck checker = new robotCheck();          /* create instance from robotCheck class */
    private int ThreadID;
    final SeedsFile seeds;
    public Crawler(int id, SeedsFile inputFile) {
        seeds = inputFile;
        this.ThreadID = id;
        System.out.println("\n Crawler with rank =  " + this.ThreadID + " created.");
    }

    @Override
    public void run() {

        crawl(this.seeds.current_line);

    }

    private Document Request(String url) {
        try {
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();

            if (connection.response().statusCode() == 200) {
                System.out.println("\n *** crawler ID: " + this.ThreadID + ",, Received webpage at " + url);

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


                if (SeedsFile.Level >= MAX_PAGES) {
                    System.out.println("\n Crawling reached its maximum now, " + MAX_PAGES + "pages are available now. ");
                    SeedsFile.current_line = 0;
                    seeds.setCurrentLine();
                    break;
                }
                else {
                    String URL;
                    synchronized (seeds) {
                         URL = this.seeds.FileReader(SeedsFile.current_line);
                        seeds.setCurrentLine();
                    }
                    if (!URL.isEmpty())  {
                        System.out.println("\n Thread num = " + Thread.currentThread().getName() + " now fetch link at line " + Integer.toString(SeedsFile.current_line - 1) + " link is " + URL);

                        try {
                            if(checker.check(new URL(URL)))
                            {
                            }
                        }
                        catch (IOException exception)
                        {
                            System.out.println(exception);
                        }

                        Document document = Request(URL);

                        if (document!= null) {

                            for(Element currentLink :document.select("a[href]"))
                            {

                                String nextLink = currentLink.absUrl("href");
                                try {
                                    if(checker.check(new URL(nextLink)))
                                    {
                                        System.out.println("Writing to seeds");
                                        synchronized (seeds) {
                                            if(robotCheck.linksAdded.containsKey(nextLink)) continue;
                                            else seeds.FileWriter(nextLink);
                                        }
                                    }
                                }
                                catch (IOException exception)
                                {
                                    System.out.println(exception);
                                }


                            }
                        }

                    }
                    else {break;}
                }

            try {
                Thread.sleep(500);

            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + "is awaken");
            }

        }

    }
}

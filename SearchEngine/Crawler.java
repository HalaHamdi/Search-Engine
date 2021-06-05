package SearchEngine;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import org.apache.tika.io.IOUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.*;
import java.net.URLConnection;
/****************************************************************************/
/* Robot Checker is responsible for check if the document is allowed to be downloaded*/
class robotCheck {

    /* LinksVisited --> saving the host of the url and the disallows urls for this host */
    public static java.util.HashMap<String, ArrayList<String>> LinksVisited = new java.util.HashMap<String, ArrayList<String>>();
    /* string for the encoding content, int rank --> saving the links that is allowed to be downloaded and already downloaded */
    public static java.util.HashMap<String, Integer> linksAdded = new java.util.HashMap<String, Integer>();

    /* empty constructor */
    public robotCheck() { }

    /* this class is the main class of the robots checker --> access the robots.txt of the host and save it */
    public boolean robotSafe(String link) throws IOException {

        URL url = new URL(link);
        String protocol = url.getProtocol(); /* such as : http / https / ftp */
        String host = url.getHost();         /* such as : if we have url : https:// www.geeksforgeeks.org --> host : www.geeksforgeeks.org */
        host.replace("www", "");

        /* accessing robots.txt from the url */
        try {
            url = new URL(protocol + "://" + host + "/robots.txt");
        } catch (MalformedURLException error) {
            System.out.println("Error raised while trying to access robots.txt : " + error);
            return false;
        }
        /*** This part is responsible for accessing the robots.txt as string of commands ****/
        /*********/
        if (url != null) {
            InputStream urlRobotStream = url.openStream();
            String robotsCommands;
            try {
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
            } catch (IOException e) {
                return true; /* if there is no robots.txt file, it is okay to download the page */
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
                url = new URL(url.toString().substring(0,url.toString().length() - 1));
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
                        System.out.println("\n page downloaded");
                        return true;
                    }
                    else
                    {
                        int oldValue = linksAdded.get(urlFile);
                        linksAdded.replace(urlFile,oldValue,oldValue+1);
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
                    if (Allowed(url)) {
                        if (!linksAdded.containsKey(urlFile)) {
                            linksAdded.put(urlFile, 0);
                            downloadPage(url);
                            System.out.println("\n page downloaded");
                            return true;
                        } else {
                            int oldValue = linksAdded.get(urlFile);
                            linksAdded.replace(urlFile, oldValue, oldValue + 1);
                            System.out.println("\n page existed , rank increased");
                            return false;
                        }
                    }
                }
            }
        }
        catch (IOException exception)
        {
            System.out.println("\n Error while try to check for robots.txt "+ exception);

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
            BufferedWriter writer =
                    new BufferedWriter(new FileWriter("./Downloads/"+document.title()+".html"));


            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
            }

            reader.close();
            writer.close();
            System.out.println("Successfully Downloaded.");
        }

        catch (IOException exception) {
            System.out.println("IOException raised " + exception);
            return false;
        }
        return true;
    }
}
/*************************************************************************************/
/* This class is for file operations */
class SeedsFile {

    // TODO ::  encoding the whole content of url then save (case of different links with the same page content).
    // TODO: save the state of the crawler into the database --> Level
    static File Seeds;
    static FileWriter fileWriter;
    static int Level = 7;            /* saving the count of the links crawled, initially with 6 since seed has 5 links else get from the db */
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
            System.out.println("\n An error occurred while creating the file .. " + error.getMessage());
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

        this.Level++;
        close();
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
/**********************************************************************/
/* Crawler */
public class Crawler implements Runnable {

    private static final int MAX_PAGES = 5000;        /* Max number of pages can be downloaded.*/
    robotCheck checker = new robotCheck();          /* create instance from robotCheck class */
    private int ThreadID;
    final SeedsFile seeds;
    public Crawler(int id, SeedsFile inputFile) {
        this.seeds = inputFile;
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
            synchronized (this.seeds) {

                if (SeedsFile.Level >= MAX_PAGES ) {
                    System.out.println("\n Crawling reached its maximum now, " + MAX_PAGES + "pages are available now. ");
                }
                else {

                    String URL = this.seeds.FileReader(SeedsFile.current_line);
                    if (!URL.isEmpty())  {
                        System.out.println("\n Thread num = " + Thread.currentThread().getName() + " now fetch link at line " + SeedsFile.current_line + " link is " + URL);

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
                                        seeds.FileWriter(nextLink);

                                    }
                                }
                                catch (IOException exception)
                                {
                                    System.out.println(exception);
                                }


                            }

                        }
                        this.seeds.setCurrentLine();
                    }
                    else {break;}
                }
            }
            try {
                Thread.sleep(500);

            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + "is awaken");
            }

        }

    }
}

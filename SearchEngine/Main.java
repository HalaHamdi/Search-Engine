package SearchEngine;

public class Main {

    public static void main(String[] args) throws InterruptedException{

         int numThreads = 5;
         SeedsFile File = new SeedsFile();
         Thread []crawlers = new Thread[numThreads];
         for (int i=0;i<numThreads;i++)
         {
             crawlers[i]= new Thread(new Crawler(i,File));

             crawlers[i].setName(Integer.toString(i));
             crawlers[i].start();

         }
        for (Thread thread : crawlers) {
            thread.join();
        }

         File.close();
    }

}

package SearchEngine;

public class Main {

    public static void main(String[] args) throws InterruptedException{


         SeedsFile File = new SeedsFile();
         Thread []crawlers = new Thread[3];
         for (int i=0;i<3;i++)
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


import java.util.*;

public class WikiRacer {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while(true) {
            //Input first Wikipedia page
            System.out.println("Enter your starting wikipedia page:");
            System.out.println("Type \"exit\" to terminate the program.");
            String next = in.next();
            if(next.equals("exit")) {
                //Exit the program
                System.exit(0);
            }
            WikiPage start = WikiPage.factory(next);
            while(start == null) {
                //Invalid Page
                System.out.println("Invalid Page, Try Again:");
                start = WikiPage.factory(in.next());
            }


            //Input second Wikipedia page
            System.out.println("Enter your final wikipedia page:");
            System.out.println("Type \"exit\" to terminate the program.");
            next = in.next();
            if(next.equals("exit")) {
                //Exit the program
                System.exit(0);
            }
            WikiPage destination = WikiPage.factory(next);
            while(destination == null || destination.equals(start)) {
                //Invalid Page
                System.out.println("Invalid Page, Try Again:");
                destination = WikiPage.factory(in.next());
            }


            //Find a path to the destination and print
            long startTime = System.currentTimeMillis();
            WikiTree tree = new WikiTree(start, destination);
            tree.search();
            System.out.println(tree);
            System.out.println();
            System.out.println("Process finished in " +
                    (System.currentTimeMillis() - startTime) / 1000.0 +
                    " seconds.\n");
        }
    }
}

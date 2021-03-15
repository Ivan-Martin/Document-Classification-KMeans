import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main (String [] args){
        System.out.println("Enter document directory path: ");
        Scanner in = new Scanner (System.in);
        File directory = new File(in.nextLine());

        ArrayList <Document> list = new ArrayList<>();

        FileParser fp = new FileParser();

        int id = 0;
        for (File f : directory.listFiles()){
            list.add(fp.parse(f, id));
            id++;
        }

        K_Means kmeans = new K_Means();

        kmeans.setCollection(list);
        System.out.println("Select comparation method for your documents:");
        System.out.println("J - Jaccard Coefficient");
        System.out.println("C - Cosine Similarity");
        System.out.println("E - Euclidean Distance");

        String read = in.nextLine().toUpperCase();
        while (!(read.equals("J") || read.equals("C") || read.equals("E"))) {
            System.out.println("Comparison method must be [J/C/E]");
            read = in.nextLine().toUpperCase();
        }

        System.out.println("Enter number of groups (Enter 0 to compute best number of groups automatically)");
        int groups = in.nextInt();

        kmeans.setGroups(groups);

        kmeans.setOutput(new File ("output.txt"));

        Method m = null;

        switch (read.charAt(0)){
            case 'J':
                m = Method.Jaccard;
                break;
            case 'E':
                m = Method.Euclidean;
                break;
            case 'C':
                m = Method.Cosine;
                break;
        }

        long time = System.currentTimeMillis();
        kmeans.calculateGroups(m);
        System.out.println("Time taken: " + (System.currentTimeMillis() - time));
    }
}

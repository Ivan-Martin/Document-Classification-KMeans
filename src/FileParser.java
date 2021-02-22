import java.io.*;
import java.util.ArrayList;

public class FileParser {

    public Document parse (File f, int id){
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            System.err.println("Invalid document file");
            e.printStackTrace();
            return null;
        }

        try {
            ArrayList<Integer> list = new ArrayList<>();

            String name = br.readLine();

            String line;

            while ((line = br.readLine()) != null){
                list.add(Integer.parseInt(line));
            }

            Object [] array2 = list.toArray();

            int [] array = new int [array2.length];
            for (int i = 0; i < array2.length; i++){
                array[i] = (int) array2[i];
            }

            ProcessedDocument doc = new ProcessedDocument(array, null);
            doc.id = id;
            doc.name = name;
            return doc;
        } catch (IOException e){
            System.err.println("Invalid document file at " + f.getName());
            e.printStackTrace();
            return null;
        }
    }
}

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
            ArrayList<String> list = new ArrayList<>();

            String name = br.readLine();

            String line;

            while ((line = br.readLine()) != null){
                list.add(line);
            }

            Object [] array2 = list.toArray();


            ArrayList <Integer> arrayList = new ArrayList<>();
            for (int i = 0; i < array2.length; i++){
                String splitted [] = ((String) array2[i]).split(" ");
                int word = Integer.parseInt(splitted[0]);
                int weight = Integer.parseInt(splitted[1]);
                while (arrayList.size() < word) {
                    arrayList.add(0);
                }
                arrayList.set(word, weight);
            }

            ProcessedDocument doc = new ProcessedDocument(arrayList,null);
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

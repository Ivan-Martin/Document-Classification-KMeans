import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;

public class K_Means {

    private ArrayList<Document> collection;
    private File output;
    private int groups;
    private int limit = 1000;

    public void calculateGroups (Method m){
        if (groups == 0){

        } else {
            calculateGroups(m, groups);
        }
    }

    public void calculateGroups (){
        calculateGroups(null);
    }

    public void setCollection(ArrayList<Document> collection) {
        this.collection = collection;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public void setGroups(int groups) {
        this.groups = groups;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    private void calculateGroups(Method m, int groups){
        //First part: Initializations
        // 1.- Set the proper method to each document
        // 2.- Set the correspondence between id and document object

        Document [] centroids = new Document [groups];
        //At centroids[C] stores the Document representing the centroid of C group.
        HashMap <Integer, Document> correspondence = new HashMap<>();
        for (Document d : collection){
            correspondence.put(d.id, d);
            if (d instanceof ProcessedDocument){
                ((ProcessedDocument) d).setMethod(m);
            }
        }

        //Second part: initialize Centroids by picking random documents.
        Collections.shuffle(collection);
        for (int i = 0; i < groups; i++){
            centroids[i] = collection.get(i);
        }

        boolean changes = true;
        int iteration = 0;

        HashMap<Integer, Integer> previousdocumentGroup;
        //Stores previous allocation of Documents in groups.
        HashMap<Integer, Integer> documentGroup = new HashMap <> ();

        //Two stop conditions:
        //Stop at either
        // a) Made more iterations than the established limit.
        // b) Make an iteration without changes.
        while (changes && iteration < limit){
            previousdocumentGroup = documentGroup;
            documentGroup = new HashMap <> ();
            for (int i = 0; i < collection.size(); i++){
                //For each document, select the nearest centroid to it and assign it to that group.
                int nearestGroup = selectGroup(collection.get(i), centroids);
                documentGroup.put(i, nearestGroup);
            }

            relocateCentroids(centroids, documentGroup, correspondence);
            //Relocate the centroid of each group given the documents now allocated.

            changes = previousdocumentGroup.equals(documentGroup);
            iteration++;
        }

        printResults(centroids, documentGroup, correspondence);
    }

    private void printResults(Document[] centroids, HashMap<Integer, Integer> documentGroup, HashMap <Integer, Document> correspondence) {
        PrintWriter pw;
        try{
            pw = new PrintWriter(output);
        } catch (FileNotFoundException f){
            System.err.println("Error: Output file not found, changing to printing to console.");
            f.printStackTrace();
            pw = new PrintWriter(System.out);
        }
        pw.println(centroids.length + " groups found");

        HashMap <Integer, ArrayList <Document>> clusters = locateDocuments(documentGroup, correspondence);

        for (int i = 0; i < centroids.length; i++){
            ArrayList <Document> list = clusters.get(i);
            pw.println("Group: " + i);
            pw.println("Group centroid: " + centroids[i].name);
            for (Document di : list){
                pw.println(di.name);
            }
            pw.println();
        }

    }

    private HashMap <Integer, ArrayList <Document>> locateDocuments (HashMap<Integer, Integer> documentGroup, HashMap <Integer, Document> correspondence) {
        HashMap <Integer, ArrayList <Document>> clusters = new HashMap<>();
        //Compute each cluster documents
        for (Integer i : documentGroup.keySet()){
            int group = documentGroup.get(i);
            if (clusters.containsKey(group)){
                clusters.get(group).add(correspondence.get(i));
            } else {
                ArrayList <Document> list = new ArrayList<>();
                list.add(correspondence.get(i));
                clusters.put(group, list);
            }
        }
        return clusters;
    }

    private void relocateCentroids(Document[] centroids, HashMap<Integer, Integer> documentGroup, HashMap <Integer, Document> correspondence) {

        HashMap <Integer, ArrayList <Document>> clusters = locateDocuments(documentGroup, correspondence);

        for (int group : clusters.keySet()){
            float min = Float.MAX_VALUE;
            Document center = centroids[group];
            for (Document d : clusters.get(group)){
                float value = 0.0f;
                for (Document d_i : clusters.get(group)){
                    if (d_i.id != d.id){
                        value += d.computeSimilarity(d_i);
                    }
                }
                if (value < min){
                    min = value;
                    center = d;
                }
            }
            centroids[group] = center;
        }
    }

    private int selectGroup(Document document, Document[] centroids) {
        float max = Float.MIN_VALUE;
        int group = -1;
        for (int i = 0; i < centroids.length; i++){
            Document d = centroids [i];
            float similarity = document.computeSimilarity(d);
            if (max < similarity){
                max = similarity;
                group = i;
            }
        }
        return group;
    }



}

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;

public class K_Means {

    private ArrayList<Document> collection;
    private File output;
    private boolean printGroups;
    private int groups;
    private int limit = 1000;

    public void calculateGroups (Method m){
        if (groups == 0){

        } else {
            calculateGroups(m, groups);
        }
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
    }

    private void relocateCentroids(Document[] centroids, HashMap<Integer, Integer> documentGroup, HashMap <Integer, Document> correspondence) {
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

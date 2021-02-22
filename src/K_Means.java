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
    private boolean print = true;

    //TODO: Include seed to Random methods.

    public void calculateGroups (Method m){
        if (groups == 0){
            optimizeGroups(m);
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

    private HashMap<Integer, ArrayList<Document>> calculateGroups(Method m, int groups){
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

        if (print) printResults(centroids, documentGroup, correspondence);

        return locateDocuments(documentGroup, correspondence);
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

    private void printResults(HashMap <Integer, ArrayList <Document>> clusters, int clusterNumber){
        PrintWriter pw;
        try{
            pw = new PrintWriter(output);
        } catch (FileNotFoundException f){
            System.err.println("Error: Output file not found, changing to printing to console.");
            f.printStackTrace();
            pw = new PrintWriter(System.out);
        }
        pw.println(clusterNumber + " groups found");

        for (int i = 0; i < clusterNumber; i++){
            ArrayList <Document> list = clusters.get(i);
            pw.println("Group: " + i);
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

    private void optimizeGroups (Method m) {
        print = false;
        int bestClusters = 2;
        float bestCoefficient = 0.0f;
        HashMap <Integer, ArrayList <Document>> bestResult = null;
        for (int i = 2; i <= 20; i++){
            HashMap <Integer, ArrayList <Document>> result = calculateGroups(m, i);
            float silhouetteCoefficient = calculateSilhouetteCoefficient(result);
            if (silhouetteCoefficient > bestCoefficient){
                bestCoefficient = silhouetteCoefficient;
                bestClusters = i;
                bestResult = result;
            }
        }
        print = true;
        printResults(bestResult, bestClusters);

    }

    private float calculateSilhouetteCoefficient(HashMap<Integer, ArrayList<Document>> result) {
        float a = 0.0f; //Stores the avg similarity of each document between documents of the same cluster
        HashMap <Integer, Integer> documentCluster = new HashMap<>();
        //Stores which cluster each document belongs to.
        for (int i : result.keySet()){
            //Computers the similarity of each document to the rest of documents of same cluster.
            float sumSimilarity = 0.0f;
            ArrayList <Document> list = result.get(i);
            for (Document d : list){
                documentCluster.put(d.id, i);
                float avgIndividualSimilarity = 0.0f;
                for (Document d2 : list){
                    if (d.id != d2.id){
                        avgIndividualSimilarity += d.computeSimilarity(d2);
                    }
                }
                avgIndividualSimilarity /= list.size() - 1;
                sumSimilarity += avgIndividualSimilarity;
            }
            float avgClusterSimilarity = sumSimilarity / list.size();
            a += avgClusterSimilarity;
        }
        a /= result.keySet().size();

        float b; //Stores the avg similarity between documents of the same cluster and different clusters

        float sumDifferentSimilarities = 0.0f;
        for (Document d : collection){
            int cluster = documentCluster.get(d.id);
            float indivSimilarity = 0.0f;
            for (int k : result.keySet()){
                if (k != cluster){
                    for (Document doc : result.get(k)){
                        indivSimilarity += d.computeSimilarity(doc);
                    }
                }
            }
            indivSimilarity /= collection.size() - result.get(cluster).size();
            sumDifferentSimilarities += indivSimilarity;
        }

        b = sumDifferentSimilarities / collection.size();

        return (b - a) / Math.max(a, b);
    }


}

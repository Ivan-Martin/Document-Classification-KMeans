import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class K_Means {

    private ArrayList<Document> collection;
    private File output;
    private int groups;
    private int limit = 100;
    private boolean print = true;
    private int seed = 24;
    private boolean printSilhouette = true;
    private boolean maximize = true;

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

    public void setMaximize (boolean maximize) { this.maximize = maximize; }

    private HashMap<Integer, ArrayList<Document>> calculateGroups(Method m, int groups){
        //First part: Initializations
        // 1.- Set the proper method to each document
        // 2.- Set the correspondence between id and document object
        // 3.- Set the maximize / minimize function

        switch (m){
            case Euclidean:
                maximize = false;
                break;
            case Cosine:
            case Jaccard:
                maximize = true;
                break;
        }

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
        Collections.shuffle(collection, new Random(seed));
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
                int nearestGroup = selectGroup(collection.get(i), centroids, maximize);
                documentGroup.put(i, nearestGroup);
            }

            relocateCentroids(centroids, documentGroup, correspondence);
            //Relocate the centroid of each group given the documents now allocated.

            changes = !previousdocumentGroup.equals(documentGroup);
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
            if (list != null){
                pw.println("Group: " + i);
                for (Document di : list){
                    pw.println(di.name);
                }
                pw.println();
            }
        }

        pw.flush();
        pw.close();
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
            if (list != null){
                pw.println("Group: " + i);
                for (Document di : list){
                    pw.println(di.name);
                }
                pw.println();
            }
        }
        pw.flush();
        pw.close();
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
        int maxId = 0;
        for (int group : clusters.keySet()){
            Document center = centroids[group];
            Object [] array = clusters.get(group).toArray();
            Document [] arrayD = new Document [array.length];
            for (int i = 0; i < arrayD.length; i++){
                arrayD[i] = (Document) array[i];
                maxId = Math.max(maxId, arrayD[i].id);
            }
            Document newCenter = center.calculateCentroid(arrayD);
            centroids[group] = newCenter;
        }
        for (Document d : centroids){
            maxId++;
            d.id = maxId;
        }
    }

    private int selectGroup(Document document, Document[] centroids, boolean maximize) {
        float max = -Float.MAX_VALUE;
        float min = Float.MAX_VALUE;
        int maxGroup = -1;
        int minGroup = -1;
        for (int i = 0; i < centroids.length; i++){
            Document d = centroids [i];
            float similarity = document.computeSimilarity(d);
            if (max < similarity){
                max = similarity;
                maxGroup = i;
            }
            if (min > similarity){
                min = similarity;
                minGroup = i;
            }
        }
        return maximize ? maxGroup : minGroup;
    }

    private void optimizeGroups (Method m) {
        print = false;
        int bestClusters = 2;
        float bestCoefficient = -1.0f;
        HashMap <Integer, ArrayList <Document>> bestResult = null;
        for (int i = 2; i <= 10; i++){
            HashMap <Integer, ArrayList <Document>> result = calculateGroups(m, i);
            float silhouetteCoefficient = calculateSilhouetteCoefficient(result);
            if (silhouetteCoefficient > bestCoefficient){
                bestCoefficient = silhouetteCoefficient;
                bestClusters = i;
                bestResult = result;
            }
            if (printSilhouette) System.out.println("Silhouette coefficient for groups: " + i + " equals " + silhouetteCoefficient);
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

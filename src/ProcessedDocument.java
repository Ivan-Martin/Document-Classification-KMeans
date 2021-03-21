import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ProcessedDocument extends Document {

    private HashMap<Integer, Float> words;
    private Method m;

    public ProcessedDocument(HashMap<Integer, Float> words, Method m){
        this.words = words;
        this.m = m;
    }

    public void setMethod (Method m){
        this.m = m;
    }

    @Override
    public float computeSimilarity(Document d) {
        switch (m){
            case Cosine:
                return computeCosineSimilarity (d);
            case Euclidean:
                return computeEuclideanSimilarity (d);
            case Jaccard:
                return jaccardCoefficient (d);
        }
        return 0.0f;
    }

    @Override
    public Document calculateCentroid(Document[] group) {
        HashMap <Integer, Float> words = new HashMap <> ();
        int size = group.length;
        for (Document d : group){
            ProcessedDocument doc = (ProcessedDocument) d;
            for (int i : doc.words.keySet()){
                if (words.containsKey(i)){
                    float actual = words.get(i);
                    words.put(i, actual+doc.words.get(i)/size);
                } else {
                    words.put(i, doc.words.get(i)/size);
                }
            }
        }

        ProcessedDocument d = new ProcessedDocument(words, m);
        return d;
    }

    private float computeCosineSimilarity (Document d){
        ProcessedDocument doc = (ProcessedDocument) d;
        float numerator = 0.0f;
        float denomFirst = 0.0f;
        float denomSecond = 0.0f;
        HashSet <Integer> union = new HashSet <> (this.words.keySet());
        union.addAll(doc.words.keySet());
        for (int i : union){
            if (this.words.containsKey(i) && doc.words.containsKey(i)) numerator += this.words.get(i) * doc.words.get(i);
            if (this.words.containsKey(i)) denomFirst += this.words.get(i) * this.words.get(i);
            if (doc.words.containsKey(i)) denomSecond += doc.words.get(i) * doc.words.get(i);
        }
        float denominator = (float) (Math.sqrt(denomFirst) + Math.sqrt(denomSecond));
        return numerator / denominator;
    }

    private float computeEuclideanSimilarity (Document d){
        ProcessedDocument doc = (ProcessedDocument) d;
        float sum = 0.0f;
        HashSet <Integer> union = new HashSet <> (this.words.keySet());
        union.addAll(doc.words.keySet());
        for (int i : union){
            if (this.words.containsKey(i) && doc.words.containsKey(i)) sum += Math.pow(this.words.get(i) - doc.words.get(i), 2);
            else if (this.words.containsKey(i)) sum += Math.pow(this.words.get(i), 2);
            else sum += Math.pow(doc.words.get(i), 2);
        }
        return (float) Math.sqrt(sum);
    }

    private float jaccardCoefficient (Document d){
        ProcessedDocument doc = (ProcessedDocument) d;
        Set<Integer> set1 = words.keySet();
        Set<Integer> set2 = doc.words.keySet();

        HashSet <Integer> intersection = new HashSet<Integer>(set1);
        intersection.retainAll(set2);

        HashSet <Integer> union = new HashSet <Integer> (set1);
        union.addAll(set2);

        return (float) intersection.size() / (float) union.size();
    }
}

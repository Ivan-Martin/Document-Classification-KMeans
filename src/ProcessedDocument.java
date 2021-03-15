import java.util.ArrayList;
import java.util.HashSet;

public class ProcessedDocument extends Document {

    private ArrayList<Integer> words;
    private Method m;

    public ProcessedDocument(ArrayList<Integer> words, Method m){
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

    private float computeCosineSimilarity (Document d){
        ProcessedDocument doc = (ProcessedDocument) d;
        float numerator = 0.0f;
        float denomFirst = 0.0f;
        float denomSecond = 0.0f;
        int maxLength = Math.max(this.words.size(), doc.words.size());
        int minLength = Math.min(this.words.size(), doc.words.size());
        for (int i = 0; i < maxLength; i++){
            if (i < minLength) numerator += this.words.get(i) * doc.words.get(i);
            if (i < this.words.size()) denomFirst += this.words.get(i) * this.words.get(i);
            if (i < doc.words.size()) denomSecond += doc.words.get(i) * doc.words.get(i);
        }
        float denominator = (float) (Math.sqrt(denomFirst) + Math.sqrt(denomSecond));
        return numerator / denominator;
    }

    private float computeEuclideanSimilarity (Document d){
        ProcessedDocument doc = (ProcessedDocument) d;
        float sum = 0.0f;
        int maxLength = Math.max(this.words.size(), doc.words.size());
        for (int i = 0; i < maxLength; i++){
            if (i < this.words.size() && i < doc.words.size()) sum += Math.pow(this.words.get(i) - doc.words.get(i), 2);
            else if (i < this.words.size()) sum += Math.pow(this.words.get(i), 2);
            else sum += Math.pow(-doc.words.get(i), 2);
        }
        return (float) Math.sqrt(sum);
    }

    private float jaccardCoefficient (Document d){
        ProcessedDocument doc = (ProcessedDocument) d;
        HashSet<Integer> set1 = new HashSet<>(words);

        HashSet<Integer> set2 = new HashSet<>(doc.words);

        HashSet <Integer> intersection = new HashSet<Integer>(set1);
        intersection.retainAll(set2);

        HashSet <Integer> union = new HashSet <Integer> (set1);
        union.addAll(set2);

        return (float) intersection.size() / (float) union.size();
    }
}

import java.util.HashSet;

public class ProcessedDocument extends Document {

    private int [] words;
    private Method m;

    public ProcessedDocument(int [] words, Method m){
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
            case Other:
                //return otherSimilarity (d);
                break;
        }
        return 0.0f;
    }

    private float computeCosineSimilarity (Document d){
        ProcessedDocument doc = (ProcessedDocument) d;
        float numerator = 0.0f;
        float denomFirst = 0.0f;
        float denomSecond = 0.0f;
        for (int i = 0; i < words.length; i++){
            numerator += this.words[i] * doc.words[i];
            denomFirst += this.words[i] * this.words[i];
            denomSecond += doc.words[i] * doc.words[i];
        }
        float denominator = (float) (Math.sqrt(denomFirst) + Math.sqrt(denomSecond));
        return numerator / denominator;
    }

    private float computeEuclideanSimilarity (Document d){
        ProcessedDocument doc = (ProcessedDocument) d;
        float sum = 0.0f;
        for (int i = 0; i < words.length; i++){
            sum += Math.pow(words[i] - doc.words[i], 2);
        }
        return (float) Math.sqrt(sum);
    }
}

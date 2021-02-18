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
                break;
            case Euclidean:
                return computeEuclideanSimilairty (d);
                break;
            case Other:
                return otherSimilarity (d);
                break;
        }
        return 0.0f;
    }
}

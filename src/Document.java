public abstract class Document {

    String name; //Stores the name of the document
    int id; //Stores the numeric ID of the document

    public abstract float computeSimilarity (Document d);
    //Computes similarity between this document and the given one.

    public abstract Document calculateCentroid(Document [] group);

}

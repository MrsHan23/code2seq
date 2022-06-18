package JavaExtractor.Common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class TFIDF {

    /** Calculates TF of a term t given a document 
     * 
     * @param t - term in a document
     * @param document - list of words in a given document
     * @return tf of t 
     */
    public static double tf (String t, List<String> document) {
        return count(t, document)/ document.size();
    }


    /** Counts number of times a term t appears in a document
     * 
     * @param t - term in a document
     * @param document - list of words in a given document
     * @return number of times t appears in document
     */
    public static double count(String t, List<String> document) {
        double count = 0;
        for (String word: document) {
            if (word.equals(t)){
                count++;
            }
        }
        return count;
    }

    /** Calculates df for a term t in a collection of documents
     * 
     * @param t - term in a document
     * @param collection - list of documents
     * @return df of term t
     */
    public static double df(String t, List<String> collection) {
        double count = 0;
        for (String document: collection) {
            if (document.contains(t)){
                count++;
            }
        }
        return count;
    }

    /** Calculates idf for a term t in a collection of documents
     * 
     * @param t - term in a document
     * @param collection - list of documents
     * @return idf of term t
     */
    public static double idf (String t, List<String> collection){
        return Math.log(collection.size()/(df(t, collection)));
    }

    /** Calculates TFIDF of a term t given a document and a collection of documents
     * 
     * @param t - term in a document
     * @param document - list of words in a given document
     * @param collection - list of documents
     * @return tfidf of term t 
     */
    public static double tfIdf(String t, List<String> document, List<String> collection){
        return tf(t, document) * idf(t, collection);
    }

    /** Gets the top N keywords in a sentence using TFIDF
     * 
     * @param sentence - string to get keywords from
     * @param collection - list of sentences/documents
     * @param N - number of keywords
     * @return a string containing the top N keywords in a sentence
     */
    public static String getSentence(String sentence, List<String> collection, int N) {

        // split sentence into words 
        List<String> sentenceList = Arrays.asList(sentence.split("\\s"));

        // calculate tfidf for each term in sentence and put store the results in a hashmap
        HashMap<String, Double> tfIdfMap = new HashMap<>();
        for (String term: sentenceList){
            tfIdfMap.put(term, tfIdf(term, sentenceList, collection));
        }

        // sort results by tfidf values in decreasing order
        tfIdfMap = tfIdfMap.entrySet().stream()
            .sorted(Entry.<String, Double>comparingByValue().reversed())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                                 (e1, e2) -> e1, LinkedHashMap::new));

        // get top N keywords in sentence
        List<String> result = tfIdfMap.keySet().stream().limit(N).collect(Collectors.toList());

        // join keywords in a single string
        return String.join("\\s", result);
    }
    
}

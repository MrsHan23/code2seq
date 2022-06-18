package JavaExtractor.Common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class StopWordsFilter {

    public static List<String> stopwords;
    
    /**
     *  Get list of stopwords from dataset
     */
    public static void setup(){
        try {
            stopwords = Files.readAllLines(Paths.get("JavaExtractor/JPredict/src/main/java/JavaExtractor/Common/stop_words.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove stopwords from a string
     * @param input - string to modify
     * @return input without stopwords
     */
    public static String removeStopWords(String input){

        setup();
        
        String[] allWords = input.split("\\s");
        StringBuilder builder = new StringBuilder();
        for(String word: allWords) {
            if(! stopwords.contains(word)) {
                builder.append(word);
                builder.append("\\s");
            }
        }
        return builder.toString().trim();
    }
}
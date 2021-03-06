package pa.tfidf;

import java.io.*;
import java.util.*;

import pa.utils.IOUtils;


public class TFIDF {

    public TFIDF(){};

    private Map<String, List<String>> map;
    public TFIDF(Map<String, List<String>> tokMap) {
        map = tokMap;
    }
    private String idFile = "resources/ids.txt";
    private String docFile = "resources/docs.txt";
    private String termFile = "resources/terms.txt";
    private String matrixFile = "resources/matrix.txt";
    private String topicsFile = "resources/topics.txt";

    private static IOUtils utils = new IOUtils();

    // Compute TF-IDF matrix
    public void computeTfidfMatrix() throws IOException {

        // TF-IDF matrix
        List<List<Double>> tfidfMatrix = new ArrayList<List<Double>>();

        // Set of all terms
        Set<String> termSet = new HashSet<String>();
        // List of all documents
        List<List<String>> docs = new ArrayList<List<String>>();
        // List of sorted document IDs
        List<String> docIds = new ArrayList(map.keySet());
        Collections.sort(docIds, new AlphanumComparator());

        for (String id: docIds) {
            List<String> toks = map.get(id);
            termSet.addAll(toks);
            docs.add(toks);
        }

        // Sort set of terms
        List<String> terms = new ArrayList(termSet);
        Collections.sort(terms, new AlphanumComparator());

        // Write terms, document IDs, and document tokens
        utils.writeList(docIds, idFile);
        utils.writeList(terms, termFile);
        utils.writeDocs(docs, docFile);

        // Compute TF-IDF matrix
        for (String id: docIds) {

            List<String> toks = map.get(id);
            List<Double> scores = new ArrayList<Double>();

            for (String term: terms) {
                double score = tfIdf(toks, docs, term);
                scores.add(score);
            }
            tfidfMatrix.add(scores);
        }

        // Write matrix
        utils.writeMatrix(tfidfMatrix, matrixFile);
        // Get topics
        getTopics(tfidfMatrix, docs, docIds);
    }

    // Get topics based on TF-IDF scores
    private void getTopics(List<List<Double>> matrix, List<List<String>> docs, List<String> ids) throws IOException {

        System.out.println("Generating topics...");
        FileWriter writer = new FileWriter(topicsFile); 

        for (int i=1; i<=15; i++) {

            List<String> toks = new ArrayList<String>();
            for (String id: ids) {
                String init = "C" + String.valueOf(i) + "_";
                if (id.startsWith(init)) {
                    toks.addAll(map.get(id));
                }
            }

            Map<String, Double> scores = new HashMap<String, Double>();
            for (String tok: toks) {
                double score = tfIdf(toks, docs, tok);
                scores.put(tok, score);
            }

            Map<String, Double> sorted = sortByValues(scores);
            Set<String> keys = sorted.keySet();
            String[] keywordsAll = keys.toArray(new String[keys.size()]);
            Collections.reverse(Arrays.asList(keywordsAll));
            String[] keywords = Arrays.copyOfRange(keywordsAll, 1, 50);

            // Write folder topics to file
            String row = "C" + String.valueOf(i) + ",";
            for(String str: keywords) {
                row += str + ",";
            }
            writer.write(row + "\n");
        }
        writer.close();
    }

    // Term frequency (TF)
    private double tf(List<String> toks, String term) {
        double result = 0;
        for (String tok: toks) {
            if (term.equals(tok))
                result++;
        }
        return result / toks.size();
    }

    // Inverse document frequency (IDF)
    private double idf(List<List<String>> docs, String term) {
        double cnt = 0;
        for (List<String> toks: docs) {
            for (String tok: toks) {
                if (term.equals(tok)) {
                    cnt++;
                    break;
                }
            }
        }
        return Math.log(docs.size() / cnt);
    }

    // Term frequency–inverse document frequency (TF-IDF)
    public double tfIdf(List<String> toks, List<List<String>> docs, String term) {
        return tf(toks, term) * idf(docs, term);
    }

    // Sort map by values
    @SuppressWarnings("unchecked")
    public static HashMap sortByValues(Map map) { 

        List list = new LinkedList(map.entrySet());

        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                    .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();

            sortedHashMap.put(entry.getKey(), entry.getValue());
        } 
        return sortedHashMap;
    }

}


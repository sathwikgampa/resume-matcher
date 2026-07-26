import java.util.*;

public class SimilarityEngine {

    public Map<String, Integer> buildFrequencyMap(List<String> tokens) {
        Map<String, Integer> freq = new HashMap<>();
        for (String token : tokens) {
            freq.put(token, freq.getOrDefault(token, 0) + 1);
        }
        return freq;
    }

    public double cosineSimilarity(Map<String, Integer> vecA, Map<String, Integer> vecB) {
        Set<String> masterKeys = new HashSet<>();
        masterKeys.addAll(vecA.keySet());
        masterKeys.addAll(vecB.keySet());

        long dotProduct = 0;
        long sumSqA = 0;
        long sumSqB = 0;

        for (String key : masterKeys) {
            int a = vecA.getOrDefault(key, 0);
            int b = vecB.getOrDefault(key, 0);
            dotProduct += (long) a * b;
            sumSqA += (long) a * a;
            sumSqB += (long) b * b;
        }

        double magnitudeA = Math.sqrt(sumSqA);
        double magnitudeB = Math.sqrt(sumSqB);

        if (magnitudeA == 0 || magnitudeB == 0) {
            return 0.0;
        }

        return (dotProduct / (magnitudeA * magnitudeB)) * 100;
    }

    public int countMatchedKeywords(Map<String, Integer> jobVec, Map<String, Integer> resumeVec) {
        int count = 0;
        for (String key : jobVec.keySet()) {
            if (resumeVec.containsKey(key)) count++;
        }
        return count;
    }

    public Set<String> getMatchedKeywords(Map<String, Integer> jobVec, Map<String, Integer> resumeVec) {
        Set<String> matched = new TreeSet<>();
        for (String key : jobVec.keySet()) {
            if (resumeVec.containsKey(key)) {
                matched.add(key);
            }
        }
        return matched;
    }

    public Set<String> getMissingKeywords(Map<String, Integer> jobVec, Map<String, Integer> resumeVec) {
        Set<String> missing = new TreeSet<>();
        for (String key : jobVec.keySet()) {
            if (!resumeVec.containsKey(key)) {
                missing.add(key);
            }
        }
        return missing;
    }
}
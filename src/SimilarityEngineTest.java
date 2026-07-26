import java.util.List;
import java.util.Map;

public class SimilarityEngineTest {
    public static void main(String[] args) {
        TextProcessor tp = new TextProcessor();
        SimilarityEngine engine = new SimilarityEngine();

        String jobDescription = "We need a Senior Java Developer with strong Spring Boot and SQL experience.";
        String matchingResume = "Java Developer with 5 years experience in Spring Boot and SQL databases.";
        String unrelatedResume = "Graphic designer skilled in Photoshop, Illustrator, and branding.";

        Map<String, Integer> jobVec = engine.buildFrequencyMap(tp.tokenize(jobDescription));
        Map<String, Integer> matchVec = engine.buildFrequencyMap(tp.tokenize(matchingResume));
        Map<String, Integer> unrelatedVec = engine.buildFrequencyMap(tp.tokenize(unrelatedResume));

        double scoreMatch = engine.cosineSimilarity(jobVec, matchVec);
        double scoreUnrelated = engine.cosineSimilarity(jobVec, unrelatedVec);

        System.out.printf("Matching resume score: %.2f%n", scoreMatch);
        System.out.printf("Unrelated resume score: %.2f%n", scoreUnrelated);
    }
}
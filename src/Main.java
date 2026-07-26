import java.util.*;

public class Main {
    public static void main(String[] args) {
        TextProcessor processor = new TextProcessor();
        SimilarityEngine engine = new SimilarityEngine();

        String jobDescription = "We are hiring a Senior Java Developer with strong experience in "
                + "Spring Boot, REST APIs, SQL databases, and microservices architecture. "
                + "Familiarity with Docker and Kubernetes is a plus.";

        Map<String, String> candidates = new LinkedHashMap<>();
        candidates.put("Alice - Java Engineer", "Backend developer with 6 years building REST APIs "
                + "using Spring Boot, working with SQL databases and Docker containers.");
        candidates.put("Bob - Graphic Designer", "Creative designer skilled in Photoshop, Illustrator, "
                + "branding, and typography for print and digital media.");
        candidates.put("Carol - Finance Analyst", "Financial analyst experienced in forecasting, "
                + "budgeting, Excel modeling, and quarterly reporting.");

        Map<String, Integer> jobVec = engine.buildFrequencyMap(processor.tokenize(jobDescription));

        // Store results so we can sort before printing
        List<String[]> results = new ArrayList<>(); // [name, matches, score] as strings for now

        for (var entry : candidates.entrySet()) {
            Map<String, Integer> resumeVec = engine.buildFrequencyMap(processor.tokenize(entry.getValue()));
            double score = engine.cosineSimilarity(jobVec, resumeVec);
            int matches = engine.countMatchedKeywords(jobVec, resumeVec);
            results.add(new String[]{entry.getKey(), String.valueOf(matches), String.valueOf(score)});
        }

        // Sort descending by score (index 2, parsed as double)
        results.sort((a, b) -> Double.compare(Double.parseDouble(b[2]), Double.parseDouble(a[2])));

        System.out.printf("%-25s %-10s %-10s%n", "Candidate", "Matches", "Score");
        System.out.println("-".repeat(45));
        for (String[] row : results) {
            double score = Double.parseDouble(row[2]);
            System.out.printf("%-25s %-10s %-10.2f%n", row[0], row[1], score);
        }
    }
}
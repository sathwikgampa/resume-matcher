import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        TextProcessor processor = new TextProcessor();
        SimilarityEngine engine = new SimilarityEngine();
        WebScraper scraper = new WebScraper();

        String jobDescription;

        // Toggle this to switch between live scraping and hardcoded text
        boolean useLiveScraping = false;

        if (useLiveScraping) {
            String jobUrl = "https://boards.greenhouse.io/somecompany/jobs/1234567"; // replace with a real URL
            jobDescription = scraper.fetchPageText(jobUrl);
        } else {
            jobDescription = "We are hiring a Senior Java Developer with strong experience in "
                    + "Spring Boot, REST APIs, SQL databases, and microservices architecture. "
                    + "Familiarity with Docker and Kubernetes is a plus.";
        }

        Map<String, String> candidates = new LinkedHashMap<>();
        candidates.put("Alice - Java Engineer", "Backend developer with 6 years building REST APIs "
                + "using Spring Boot, working with SQL databases and Docker containers.");
        candidates.put("Bob - Graphic Designer", "Creative designer skilled in Photoshop, Illustrator, "
                + "branding, and typography for print and digital media.");
        candidates.put("Carol - Finance Analyst", "Financial analyst experienced in forecasting, "
                + "budgeting, Excel modeling, and quarterly reporting.");

        Map<String, Integer> jobVec = engine.buildFrequencyMap(processor.tokenize(jobDescription));

        // Store results for the summary table
        List<String[]> results = new ArrayList<>();

        // Store detailed breakdowns to print after the table
        Map<String, Map<String, Integer>> resumeVectors = new LinkedHashMap<>();

        for (var entry : candidates.entrySet()) {
            Map<String, Integer> resumeVec = engine.buildFrequencyMap(processor.tokenize(entry.getValue()));
            resumeVectors.put(entry.getKey(), resumeVec);

            double score = engine.cosineSimilarity(jobVec, resumeVec);
            int matches = engine.countMatchedKeywords(jobVec, resumeVec);
            results.add(new String[]{entry.getKey(), String.valueOf(matches), String.valueOf(score)});
        }

        // Sort descending by score
        results.sort((a, b) -> Double.compare(Double.parseDouble(b[2]), Double.parseDouble(a[2])));

        // Print ranked summary table
        System.out.printf("%-25s %-10s %-10s%n", "Candidate", "Matches", "Score");
        System.out.println("-".repeat(45));
        for (String[] row : results) {
            double score = Double.parseDouble(row[2]);
            System.out.printf("%-25s %-10s %-10.2f%n", row[0], row[1], score);
        }

        // Print detailed breakdown per candidate (in the same sorted order)
        System.out.println("\n=== Detailed Breakdown ===");
        for (String[] row : results) {
            String name = row[0];
            double score = Double.parseDouble(row[2]);
            Map<String, Integer> resumeVec = resumeVectors.get(name);

            Set<String> strengths = engine.getMatchedKeywords(jobVec, resumeVec);
            Set<String> gaps = engine.getMissingKeywords(jobVec, resumeVec);

            System.out.println("\n" + name);
            System.out.println("  Score: " + String.format("%.2f", score) + "%");
            System.out.println("  Key Strengths: " + strengths);
            System.out.println("  Critical Gaps: " + gaps);
        }
    }
}
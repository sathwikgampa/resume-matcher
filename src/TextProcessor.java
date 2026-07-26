import java.util.*;

public class TextProcessor {

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "for", "with", "this", "which", "that",
            "is", "are", "was", "were", "will", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "our", "we", "you",
            "your", "they", "their", "it", "its", "as", "at", "by", "from",
            "in", "into", "of", "on", "or", "to", "up", "about", "who",
            "what", "when", "where", "why", "how", "all", "each", "other",
            "than", "then", "there", "these", "those", "so", "if"
    ));

    public List<String> tokenize(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ArrayList<>();
        }

        String lower = rawText.toLowerCase();

        // Keep letters, spaces, + and # so "c++" and "c#" survive
        String cleaned = lower.replaceAll("[^a-z\\s+#]", " ");

        String[] rawTokens = cleaned.trim().split("\\s+");

        List<String> filtered = new ArrayList<>();
        for (String token : rawTokens) {
            if (token.length() <= 1) continue;
            if (STOPWORDS.contains(token)) continue;
            filtered.add(token);
        }
        return filtered;
    }
}
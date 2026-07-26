import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server {

    static TextProcessor processor = new TextProcessor();
    static SimilarityEngine engine = new SimilarityEngine();
    static WebScraper scraper = new WebScraper();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", Server::serveHomePage);
        server.createContext("/match", Server::handleMatch);

        server.setExecutor(null);
        server.start();

        System.out.println("Server running at http://localhost:8080");
    }

    // Serves a basic HTML form (temporary UI — will be replaced by a proper frontend later)
    static void serveHomePage(HttpExchange exchange) throws IOException {
        String html = """
            <html>
            <head><title>Resume Matcher</title></head>
            <body style="font-family: sans-serif; max-width: 600px; margin: 40px auto;">
                <h2>Resume Matcher</h2>
                <form action="/match" method="POST">
                    <label>Job Posting URL:</label><br>
                    <input type="text" name="jobUrl" style="width: 100%;"><br><br>
                    <label>Paste Resume Text:</label><br>
                    <textarea name="resumeText" rows="10" style="width: 100%;"></textarea><br><br>
                    <button type="submit">Get Match Score</button>
                </form>
            </body>
            </html>
            """;
        sendResponse(exchange, 200, html, "text/html");
    }

    // Core API endpoint — returns JSON so any future frontend (React, vanilla JS, mobile) can call it
    static void handleMatch(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseFormData(body);

        String jobUrl = params.getOrDefault("jobUrl", "");
        String resumeText = params.getOrDefault("resumeText", "");

        String jobDescription;
        try {
            jobDescription = scraper.fetchPageText(jobUrl);
        } catch (Exception e) {
            String errorJson = "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}";
            sendResponse(exchange, 200, errorJson, "application/json");
            return;
        }

        var jobVec = engine.buildFrequencyMap(processor.tokenize(jobDescription));
        var resumeVec = engine.buildFrequencyMap(processor.tokenize(resumeText));

        double score = engine.cosineSimilarity(jobVec, resumeVec);
        int matches = engine.countMatchedKeywords(jobVec, resumeVec);
        Set<String> strengths = engine.getMatchedKeywords(jobVec, resumeVec);
        Set<String> gaps = engine.getMissingKeywords(jobVec, resumeVec);

        String json = String.format("""
            {
              "score": %.2f,
              "matchedCount": %d,
              "keyStrengths": %s,
              "criticalGaps": %s
            }
            """,
                score, matches, toJsonArray(strengths), toJsonArray(gaps));

        sendResponse(exchange, 200, json, "application/json");
    }

    // Minimal x-www-form-urlencoded parser (no dependencies)
    static Map<String, String> parseFormData(String body) {
        Map<String, String> map = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = java.net.URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                map.put(key, value);
            }
        }
        return map;
    }

    // Converts a Set<String> into a JSON array string, e.g. ["java","spring"]
    static String toJsonArray(Set<String> items) {
        StringBuilder sb = new StringBuilder("[");
        int i = 0;
        for (String item : items) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(item)).append("\"");
            i++;
        }
        sb.append("]");
        return sb.toString();
    }

    static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    static void sendResponse(HttpExchange exchange, int statusCode, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
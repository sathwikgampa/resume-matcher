import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebScraper {

    public String fetchPageText(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String html = response.body();

        String noScripts = html.replaceAll("(?s)<script.*?</script>", " ");
        String noStyles = noScripts.replaceAll("(?s)<style.*?</style>", " ");
        String noTags = noStyles.replaceAll("<[^>]*>", " ");

        return noTags.replaceAll("\\s+", " ").trim();
    }
}
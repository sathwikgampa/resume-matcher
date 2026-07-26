public class WebScraperTest {
    public static void main(String[] args) throws Exception {
        WebScraper scraper = new WebScraper();
        String text = scraper.fetchPageText("https://example.com");
        System.out.println(text);
    }
}
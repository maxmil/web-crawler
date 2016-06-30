package just.another.webcrawler;

/**
 * A provider that allows easy mocking of PageCrawlers for testing
 */
public class PageCrawlerProvider {

    public PageCrawler newPageCrawler(String baseUrl, PageProcessor pageProcessor, UrlReader urlReader) {
        return new PageCrawler(baseUrl, pageProcessor, urlReader);
    }
}

package just.another.webcrawler;

public class PageCrawlerProvider {

    public PageCrawler newPageCrawler(String baseUrl, PageProcessor pageProcessor, UrlReader urlReader) {
        return new PageCrawler(baseUrl, pageProcessor, urlReader);
    }
}

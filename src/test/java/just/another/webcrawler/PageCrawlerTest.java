package just.another.webcrawler;

import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

public class PageCrawlerTest {

    private static final String BASE_URL = "http://www.someurl.com";

    private PageProcessor pageProcessor = mock(PageProcessor.class);
    private UrlReader urlReader = mock(UrlReader.class);
    private PageCrawler pageCrawler = new PageCrawler(BASE_URL, pageProcessor, urlReader);

    @Test
    public void crawlsAbsoluteInternalLink() throws Exception {
        String url = BASE_URL + "/page";
        String content = "<a href=\"" + url + "\" />";
        setupProcessorAndReader(content);

        pageCrawler.run();

        verify(pageProcessor).submitResult(BASE_URL, resultsFromInternalLink(url));
    }

    @Test
    public void crawlsRelativeInternalLink() throws Exception {
        String url = "page";
        String content = "<a href=\"" + url + "\" />";
        setupProcessorAndReader(content);

        pageCrawler.run();

        verify(pageProcessor).submitResult(BASE_URL, resultsFromInternalLink(BASE_URL + "/" + url));
    }

    @Test
    public void ignoresQueryStringForInternalLink() throws Exception {
        String url = BASE_URL + "/page";
        String content = "<a someotherprop='' href='" + url + "?withquerystring' />";
        setupProcessorAndReader(content);

        pageCrawler.run();

        verify(pageProcessor).submitResult(BASE_URL, resultsFromInternalLink(url));
    }

    @Test
    public void ignoresAnchorForInternalLink() throws Exception {
        String url = BASE_URL + "/page";
        String content = "<a someotherprop='' href='" + url + "#withquerystring' />";
        setupProcessorAndReader(content);

        pageCrawler.run();

        verify(pageProcessor).submitResult(BASE_URL, resultsFromInternalLink(url));
    }

    @Test
    public void ignoresLinksThatLinkToThemselves() throws Exception {
        String content = "<a href=\"" + BASE_URL + "\" />";
        setupProcessorAndReader(content);

        pageCrawler.run();

        verify(pageProcessor).submitResult(BASE_URL, CrawlResult.EMPTY);
    }

    @Test
    public void crawlsExternalLink() throws Exception {
        String url = "http://www.twitter.com";
        String content = "<a href=\"" + url + "\" />";
        setupProcessorAndReader(content);

        pageCrawler.run();

        verify(pageProcessor).submitResult(BASE_URL, resultsFromExternalLink(url));
    }

    @Test
    public void crawlsImage() throws Exception {
        String url = "image.gif";
        String content = "<img src=\"" + url + "\" />";
        setupProcessorAndReader(content);

        pageCrawler.run();

        verify(pageProcessor).submitResult(BASE_URL, resultsFromImage(BASE_URL + "/" + url));
    }

    private void setupProcessorAndReader(String urlContent) throws InterruptedException, IOException {
        when(pageProcessor.getNextPage()).thenReturn(BASE_URL, PageProcessor.COMPLETE);
        when(urlReader.read(BASE_URL)).thenReturn(urlContent);
    }

    private CrawlResult resultsFromInternalLink(String link) {
        return new CrawlResult(new HashSet<>(singletonList(link)), emptySet(), emptySet());
    }

    private CrawlResult resultsFromExternalLink(String link) {
        return new CrawlResult(emptySet(), new HashSet<>(singletonList(link)), emptySet());
    }

    private CrawlResult resultsFromImage(String image) {
        return new CrawlResult(emptySet(), emptySet(), new HashSet<>(singletonList(image)));
    }

}


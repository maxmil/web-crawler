package just.another.webcrawler;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WebCrawlerTest {

    private static final String BASE_URL = "http://www.someurl.com";

    private PageProcessor pageProcessor = mock(PageProcessor.class);
    private PageCrawlerProvider pageCrawlerProvider = mock(PageCrawlerProvider.class);
    private PageCrawler pageCrawler = mock(PageCrawler.class);

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private WebCrawler webCrawler;

    @Before
    public void setUp() throws Exception {
        when(pageCrawlerProvider.newPageCrawler(anyString(), any(PageProcessor.class), any(UrlReader.class))).thenReturn(pageCrawler);

        webCrawler = new WebCrawler(pageProcessor, BASE_URL, new PrintStream(baos), 1, pageCrawlerProvider);
    }

    @Test
    public void simpleSiteTest() throws Exception {
        when(pageProcessor.getSiteMap()).thenReturn(
                Page.newBuilder(BASE_URL)
                        .withExternalLinks("external1", "external2")
                        .withImages("image1", "image2")
                        .withInternalLinks(
                                Page.newBuilder("internal1")
                                        .withImages("image1.1")
                                        .withExternalLinks("external1.1")
                                        .build()
                        )
                        .build());

        webCrawler.crawl();

        String expected = "http://www.someurl.com\n" +
                "  Images\n" +
                "    image1\n" +
                "    image2\n" +
                "  ExternalLinks\n" +
                "    external2\n" +
                "    external1\n" +
                "  Internal links\n" +
                "    internal1\n" +
                "      Images\n" +
                "        image1.1\n" +
                "      ExternalLinks\n" +
                "        external1.1\n";

        assertThat(baos.toString(), is(expected));
    }
}
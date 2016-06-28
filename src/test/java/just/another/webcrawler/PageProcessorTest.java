package just.another.webcrawler;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static just.another.webcrawler.PageProcessor.COMPLETE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class pageProcessorTest {

    private static final String BASE_URL = "http://www.someurl.com";

    private PageProcessor pageProcessor = new PageProcessor(BASE_URL);

    @Test
    public void getsStartingUrlAsFirstTask() throws Exception {

        String firstTask = pageProcessor.getNextPage();

        assertThat(firstTask, is(BASE_URL));
    }

    @Test
    public void siteMapForPageWithAnExternalLink() throws Exception {
        String externalLink = "http://www.google.com";

        String firstTask = pageProcessor.getNextPage();
        pageProcessor.submitResult(firstTask, new CrawlResult(emptySet(), asSet(externalLink), emptySet()));

        Page expectedPage = new Page(BASE_URL, emptySet(), asSet(externalLink), emptySet());
        assertThat(pageProcessor.getSiteMap(), is(expectedPage));
    }

    @Test
    public void siteMapForPageWithAnImage() throws Exception {
        String image = "http://www.someurl.com/image.gif";

        String firstTask = pageProcessor.getNextPage();
        pageProcessor.submitResult(firstTask, new CrawlResult(emptySet(), emptySet(), asSet(image)));

        Page expectedPage = new Page(BASE_URL, emptySet(), emptySet(), asSet(image));
        assertThat(pageProcessor.getSiteMap(), is(expectedPage));
    }

    @Test
    public void siteMapForPageWithAnInternalLink() throws Exception {
        String internalLink = BASE_URL + "/internal-page";

        String firstTask = pageProcessor.getNextPage();
        pageProcessor.submitResult(firstTask, new CrawlResult(asSet(internalLink), emptySet(), emptySet()));
        String secondTask = pageProcessor.getNextPage();
        pageProcessor.submitResult(secondTask, new CrawlResult(emptySet(), emptySet(), emptySet()));

        Page expectedPage = new Page(BASE_URL, asSet(new Page(internalLink, emptySet(), emptySet(), emptySet())), emptySet(), emptySet());
        assertThat(pageProcessor.getSiteMap(), is(expectedPage));
    }

    @Test
    public void siteMapForPageWithAllResourcesAndChildPage() throws Exception {

        String firstTask = pageProcessor.getNextPage();
        pageProcessor.submitResult(firstTask, new CrawlResult(
                asSet(BASE_URL + "/internal-page"),
                asSet("http://www.google.com"),
                asSet("http://www.someurl.com/image.gif")
        ));
        String secondTask = pageProcessor.getNextPage();
        pageProcessor.submitResult(secondTask, new CrawlResult(
                emptySet(),
                asSet("http://www.twitter.com"),
                asSet("http://www.someurl.com/image.gif")
        ));

        Page expectedPage = new Page(
                BASE_URL,
                asSet(new Page(
                        BASE_URL + "/internal-page",
                        emptySet(),
                        asSet("http://www.twitter.com"),
                        asSet("http://www.someurl.com/image.gif"))),
                asSet("http://www.google.com"),
                asSet("http://www.someurl.com/image.gif"));
        assertThat(pageProcessor.getSiteMap(), is(expectedPage));
    }

    @Test
    public void circularReferencesRemovedFromSiteMap() throws Exception {
        String firstLink = BASE_URL + "/first";
        String secondLink = BASE_URL + "/second";

        pageProcessor.submitResult(pageProcessor.getNextPage(), new CrawlResult(asSet(firstLink), emptySet(), emptySet()));
        pageProcessor.submitResult(pageProcessor.getNextPage(), new CrawlResult(asSet(secondLink), emptySet(), emptySet()));
        pageProcessor.submitResult(pageProcessor.getNextPage(), new CrawlResult(asSet(firstLink), emptySet(), emptySet()));

        assertThat(pageProcessor.getNextPage(), is(COMPLETE));
        Page expectedSiteMap = new Page(
                BASE_URL,
                asSet(new Page(
                        firstLink,
                        asSet(new Page(secondLink, emptySet(), emptySet(), emptySet())),
                        emptySet(),
                        emptySet())),
                emptySet(),
                emptySet());
        assertThat(pageProcessor.getSiteMap(), is(expectedSiteMap));
    }

    @Test
    public void whenNoMoreInternalLinks_completeRetrievedFromQueue() throws Exception {
        String internalLink = BASE_URL + "/internal-page";

        String firstTask = pageProcessor.getNextPage();
        pageProcessor.submitResult(firstTask, new CrawlResult(asSet(internalLink), emptySet(), emptySet()));
        String secondTask = pageProcessor.getNextPage();
        pageProcessor.submitResult(secondTask, new CrawlResult(emptySet(), emptySet(), emptySet()));
        String noMorePages = pageProcessor.getNextPage();

        assertThat(noMorePages, is(COMPLETE));

    }

    @SafeVarargs
    private static <T> Set<T> asSet(T... items) {
        return new HashSet<>(Arrays.asList(items));
    }
}
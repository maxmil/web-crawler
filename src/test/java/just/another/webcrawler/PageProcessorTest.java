package just.another.webcrawler;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class pageProcessorTest {

    private static final String BASE_URL = "http://www.someurl.com";

    private PageProcessor taskManager = new PageProcessor(BASE_URL);

    @Test
    public void getsStartingUrlAsFirstTask() throws Exception {

        String firstTask = taskManager.getNextPage();

        assertThat(firstTask, is(BASE_URL));
    }

    @Test
    public void siteMapForPageWithAnExternalLink() throws Exception {
        String externalLink = "http://www.google.com";

        String firstTask = taskManager.getNextPage();
        taskManager.submitResult(firstTask, new CrawlResult(emptySet(), asSet(externalLink), emptySet()));

        Page expectedPage = new Page(BASE_URL, emptySet(), asSet(externalLink), emptySet());
        assertThat(taskManager.getSiteMap(), is(expectedPage));
    }

    @Test
    public void siteMapForPageWithAnImage() throws Exception {
        String image = "http://www.someurl.com/image.gif";

        String firstTask = taskManager.getNextPage();
        taskManager.submitResult(firstTask, new CrawlResult(emptySet(), emptySet(), asSet(image)));

        Page expectedPage = new Page(BASE_URL, emptySet(), emptySet(), asSet(image));
        assertThat(taskManager.getSiteMap(), is(expectedPage));
    }

    @Test
    public void siteMapForPageWithAnInternalLink() throws Exception {
        String internalLink = BASE_URL + "/internal-page";

        String firstTask = taskManager.getNextPage();
        taskManager.submitResult(firstTask, new CrawlResult(asSet(internalLink), emptySet(), emptySet()));
        String secondTask = taskManager.getNextPage();
        taskManager.submitResult(secondTask, new CrawlResult(emptySet(), emptySet(), emptySet()));

        Page expectedPage = new Page(BASE_URL, asSet(new Page(internalLink, emptySet(), emptySet(), emptySet())), emptySet(), emptySet());
        assertThat(taskManager.getSiteMap(), is(expectedPage));
    }

    @Test
    public void siteMapForPageWithAllResourcesAndChildPage() throws Exception {

        String firstTask = taskManager.getNextPage();
        taskManager.submitResult(firstTask, new CrawlResult(
                asSet(BASE_URL + "/internal-page"),
                asSet("http://www.google.com"),
                asSet("http://www.someurl.com/image.gif")
        ));
        String secondTask = taskManager.getNextPage();
        taskManager.submitResult(secondTask, new CrawlResult(
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
        assertThat(taskManager.getSiteMap(), is(expectedPage));
    }

    @Test
    public void whenNoMoreInternalLinks_completeRetrievedFromQueue() throws Exception {
        String internalLink = BASE_URL + "/internal-page";

        String firstTask = taskManager.getNextPage();
        taskManager.submitResult(firstTask, new CrawlResult(asSet(internalLink), emptySet(), emptySet()));
        String secondTask = taskManager.getNextPage();
        taskManager.submitResult(secondTask, new CrawlResult(emptySet(), emptySet(), emptySet()));
        String noMorePages = taskManager.getNextPage();

        assertThat(noMorePages, is(PageProcessor.COMPLETE));

    }

    @SafeVarargs
    private static <T> Set<T> asSet(T... items) {
        return new HashSet<>(Arrays.asList(items));
    }
}
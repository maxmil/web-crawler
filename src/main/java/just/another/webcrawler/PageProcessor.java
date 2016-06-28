package just.another.webcrawler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.LongAdder;

import static java.util.stream.Collectors.toSet;

public class PageProcessor {

    // Used as a poisoned pill to instruct consumers of the queue that they can shut down
    public static final String COMPLETE = "::COMPLETE::";

    private final String baseUrl;
    private final Map<String, CrawlResult> results = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<String> pageQueue = new LinkedBlockingQueue<>();
    private final LongAdder unprocessedPages = new LongAdder();

    public PageProcessor(String baseUrl) {
        this.baseUrl = baseUrl;
        addLinkToQueue(baseUrl);
    }

    public String getNextPage() throws InterruptedException {
        return pageQueue.take();
    }

    public void submitResult(String url, CrawlResult result) {
        results.put(url, result);
        for (String link : result.getInternalLinks()) {
            addLinkToQueue(link);
        }

        unprocessedPages.decrement();
        if(unprocessedPages.longValue() == 0) {
            pageQueue.offer(COMPLETE);
        }
    }

    public Page getSiteMap() {
        return getPageTree(baseUrl, new HashSet<>());
    }

    private void addLinkToQueue(String url) {
        if (!results.containsKey(url)) {
            unprocessedPages.increment();
            pageQueue.offer(url);
        }
    }

    private Page getPageTree(String url, Set<String> parentLinks) {
        CrawlResult crawlResult = results.get(url);
        parentLinks.add(url);
        Set<Page> pages = crawlResult.getInternalLinks().stream()
                .filter(link -> !parentLinks.contains(link))
                .map(link -> getPageTree(link, parentLinks))
                .collect(toSet());
        return new Page(url, pages, crawlResult.getExternalLinks(), crawlResult.getImages());
    }

    private boolean isInternal(String link) {
        return link.indexOf(baseUrl) == 0 || (link.length() < 4 || !link.substring(0, 4).toLowerCase().equals("http"));
    }
}

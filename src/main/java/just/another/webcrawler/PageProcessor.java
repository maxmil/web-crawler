package just.another.webcrawler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toSet;

/**
 * Uses a BlockingQueue to act as a producer of pages that can be crawled concurrently.
 *
 * All CrawlResults are gathered in a Map from which the site map can be constructed.
 */
public class PageProcessor {

    // Used as a poisoned pill to instruct consumers of the queue that they can shut down
    public static final String COMPLETE = "::COMPLETE::";

    private final String baseUrl;
    private final Map<String, CrawlResult> results = new ConcurrentHashMap<>();
    private final BlockingQueue<String> pageQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger unprocessedPages = new AtomicInteger();
    private final int nConsumers;

    public PageProcessor(String baseUrl, int nConsumers) {
        this.baseUrl = baseUrl;
        this.nConsumers = nConsumers;
        addLinkToQueue(baseUrl);
    }

    public String getNextPage() {
        try {
            return pageQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to get next page from process queue", e);
        }
    }

    public void submitResult(String url, CrawlResult result) {
        results.put(url, result);
        result.getInternalLinks().forEach(this::addLinkToQueue);
        pageComplete();
    }

    public void submitError() {
        pageComplete();
    }

    public Page getSiteMap() {
        return getPageTree(baseUrl, new HashSet<>());
    }

    private void addLinkToQueue(String url) {
        if (results.putIfAbsent(url, CrawlResult.PENDING) == null) {
            unprocessedPages.incrementAndGet();
            try {
                pageQueue.put(url);
            } catch (InterruptedException e) {
                throw new RuntimeException("Unable to append to page process queue", e);
            }
        }
    }

    private void pageComplete() {
        if (unprocessedPages.decrementAndGet() == 0) {
            for (int i = 0; i < nConsumers; i++) {
                try {
                    pageQueue.put(COMPLETE);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Unable to append to page process queue", e);
                }
            }
        }
    }

    private Page getPageTree(String url, Set<String> parentLinks) {
        CrawlResult crawlResult = results.get(url);
        if(crawlResult == CrawlResult.PENDING) {
            return null;
        }
        parentLinks.add(url);
        Set<Page> pages = crawlResult.getInternalLinks().stream()
                .filter(link -> !parentLinks.contains(link)) // Filter out circular references
                .map(link -> getPageTree(link, parentLinks))
                .filter(page -> page != null)
                .collect(toSet());
        return new Page(url, pages, crawlResult.getExternalLinks(), crawlResult.getImages());
    }
}

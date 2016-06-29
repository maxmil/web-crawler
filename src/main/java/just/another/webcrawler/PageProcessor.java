package just.another.webcrawler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toSet;

public class PageProcessor {

    // Used as a poisoned pill to instruct consumers of the queue that they can shut down
    public static final String COMPLETE = "::COMPLETE::";

    private final String baseUrl;
    private final Map<String, CrawlResult> results = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<String> pageQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger unprocessedPages = new AtomicInteger();
    private final int nConsumers;

    public PageProcessor(String baseUrl, int nConsumers) {
        this.baseUrl = baseUrl;
        this.nConsumers = nConsumers;
        addLinkToQueue(baseUrl);
    }

    public String getNextPage() {
        try {
            String url = pageQueue.take();
            results.put(url, CrawlResult.PENDING);
            return url;
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
        if (results.putIfAbsent(url, CrawlResult.EMPTY) == null) {
            unprocessedPages.incrementAndGet();
            try {
                pageQueue.put(url);
            } catch (InterruptedException e) {
                throw new RuntimeException("Unable to append to page process queue", e);
            }
        }
    }

    private void pageComplete() {
        unprocessedPages.decrementAndGet();
        if (unprocessedPages.get() == 0) {
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

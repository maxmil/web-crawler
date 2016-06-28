package just.another.webcrawler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class PageProcessor {

    private final String baseUrl;
    private final Map<String, CrawlResult> results = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<String> pageQueue = new LinkedBlockingQueue<>();
    private final LongAdder pendingTasks = new LongAdder();

    public PageProcessor(String baseUrl) {
        this.baseUrl = baseUrl;
        addTask(baseUrl);
    }

    public String getNextPage() throws InterruptedException {
        return pageQueue.take();
    }

    public void submitResult(String url, CrawlResult result) {
        results.put(url, result);
        for (String link : result.getInternalLinks()) {
            addTask(link);
        }

        // get the page with set result, for each internal link create a new page, add to the queue and increment pending pageQueue

        // decrement pending pageQueue

        // add poisoned pills if pending pageQueue == 0

    }

    private void addTask(String url) {
        if (!results.containsKey(url)) {
            pendingTasks.increment();
            pageQueue.offer(url);
        }
    }

    public Page getSiteMap() {
        return getPageTree(baseUrl);
    }

    private Page getPageTree(String url) {
        CrawlResult crawlResult = results.get(url);

        //TODO: If page has already been crawled

        Set<Page> pages = crawlResult.getInternalLinks().stream().map(this::getPageTree).collect(Collectors.toSet());
        return new Page(url, pages, crawlResult.getExternalLinks(), crawlResult.getImages());
    }

    private boolean isInternal(String link) {
        return link.indexOf(baseUrl) == 0 || (link.length() < 4 || !link.substring(0, 4).toLowerCase().equals("http"));
    }
}

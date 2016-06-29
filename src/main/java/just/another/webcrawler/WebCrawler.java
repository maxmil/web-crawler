package just.another.webcrawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.exit;
import static java.util.stream.Collectors.joining;

public class WebCrawler {

    private static final Logger logger = LoggerFactory.getLogger(WebCrawler.class);

    private static final String INDENT = "  ";
    private static final String OUTPUT_FILE = "sitemap.txt";

    private final PageProcessor pageProcessor;
    private final String baseUrl;
    private final int nThreads;
    private final CountDownLatch latch;
    private final PrintStream out;
    private final PageCrawlerProvider pageCrawlerProvider;

    public static void main(String[] args) throws FileNotFoundException {
        String baseUrl;
        int nThreads;
        try {
            baseUrl = args[0];
            nThreads = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        } catch (Exception e) {
            System.err.println("Usage: web-crawler <site> [threads]\n" +
                    "   site     The absolute URL of the site to crawl\n" +
                    "   threads  The number of concurrent threads. Defaults to 1. Some sites may refuse to serve content if too many requests are made from the same ip.");
            exit(0);
            return;
        }

        PageProcessor pageProcessor = new PageProcessor(baseUrl, nThreads);
        WebCrawler webCrawler = new WebCrawler(pageProcessor, baseUrl, new PrintStream(new FileOutputStream(OUTPUT_FILE)), nThreads, new PageCrawlerProvider());
        webCrawler.crawl();
    }

    WebCrawler(PageProcessor pageProcessor, String baseUrl, PrintStream out, int nThreads, PageCrawlerProvider pageCrawlerProvider) {
        this.baseUrl = baseUrl;
        this.pageProcessor = pageProcessor;
        this.pageCrawlerProvider = pageCrawlerProvider;
        this.nThreads = nThreads;
        this.out = out;
        latch = new CountDownLatch(nThreads);
    }

    public void crawl() {
        UrlReader urlReader = new UrlReader();
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            executorService.submit(() -> {
                PageCrawler crawler = pageCrawlerProvider.newPageCrawler(baseUrl, pageProcessor, urlReader);
                crawler.run();
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for crawlers to complete", e);
        } finally {
            executorService.shutdown();
        }

        Page siteMap = pageProcessor.getSiteMap();

        logger.info("Writing site map to " + OUTPUT_FILE);
        outputPage(siteMap, "");
    }

    private void outputPage(Page page, String prefix) {
        out.println(prefix + page.getName());
        prefix += INDENT;
        outputImages(page, prefix);
        outputExternalLinks(page, prefix);
        outputInternalLinks(page, prefix);
    }

    private void outputImages(Page page, String prefix) {
        if (!page.getImages().isEmpty()) {
            out.println(prefix + "Images");
            prefix += INDENT;
            out.print(page.getImages().stream().collect(joining("\n" + prefix, prefix, "\n")));
        }
    }

    private void outputExternalLinks(Page page, String prefix) {
        if (!page.getExternalLinks().isEmpty()) {
            out.println(prefix + "ExternalLinks");
            prefix += INDENT;
            out.print(page.getExternalLinks().stream().collect(joining("\n" + prefix, prefix, "\n")));
        }
    }

    private void outputInternalLinks(Page page, String prefix) {
        if (!page.getInternalLinks().isEmpty()) {
            out.println(prefix + "Internal links");
            for (Page internalLink : page.getInternalLinks()) {
                outputPage(internalLink, prefix + INDENT);
            }
        }
    }
}

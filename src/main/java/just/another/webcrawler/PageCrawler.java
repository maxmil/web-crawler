package just.another.webcrawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageCrawler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PageCrawler.class);

    private static final Pattern LINK_PATTERN = Pattern.compile("(?i)<a\\s+[^>]*?href\\s*=\\s*((\"[^\"]*\")|('[^']*'))");
    private static final Pattern IMG_PATTERN = Pattern.compile("(?i)<img\\s+[^>]*?src\\s*=\\s*((\"[^\"]*\")|('[^']*'))");

    private final String baseUrl;
    private final PageProcessor pageProcessor;
    private final UrlReader urlReader;

    public PageCrawler(String baseUrl, PageProcessor pageProcessor, UrlReader urlReader) {
        this.baseUrl = baseUrl;
        this.pageProcessor = pageProcessor;
        this.urlReader = urlReader;
    }

    public void run() {
        String url = null;
        while (true) {
            try {
                url = pageProcessor.getNextPage();
                if (url.equals(PageProcessor.COMPLETE)) {
                    break;
                } else {
                    pageProcessor.submitResult(url, crawlPage(url));
                }
            } catch (Exception e) {
                logger.info("Unable to process url " + url);
                pageProcessor.submitError();
            }
        }
    }

    private CrawlResult crawlPage(String url) throws IOException {
        logger.info("Crawling " + url);
        String content = urlReader.read(url);
        Set<String> internalLinks = new HashSet<>();
        Set<String> externalLinks = new HashSet<>();
        Matcher linkMatcher = LINK_PATTERN.matcher(content);
        while (linkMatcher.find()) {
            String link = sanitizeUrl(url, linkMatcher.group(1));
            if (link != null && link.length() > 0 && !link.equals(url)) {
                if (isInternal(link)) {
                    internalLinks.add(link);
                } else {
                    externalLinks.add(link);
                }
            }
        }

        Set<String> images = getImages(url, content);

        return new CrawlResult(internalLinks, externalLinks, images);
    }

    private Set<String> getImages(String url, String content) {
        Set<String> images = new HashSet<>();
        Matcher imageMatcher = IMG_PATTERN.matcher(content);
        while (imageMatcher.find()) {
            images.add(sanitizeUrl(url, imageMatcher.group(1)));
        }
        return images;
    }

    private String sanitizeUrl(String parentUrl, String link) {
        String sanitized = link.trim().replaceAll("\"|\'", "");
        sanitized = removeQueryStringOrAnchor(sanitized);
        sanitized = removeTrailingSlash(sanitized);
        if (isRelativeUrl(sanitized)) {
            sanitized = getAbsoluteUrl(parentUrl, sanitized);
        }
        return sanitized;
    }

    private String removeTrailingSlash(String sanitized) {
        return sanitized.endsWith("/") ? sanitized.substring(0, sanitized.length() - 1) : sanitized;
    }

    private String getAbsoluteUrl(String parentUrl, String url) {
        if (url.startsWith("/")) {
            url = baseUrl + url;
        } else if (parentUrl.equals(baseUrl)) {
            url = baseUrl + "/" + url;
        } else {
            String parentBase = parentUrl.substring(0, parentUrl.lastIndexOf('/') + 1);
            url = parentBase + url;
        }
        return url;
    }

    private String removeQueryStringOrAnchor(String url) {
        return url.replaceAll("(\\?|#).*", "");
    }

    private boolean isRelativeUrl(String link) {
        return !link.toLowerCase().startsWith("http://") && !link.toLowerCase().startsWith("https://");
    }

    private boolean isInternal(String link) {
        return isRelativeUrl(link) || link.indexOf(baseUrl) == 0;
    }
}

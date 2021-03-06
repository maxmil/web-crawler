package just.another.webcrawler;

import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * An immutable bean that holds the results of a page crawl
 */
public class CrawlResult {

    public static final CrawlResult EMPTY = new CrawlResult(emptySet(), emptySet(), emptySet());
    public static final CrawlResult PENDING = new CrawlResult(emptySet(), emptySet(), emptySet());

    private final Set<String> internalLinks;
    private final Set<String> externalLinks;
    private final Set<String> images;

    public CrawlResult(Set<String> internalLinks, Set<String> externalLinks, Set<String> images) {
        this.internalLinks = internalLinks;
        this.externalLinks = externalLinks;
        this.images = images;
    }

    public Set<String> getInternalLinks() {
        return internalLinks;
    }

    public Set<String> getExternalLinks() {
        return externalLinks;
    }

    public Set<String> getImages() {
        return images;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CrawlResult that = (CrawlResult) o;

        if (internalLinks != null ? !internalLinks.equals(that.internalLinks) : that.internalLinks != null) return false;
        if (externalLinks != null ? !externalLinks.equals(that.externalLinks) : that.externalLinks != null) return false;
        return images != null ? images.equals(that.images) : that.images == null;

    }

    @Override
    public int hashCode() {
        int result = internalLinks != null ? internalLinks.hashCode() : 0;
        result = 31 * result + (externalLinks != null ? externalLinks.hashCode() : 0);
        result = 31 * result + (images != null ? images.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CrawlResult{" +
                "internalLinks=" + internalLinks +
                ", externalLinks=" + externalLinks +
                ", images=" + images +
                '}';
    }
}

package just.another.webcrawler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;

public class Page {

    private final String name;
    private final Set<Page> internalLinks;
    private final Set<String> externalLinks;
    private final Set<String> images;

    public Page(String name, Set<Page> internalLinks, Set<String> externalLinks, Set<String> images) {
        this.name = name;
        this.internalLinks = internalLinks;
        this.externalLinks = externalLinks;
        this.images = images;
    }

    private Page(Builder builder) {
        name = builder.name;
        internalLinks = builder.internalLinks;
        externalLinks = builder.externalLinks;
        images = builder.images;
    }

    public static Builder newBuilder(String name) {
        return new Builder(name);
    }

    public static Builder newBuilder(Page copy) {
        Builder builder = new Builder(copy.name);
        builder.internalLinks = copy.internalLinks;
        builder.externalLinks = copy.externalLinks;
        builder.images = copy.images;
        return builder;
    }

    public String getName() {
        return name;
    }

    public Set<Page> getInternalLinks() {
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

        Page page = (Page) o;

        if (name != null ? !name.equals(page.name) : page.name != null) return false;
        if (internalLinks != null ? !internalLinks.equals(page.internalLinks) : page.internalLinks != null) return false;
        if (externalLinks != null ? !externalLinks.equals(page.externalLinks) : page.externalLinks != null) return false;
        return images != null ? images.equals(page.images) : page.images == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (internalLinks != null ? internalLinks.hashCode() : 0);
        result = 31 * result + (externalLinks != null ? externalLinks.hashCode() : 0);
        result = 31 * result + (images != null ? images.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Page{" +
                "name='" + name + '\'' +
                ", internalLinks=" + internalLinks +
                ", externalLinks=" + externalLinks +
                ", images=" + images +
                '}';
    }

    public static final class Builder {
        private String name;
        private Set<Page> internalLinks = emptySet();
        private Set<String> externalLinks = emptySet();
        private Set<String> images = emptySet();

        private Builder(String name) {
            this.name = name;
        }

        public Builder withInternalLinks(Page... val) {
            internalLinks = new HashSet<>(Arrays.asList(val));
            return this;
        }

        public Builder withExternalLinks(String... val) {
            externalLinks = new HashSet<>(Arrays.asList(val));
            return this;
        }

        public Builder withImages(String... val) {
            images = new HashSet<>(Arrays.asList(val));
            return this;
        }

        public Page build() {
            return new Page(this);
        }
    }
}

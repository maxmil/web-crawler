# Web Crawler

A **very** simple multithreaded crawler.


## Building and running

This project uses the gradle application plugin.

The default gradle tasks will create a distribution

```
./gradlew
```

Unpack the distribution

```
tar -xf build/distributions/web-crawler-1.0-SNAPSHOT.tar
```

Run the crawler

```
web-crawler-1.0-SNAPSHOT/bin/web-crawler <site> [threads]
```

Where

  * **site** is the absolute URL of the site to crawl
  * **threads** is the number of concurrent threads. Defaults to 1. Note that some sites may refuse to serve content if too many requests are made from the same ip.

The progress of the crawler will appear on STDOUT and the results will be written to `sitemap.txt`


## General architecture

The project consists of three key classes:

  * `WebCrawler`: The main point of entry. Handles concurrent crawling and the writing of the sitemap.
  * `PageCrawler`: One instance is allocated per thread. These are consumer threads that read the URL's to process from a queue, crawl the pages and offload the results.
  * `PageProcessor`: Manages the queue and merging the results from the crawlers.


## Some considerations
 

#### Concurrency

Given that crawling is an IO intensive operation in which threads spend most of their time parked I thought that a multithreaded solution would be much more efficient.

However given the DOS protection on many domains this is not so useful. A distributed solution would be better.


#### Dependencies

I've tried to keep external dependencies to a minimum including only test libraries (JUnit and Mockito) and a logging framework (that I could have probably removed but which was useful for debugging the concurrent parts).

However using some form of dependency injection framework and something for parsing HTML documents to cleanly extract links would definitely be worth adding if this project were to evolve.


#### Features

This is a **simple** crawler and has some limitations:

   * It simply removes circular references rather than trying to process them
   * It uses some pretty nasty regular expressions to match links and images that are not foolproof
   * It tries to index anything in an anchor tag (pdf's, mailto's....)
   * It can't handle relative links pointing to super directories (../../)
   * ... the list continues

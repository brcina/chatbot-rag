package com.chatbotrag.tools.service;

import jakarta.inject.Singleton;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class WebSearchTool {
    
    public static class WebPage {
        private String url;
        private String title;
        private String content;
        private List<String> links;
        
        public WebPage(String url, String title, String content, List<String> links) {
            this.url = url;
            this.title = title;
            this.content = content;
            this.links = links;
        }
        
        // Getters
        public String getUrl() { return url; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public List<String> getLinks() { return links; }
    }
    
    public Mono<WebPage> scrapeWebPage(String url) {
        return Mono.fromCallable(() -> {
            try {
                Document doc = Jsoup.connect(url)
                    .timeout(10000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();
                
                String title = doc.title();
                String content = doc.body().text();
                
                // Extract links
                Elements linkElements = doc.select("a[href]");
                List<String> links = new ArrayList<>();
                for (Element link : linkElements) {
                    String href = link.attr("abs:href");
                    if (!href.isEmpty()) {
                        links.add(href);
                    }
                }
                
                return new WebPage(url, title, content, links);
            } catch (IOException e) {
                throw new RuntimeException("Failed to scrape webpage: " + url, e);
            }
        });
    }
    
    public Mono<String> extractTextFromUrl(String url) {
        return scrapeWebPage(url)
            .map(WebPage::getContent);
    }
    
    public Mono<List<String>> extractLinksFromUrl(String url) {
        return scrapeWebPage(url)
            .map(WebPage::getLinks);
    }
}
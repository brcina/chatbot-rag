package com.chatbotrag.core.domain;

import java.util.List;

public class SearchResult {
    private String documentId;
    private String title;
    private String content;
    private double score;
    private List<String> highlights;

    public SearchResult() {}

    public SearchResult(String documentId, String title, String content, double score) {
        this.documentId = documentId;
        this.title = title;
        this.content = content;
        this.score = score;
    }

    // Getters and setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public List<String> getHighlights() { return highlights; }
    public void setHighlights(List<String> highlights) { this.highlights = highlights; }
}
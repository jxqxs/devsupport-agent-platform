package com.example.devsupport.model;

/** 一条知识的出处，不保存正文。 */
public class KnowledgeSource {
    public enum SourceType {
        OFFICIAL_DOC,
        GITHUB_ISSUE,
        STACKOVERFLOW
    }

    private String id;
    private SourceType sourceType;
    private String product;
    private String title;
    private String sourceUrl;
    private String license;

    public KnowledgeSource() {
    }

    public KnowledgeSource(String id, SourceType sourceType, String product, String title, String sourceUrl, String license) {
        this.id = id;
        this.sourceType = sourceType;
        this.product = product;
        this.title = title;
        this.sourceUrl = sourceUrl;
        this.license = license;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}

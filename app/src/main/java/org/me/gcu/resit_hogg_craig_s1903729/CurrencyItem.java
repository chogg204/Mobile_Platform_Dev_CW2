package org.me.gcu.resit_hogg_craig_s1903729;

public class CurrencyItem {
    // Define the properties
    private String title;
    private String link;
    private String guid;
    private String pubDate;
    private String description;
    private String category;
    private double exchangeRate;
    private String type;

    // Getter and setter methods for the properties
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Override the toString() method for debugging purposes
    @Override
    public String toString() {
        return "CurrencyItem [" +
                "title=" + title +
                ", link=" + link +
                ", guid=" + guid +
                ", pubDate=" + pubDate +
                ", description=" + description +
                ", category=" + category +
                "]";
    }
}

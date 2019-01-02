package com.example.icho.internetparking.cla;

public class mapListItem {
    private String title;
    private String info;
    private String price;
    private String available;

    public mapListItem(String title, String info, String price, String available) {
        this.title = title;
        this.info = info;
        this.price = price;
        this.available = available;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }
}

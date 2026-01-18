package com.shawn.andrioduimidterm;

public class Expense {
    private String title;
    private String category;
    private String time;
    private String amount;
    private String emoji;

    public Expense(String title, String category, String time, String amount, String emoji) {
        this.title = title;
        this.category = category;
        this.time = time;
        this.amount = amount;
        this.emoji = emoji;
    }

    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getTime() { return time; }
    public String getAmount() { return amount; }
    public String getEmoji() { return emoji; }
}

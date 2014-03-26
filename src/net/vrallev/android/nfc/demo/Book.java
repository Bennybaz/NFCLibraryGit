package net.vrallev.android.nfc.demo;

/**
 * Created by Lidor on 25/03/14.
 */
public class Book {
    private String name;
    private String author;

    public Book(String author, String name) {
        this.author = author;
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

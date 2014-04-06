package net.vrallev.android.nfc.demo;

import java.io.Serializable;

/**
 * Created by Lidor on 25/03/14.
 */
public class Book implements Serializable {
    private String name;
    private String author;
    private String bookID;
    private String location;
    private String shelf;
    private String barcode;
    private String year;
    private String publisher;

    public Book(String author, String name) {
        this.author = author;
        this.name = name;
    }

    public Book()
    {

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

    public String getBookID() {
        return bookID;
    }

    public void setBookID(String bookID) {
        this.bookID = bookID;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getShelf() {
        return shelf;
    }

    public void setShelf(String shelf) {
        this.shelf = shelf;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

}

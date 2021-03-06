package com.braude.nfc.library;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lidor on 25/03/14.
 */
public class Book implements Parcelable {
    private String name;
    private String author;
    private String bookID;
    private String location;
    private String shelf;
    private String barcode;
    private String year;
    private String publisher;
    private String status;
    private String isTagged;
    private double fixedPosition;

    public Book(String author, String name) {
        this.author = author;
        this.name = name;
    }

    public Book(){

    }

    public Book(Parcel source)
    {
        name = source.readString();
        author = source.readString();
        bookID = source.readString();
        location = source.readString();
        shelf = source.readString();
        barcode = source.readString();
        year = source.readString();
        publisher = source.readString();
        status = source.readString();
        fixedPosition = source.readDouble();
        isTagged = source.readString();
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getFixedPosition()
    {
        return fixedPosition;
    }

    public void setFixedPosition(double fixedPosition)
    {
        this.fixedPosition = fixedPosition;
    }

    public String getTagged() {
        return isTagged;
    }

    public void setTagged(String tagged) {
        this.isTagged = tagged;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(name);
        dest.writeString(author);
        dest.writeString(bookID);
        dest.writeString(location);
        dest.writeString(shelf);
        dest.writeString(barcode);
        dest.writeString(year);
        dest.writeString(publisher);
        dest.writeString(status);
        dest.writeDouble(fixedPosition);
        dest.writeString(isTagged);
    }

    public static final Creator CREATOR = new Creator() {
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}

package com.darshita.photosapp;

public class ImageInfo {
    public String path;
    public String name;
    public long size;
    public long date;

    public ImageInfo(String path, String name, long size, long date) {
        this.path = path;
        this.name = name;
        this.size = size;
        this.date = date;
    }
}

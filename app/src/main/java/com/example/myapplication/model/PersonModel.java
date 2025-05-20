package com.example.myapplication.model;

public class PersonModel {
    private String uid;
    private String name;
    private String city;
    private double lat;
    private double lon;

    public PersonModel() {} // Required by Firestore

    public PersonModel(String uid, String name, String city, double lat, double lon) {
        this.uid = uid;
        this.name = name;
        this.city = city;
        this.lat = lat;
        this.lon = lon;
    }
    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
}

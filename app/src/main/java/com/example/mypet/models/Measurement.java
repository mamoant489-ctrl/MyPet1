package com.example.mypet.models;

public class Measurement {

    private String id;
    private float value;
    private String date;

    public Measurement() {
    }

    public Measurement(String id, float value, String date) {
        this.id = id;
        this.value = value;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public float getValue() {
        return value;
    }

    public String getDate() {
        return date;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
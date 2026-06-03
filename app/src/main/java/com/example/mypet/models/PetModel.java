package com.example.mypet.models;

public class PetModel {

    private String id;
    private String name;
    private String age;
    private String photoUrl;

    public PetModel() {
    }

    public PetModel(String id,
                    String name,
                    String age,
                    String photoUrl) {

        this.id = id;
        this.name = name;
        this.age = age;
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
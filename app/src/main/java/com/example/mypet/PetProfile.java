package com.example.mypet;
public class PetProfile {
    public String name;
    public String age;
    public String breed;
    public String birthDate;
    public String gender;
    public String mark;
    public String weight;
    public String height;

    // Пустой конструктор ОБЯЗАТЕЛЕН для Firebase
    public PetProfile() { }

    public PetProfile(String name, String age, String breed, String birthDate,
                      String gender, String mark, String weight, String height) {
        this.name = name;
        this.age = age;
        this.breed = breed;
        this.birthDate = birthDate;
        this.gender = gender;
        this.mark = mark;
        this.weight = weight;
        this.height = height;
    }
}

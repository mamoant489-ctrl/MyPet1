package com.example.mypet;

public class Pet {
    public String name;
    public String age;
    public String breed;
    public String birthDate;
    public String sex;
    public String mark;
    public String weight;
    public String height;
    public Pet() {
    }
    public Pet(String name, String age, String breed, String birthDate,
               String sex, String mark, String weight, String height) {
        this.name = name;
        this.age = age;
        this.breed = breed;
        this.birthDate = birthDate;
        this.sex = sex;
        this.mark = mark;
        this.weight = weight;
        this.height = height;
    }
}

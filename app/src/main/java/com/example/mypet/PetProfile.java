package com.example.mypet;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class PetProfile {
    public String nickname, breed, gender, chip, birthDate, age, photoUrl;
    public List<Map<String, String>> customParams;

    public PetProfile() {}

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getChip() { return chip; }
    public void setChip(String chip) { this.chip = chip; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public List<Map<String, String>> getCustomParams() { return customParams; }
    public void setCustomParams(List<Map<String, String>> customParams) { this.customParams = customParams; }
}

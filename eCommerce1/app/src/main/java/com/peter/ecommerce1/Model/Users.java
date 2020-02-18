package com.peter.ecommerce1.Model;

public class Users
{
    private String fullName, phoneNumber,password,profileImage,email;

    public Users()
    {

    }

    public Users(String fullName, String phoneNumber, String password, String profileImage, String email) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.profileImage = profileImage;
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

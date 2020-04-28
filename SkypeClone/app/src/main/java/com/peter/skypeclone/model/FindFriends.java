package com.peter.skypeclone.model;

public class FindFriends
{
    String uID, profileImage, userName, status;

    public FindFriends()
    {
    }

    public FindFriends(String uID, String profileImage, String userName, String status) {
        this.uID = uID;
        this.profileImage = profileImage;
        this.userName = userName;
        this.status = status;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

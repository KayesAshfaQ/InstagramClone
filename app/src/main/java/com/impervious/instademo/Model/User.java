package com.impervious.instademo.Model;

public class User {

    private String uid;
    private String username;
    private String fullname;
    private String bio;
    private String imgUrl;

    public User(String uid, String username, String fullname, String bio, String imgUrl) {
        this.uid = uid;
        this.username = username;
        this.fullname = fullname;
        this.bio = bio;
        this.imgUrl = imgUrl;
    }

    public User() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}

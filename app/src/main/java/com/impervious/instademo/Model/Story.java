package com.impervious.instademo.Model;

public class Story {

    private String imageUrl;
    private long timeStart, timeEnd;
    private String userId, storyId;

    public Story(String imageUrl, long timeStart, long timeEnd, String userId, String storyId) {
        this.imageUrl = imageUrl;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.userId = userId;
        this.storyId = storyId;
    }

    public Story() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(long timeStart) {
        this.timeStart = timeStart;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }
}

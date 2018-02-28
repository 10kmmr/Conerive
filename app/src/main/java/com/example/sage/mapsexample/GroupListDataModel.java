package com.example.sage.mapsexample;

/**
 * Created by rkinabhi on 18-02-2018.
 */

public class GroupListDataModel {
    String groupName;
    String groupDisplayPictureURL;
    String lastTripDate;
    int memberCount;
    int tripCount;
    int imageCount;

    public GroupListDataModel(String groupName, String groupDisplayPictureURL, String lastTripDate, int memberCount, int tripCount, int imageCount) {
        this.groupName = groupName;
        this.groupDisplayPictureURL = groupDisplayPictureURL;
        this.lastTripDate = lastTripDate;
        this.memberCount = memberCount;
        this.tripCount = tripCount;
        this.imageCount = imageCount;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupDisplayPictureURL() {
        return groupDisplayPictureURL;
    }

    public String getLastTripDate() {
        return lastTripDate;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public int getTripCount() {
        return tripCount;
    }

    public int getImageCount() {
        return imageCount;
    }
}

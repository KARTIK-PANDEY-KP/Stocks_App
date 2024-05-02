package com.example.stock;

public class newsmodel {
    String heading, summary, datetime, imgurl, ambiguiity_in_assignment;

    public newsmodel(String heading, String summary, String datetime, String imgurl, String ambiguiity_in_assignment) {
        this.heading = heading;
        this.summary = summary;
        this.datetime = datetime;
        this.imgurl = imgurl;
        this.ambiguiity_in_assignment = ambiguiity_in_assignment;
    }

    public String getHeading() {
        return heading;
    }

    public String getSummary() {
        return summary;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getImgurl() {
        return imgurl;
    }

    public String getAmbiguiity_in_assignment() {
        return ambiguiity_in_assignment;
    }
}

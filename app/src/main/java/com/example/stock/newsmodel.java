package com.example.stock;

public class newsmodel {
    String heading, summary, datetime, imgurl;

    public newsmodel(String heading, String summary, String datetime, String imgurl) {
        this.heading = heading;
        this.summary = summary;
        this.datetime = datetime;
        this.imgurl = imgurl;
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
}

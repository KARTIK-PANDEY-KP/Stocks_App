package com.example.stock;

public class favouritesmodel {
    String company_name;
    String ticker;
    String result;
    String curr_price;
    int image;
    int red;

    public favouritesmodel(String company_name, String ticker, String result, String curr_price, int image, int red) {
        this.company_name = company_name;
        this.ticker = ticker;
        this.result = result;
        this.curr_price = curr_price;
        this.image = image;
        this.red = red;
    }

    public String getCompany_name() {
        return company_name;
    }

    public String getTicker() {
        return ticker;
    }

    public String getResult() {
        return result;
    }

    public String getCurr_price() {
        return curr_price;
    }

    public int getImage() {
        return image;
    }

    public int isRed() {
        return red;
    }
}

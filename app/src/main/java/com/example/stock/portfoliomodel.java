package com.example.stock;

public class portfoliomodel {
    String qty_owned;
    String ticker;
    String result;
    String market_value;
    int image;
    int red;

    public portfoliomodel(String qty_owned, String ticker, String result, String market_value, int image, int red) {
        this.qty_owned = qty_owned;
        this.ticker = ticker;
        this.result = result;
        this.market_value = market_value;
        this.image = image;
        this.red = red;
    }


    public String getQty_owned() {
        return qty_owned;
    }

    public String getTicker() {
        return ticker;
    }

    public String getResult() {
        return result;
    }

    public String getMarket_value() {
        return market_value;
    }

    public int getImage() {
        return image;
    }

    public int getRed() {
        return red;
    }
}

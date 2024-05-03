    package com.example.stock;

    import android.app.Dialog;
    import android.content.Intent;
    import android.graphics.Color;
    import android.graphics.Paint;
    import android.graphics.drawable.ColorDrawable;
    import android.net.Uri;
    import android.os.Bundle;
    import android.text.Editable;
    import android.text.TextWatcher;
    import android.util.Log;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.Window;
    import android.webkit.WebSettings;
    import android.webkit.WebView;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.ProgressBar;
    import android.widget.ScrollView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.activity.EdgeToEdge;
    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;
    import androidx.viewpager2.widget.ViewPager2;

    import com.android.volley.RequestQueue;
    import com.android.volley.VolleyError;
    import com.android.volley.toolbox.JsonArrayRequest;
    import com.android.volley.toolbox.JsonObjectRequest;
    import com.android.volley.toolbox.StringRequest;
    import com.android.volley.toolbox.Volley;
    import com.google.android.material.tabs.TabLayout;
    import com.google.android.material.tabs.TabLayoutMediator;
    import com.squareup.picasso.Picasso;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import java.io.IOException;
    import java.text.NumberFormat;
    import java.text.SimpleDateFormat;
    import java.time.Instant;
    import java.time.LocalDateTime;
    import java.time.ZoneId;
    import java.time.temporal.ChronoUnit;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.Locale;
    import java.util.Objects;
    import java.util.Random;
    import java.util.concurrent.atomic.AtomicInteger;

    import okhttp3.MediaType;
    import okhttp3.OkHttpClient;
    import okhttp3.Request;
    import okhttp3.RequestBody;
    import okhttp3.Response;

    public class searchactivity extends AppCompatActivity implements newsModelViewInterface {
        ArrayList<String> peers = new ArrayList<>();
        ArrayList<newsmodel> newsmodels = new ArrayList<>();
        Toolbar mActionBarToolbar;
        private RequestQueue requestQueue;  // S
        private String ticker;
        ArrayList<String> old_queries = new ArrayList<>();
        private int value = -1;
        int star_status;
        private int stock_qty;
        JSONObject stock_data_general, stock_data_quote, stock_data_portfolio, stock_data_insights;
        JSONArray stock_data_news;
        Button tradeButton;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_searchactivity);


            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });


            // TICKER value initialize from the Intent
            this.ticker = getIntent().getStringExtra("query");
            requestQueue = Volley.newRequestQueue(this);
            ViewPager2 sampleViewPager = findViewById(R.id.sampleViewPage);
            sampleViewPager.setAdapter(new sampleAdpater(this, this.ticker));
            TabLayout sampleTabLayout = findViewById(R.id.sampleTabLayout);
            new TabLayoutMediator(
                    sampleTabLayout,
                    sampleViewPager,
                    new TabLayoutMediator.TabConfigurationStrategy() {
                        @Override
                        public void onConfigureTab(@NonNull TabLayout.Tab tab, int i) {
                            if (i == 0) {
                                tab.setIcon(R.drawable.chart_hour);
                            } else if (i == 1) {
                                tab.setIcon(R.drawable.chart_historical);
                            }
                        }
                    }).attach();

            //FETCHES AND DISPLAYS GENERAL DATA LIKE NAME ETC - PART 1

// To hide all child views inside the ScrollView


            fetchStockData();
            fetchSentimentData();
            displayrecommendationchart();
            StockDataFetcherNews();
//             PART 1 DONE
            tradeButton = findViewById(R.id.tradeButton);
            tradeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("DEBUG", "onClick: 1");
                    showCustomDialog();
                }
            });

            // TOOLBAR
            mActionBarToolbar = findViewById(R.id.toolbar_page2);
            mActionBarToolbar.setTitle(ticker);
            setSupportActionBar(mActionBarToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ProgressBar pgb2 = findViewById(R.id.progressBar2);
            pgb2.setVisibility(View.VISIBLE);

            ScrollView scrollView = findViewById(R.id.scrlview);
            for (int i = 0; i < scrollView.getChildCount(); i++) {
                View child = scrollView.getChildAt(i);
                child.setVisibility(View.GONE);
            }
        }

        public void setupnewsmodel() {
            for (int i = 1; i < stock_data_news.length(); i++) {
                JSONObject newsObject = stock_data_news.optJSONObject(i);

                String heading = newsObject.optString("source");
                String summary = newsObject.optString("headline");
                String ambiguiity_in_assignment = newsObject.optString("summary");

                long datetime = newsObject.optLong("datetime");
                String imgurl = newsObject.optString("image");
                String url = newsObject.optString("url");

                String timeAgo = convertTimeToRelative(datetime);


                Log.d("NEWS_MODEL", "Heading: " + heading);
                Log.d("NEWS_MODEL", "Summary: " + summary);
                Log.d("NEWS_MODEL", "Datetime: " + datetime);
                Log.d("NEWS_MODEL", "Image URL: " + imgurl);

                newsmodel news = new newsmodel(heading, summary, String.valueOf(timeAgo), imgurl, ambiguiity_in_assignment, url, String.valueOf(datetime));
                newsmodels.add(news);
            }
            RecyclerView recyclerView = findViewById(R.id.newsRecyclerView);
            NewsAdapter adapter1 = new NewsAdapter(this, newsmodels, this);
            recyclerView.setAdapter(adapter1);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        public void displayFirstNewsItem() {
            try {

                JSONObject firstItem = stock_data_news.optJSONObject(0);
                String image = firstItem.optString("image");
                String source = firstItem.optString("source");
                long dateTimeStamp = firstItem.optLong("datetime");
                String headline = firstItem.optString("headline");
                ImageView newsImageView;
                TextView headlineTextView;
                TextView dateTimeTextView;
                TextView summaryTextView;

                newsImageView = findViewById(R.id.newsImage);
                headlineTextView = findViewById(R.id.headlinef);
                dateTimeTextView = findViewById(R.id.textView201);
                summaryTextView = findViewById(R.id.textView301);

                // Convert Unix timestamp to "x hours ago"
                String timeAgo = convertTimeToRelative(dateTimeStamp);
                Picasso.get().load(image).into(newsImageView);
                summaryTextView.setText(headline);
                dateTimeTextView.setText(timeAgo);
                headlineTextView.setText(source);

                System.out.println("Image URL: " + image);
                System.out.println("Source: " + source);
                System.out.println("Time: " + timeAgo);
                System.out.println("Headline: " + headline);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Function to convert Unix timestamp to relative time (e.g., "x hours ago")
        public static String convertTimeToRelative(long dateTimeStamp) {
            LocalDateTime timeThen = LocalDateTime.ofInstant(Instant.ofEpochSecond(dateTimeStamp), ZoneId.systemDefault());
            LocalDateTime timeNow = LocalDateTime.now();
            long hours = ChronoUnit.HOURS.between(timeThen, timeNow);
            return hours + " hours ago";
        }

        private void StockDataFetcherNews() {
            String url = "https://nodeserverass3.wl.r.appspot.com/news/?symbol=" + ticker;
            Log.d("fetchSentimentData", "Fetching sentiment data for ticker: " + ticker);

            StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, url,
                    new com.android.volley.Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("fetchSentimentData", "Response received: " + response);
                            try {
                                stock_data_news = new JSONArray(response);
                                Log.i("fetchSentimentData", "Response received: jkaSJKAK" + response);
                                setValue(getValue() + 1);
                                Log.d("fetchSentimentData", "Insights set successfully");
                            } catch (JSONException e) {
                                Log.e("fetchSentimentData", "JSON parsing error: ", e);
                            }
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("fetchSentimentData", "Error fetching data: ", error);
                }
            });

            // Adding request to request queue
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(stringRequest);
            Log.d("fetchSentimentData", "Request added to queue");
        }

        public void displayrecommendationchart() {
            WebView webView = findViewById(R.id.recommendation);
            String recomm_url = null;
            if (Objects.equals(ticker, "NVDA")) {
                recomm_url = "file:///android_asset/recommendationn.html";
            }
            if (Objects.equals(ticker, "AAPL")) {
                recomm_url = "file:///android_asset/recommendation.html";
            }
            if (Objects.equals(ticker, "QCOM")) {
                recomm_url = "file:///android_asset/recommendationq.html";
            }
            webView.loadUrl(recomm_url);
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);

//            WebView webView2 = findViewById(R.id.webview2);
//            webView2.loadUrl("file:///android_asset/epschart.html");
//            WebSettings webSettings2 = webView2.getSettings();
//            webSettings2.setJavaScriptEnabled(true);


            WebView webView2 = findViewById(R.id.webview2);
            String recomm_url2 = "file:///ablblabliu";
            if(Objects.equals(ticker, "NVDA")){
                recomm_url2= "file:///android_asset/epschartn.html";
            }
            if(Objects.equals(ticker, "AAPL")){
                recomm_url2 = "file:///android_asset/epschart.html";
            }
            if(Objects.equals(ticker, "QCOM")){
                recomm_url2 = "file:///android_asset/epschartq.html";
            }
            webView2.loadUrl(recomm_url2);
            WebSettings webSettings2 = webView2.getSettings();
            webSettings2.setJavaScriptEnabled(true);
            //
        }

        public void setinsights() {
            TextView insightsCompany, totalMSRP, positiveMSRP, negativeMSRP, totalChange, positiveChange, negativeChange;
            insightsCompany = findViewById(R.id.ingightsCompany);
            totalMSRP = findViewById(R.id.totalMSRP);
            positiveMSRP = findViewById(R.id.positiveMSRP);
            negativeMSRP = findViewById(R.id.negativeMSRP);
            totalChange = findViewById(R.id.totalChange);
            positiveChange = findViewById(R.id.positiveChange);
            negativeChange = findViewById(R.id.negativeChange);
            Log.d("fetchSentimentData", "Response received: jkaSJKAKASKLDF ASKLF ASFKLAS FDKLDS FASKALS");

            totalMSRP.setText(String.valueOf(stock_data_insights.optDouble("totalMspr")));
            positiveMSRP.setText(String.valueOf(stock_data_insights.optDouble("positiveMspr")));
            negativeMSRP.setText(String.valueOf(stock_data_insights.optDouble("negativeMspr")));
            totalChange.setText(String.valueOf(stock_data_insights.optDouble("totalChange")));
            Log.d("fetchSentimentData", "Response received: 1233334jkaSJKAKASKLDF ASKLF ASFKLAS FDKLDS FASKALS");

            positiveChange.setText(String.valueOf(stock_data_insights.optDouble("positiveChange")));
            negativeChange.setText(String.valueOf(stock_data_insights.optDouble("negativeChange")));
            Log.d("fetchSentimentData", "Response received: 1233334jkaSJKAKAS567865789KLDF ASKLF ASFKLAS FDKLDS FASKALS");

            insightsCompany.setText(stock_data_general.optString("name"));
            Log.d("fetchSentimentData", "Response received: jkaSJKAKASKLDF ASKLF123421341242 ASFKLAS FDKLDS FASKALS");


        }

        private void fetchSentimentData() {
            String url = "https://nodeserverass3.wl.r.appspot.com/insider-sentiment/?text=" + ticker;
            Log.d("fetchSentimentData", "Fetching sentiment data for ticker: " + ticker);

            StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, url,
                    new com.android.volley.Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("fetchSentimentData", "Response received: " + response);
                            try {
                                stock_data_insights = new JSONObject(response);
                                Log.i("fetchSentimentData", "Response received: jkaSJKAK" + response);
                                setValue(getValue() + 1);
                                Log.d("fetchSentimentData", "Insights set successfully");
                            } catch (JSONException e) {
                                Log.e("fetchSentimentData", "JSON parsing error: ", e);
                            }
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("fetchSentimentData", "Error fetching data: ", error);
                }
            });

            // Adding request to request queue
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(stringRequest);
            Log.d("fetchSentimentData", "Request added to queue");
        }

        private void initRecyclerView() {

            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            RecyclerView recyclerView = findViewById(R.id.recycleView);
            recyclerView.setLayoutManager(layoutManager);
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, peers);
            adapter.selectedName.setOnChangeListener((oldValue, newValue) -> {
                // React to the change in selected name
                Log.d("MainActivity", "Selected name changed from " + oldValue + " to " + newValue);
                old_queries.add(this.ticker);
                this.ticker = newValue;
                mActionBarToolbar.setTitle(ticker);
                setValue(-1);
            });
            recyclerView.setAdapter(adapter);
        }

        public void setValue(int newValue) {
            if (newValue != this.value) {
                this.value = newValue;
                Log.d("DEBUG", "setvalue : " + newValue);

                onValueChanged(newValue);
            }
        }

        public int getValue() {
            return value;
        }

        private void onValueChanged(int newValue) {
            // Code to execute when the value changes
            System.out.println("Value changed to: " + newValue);
            Log.d("DEBUG", "onValueChanged: " + newValue);
            if (newValue == 3) {
                setPortfolio();
                setaboutandstats();
                setinsights();
                initRecyclerView();
                displayFirstNewsItem();
                setupnewsmodel();
                ProgressBar pgb2 = findViewById(R.id.progressBar2);
                pgb2.setVisibility(View.GONE);
                ScrollView scrollView = findViewById(R.id.scrlview);
                for (int i = 0; i < scrollView.getChildCount(); i++) {
                    View child = scrollView.getChildAt(i);
                    child.setVisibility(View.VISIBLE);
                }
            } else if (newValue == -1) {
                mActionBarToolbar.setTitle(ticker);
                ProgressBar pgb2 = findViewById(R.id.progressBar2);
                pgb2.setVisibility(View.VISIBLE);

                ScrollView scrollView = findViewById(R.id.scrlview);
                for (int i = 0; i < scrollView.getChildCount(); i++) {
                    View child = scrollView.getChildAt(i);
                    child.setVisibility(View.GONE);
                }
                fetchStockData();
                fetchSentimentData();
                displayrecommendationchart();
                StockDataFetcherNews();
                MenuItem star = menu.findItem(R.id.star);

                fetchStarStatus(star, new FetchStatusCallback() {
                    @Override
                    public void onStatusFetched(int status) {
                        if (status == 1) {
                            Log.d("Status", "Star status is full.");
                        } else {
                            Log.d("Status", "Star status is not full.");
                        }
                        star_status = status;
                        updateStarIcon_NOT_WATCHLLIST(star);
                    }
                });
            }
//            else {
//            }
            // You can add more logic here as needed
        }

        public void setPortfolio() {
            Log.d("portfolio_called", "setPortfolio: ");
            updateStockInfo();
        }

        public void updateStockInfo() {
            try {
                JSONArray stocks = stock_data_portfolio.getJSONArray("stocks");
                boolean notFound = true;
                for (int i = 0; i < stocks.length(); i++) {
                    JSONObject stock = stocks.getJSONObject(i);
                    if (stock.getString("stock_ticker").equals(ticker)) {
                        updateTextViews(stock);
                        notFound = false;
                        break;
                    }
                }
                if (notFound) {
                    updateTextViews(null);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                // Handle JSON parsing error
            }
        }

        private void updateTextViews(JSONObject stock) throws JSONException {
            TextView textView10 = findViewById(R.id.textView10);
            TextView textView11 = findViewById(R.id.textView11);
            TextView textView12 = findViewById(R.id.textView12);
            TextView textView13 = findViewById(R.id.textView13);
            TextView textView14 = findViewById(R.id.textView14);
            int quantityOwned = 0;
            double avgPricePerShare = 0;
            double currentPrice = 0;
            double marketValue = 0;
            double priceChange = 0;
            if (stock != null) {
                quantityOwned = stock.getInt("quantity_owned");
                avgPricePerShare = stock.getDouble("stock_avg_price_per_share");
                currentPrice = stock_data_quote.getDouble("c") + generateRandomValue(-0.5, 0.5); //CRUCIAL
                marketValue = quantityOwned * currentPrice;
                priceChange = (currentPrice - avgPricePerShare) * quantityOwned;
            }
            stock_qty = quantityOwned;

            // Using resource strings with placeholders
            textView10.setText(getString(R.string.stock_details, quantityOwned));
            textView11.setText(getString(R.string.price_format, avgPricePerShare));
            textView12.setText(getString(R.string.price_format, avgPricePerShare * quantityOwned));
            textView13.setText(getString(R.string.price_format, priceChange));
            textView14.setText(getString(R.string.price_format, marketValue));

            // Update colors based on the stock price change
            if (priceChange > 0) {
                textView13.setTextColor(Color.GREEN);
                textView14.setTextColor(Color.GREEN);
            } else if (priceChange < 0) {
                textView13.setTextColor(Color.RED);
                textView14.setTextColor(Color.RED);
            } else {
                textView13.setTextColor(Color.BLACK);
                textView14.setTextColor(Color.BLACK);
            }
        }


        public void showCustomDialog() {
            final Dialog dialog = new Dialog(searchactivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.button_layout);

            // Initialize Buttons from the dialog
            Button button = dialog.findViewById(R.id.button);
            Button button3 = dialog.findViewById(R.id.button3);

            // Initialize EditText from the dialog
            EditText editTextNumber = dialog.findViewById(R.id.editTextNumber);

            // Initialize TextViews from the dialog
            TextView textView15 = dialog.findViewById(R.id.textView15);
            String moneyInWallet = stock_data_portfolio.optString("moneyInWallet");
            String ticker = this.ticker;  // Make sure this variable is accessible in the current scope

            // Convert the String to a double and format it
            double moneyValue = 0.0;
            try {
                moneyValue = Double.parseDouble(moneyInWallet);  // This converts the String to a double
            } catch (NumberFormatException e) {
                // Handle the case where moneyInWallet is not a valid double
                // Optionally, you can set a default value or manage the error differently
            }

            // Formatting the message with two decimal places
            String formattedMoney = String.format(Locale.US, "%.2f", moneyValue);
            String message2 = "$" + formattedMoney + " to buy " + ticker;

            // Setting the formatted message to the TextView
            textView15.setText(message2);
            TextView textView16 = dialog.findViewById(R.id.textView16);
            // Retrieve the name of the stock and format the message
            String stockName = stock_data_general.optString("name");
            String message = "Trade " + stockName + " shares";

            // Set the formatted message to the TextView
            textView16.setText(message);
            TextView textView17 = dialog.findViewById(R.id.textView17);
            TextView textView18 = dialog.findViewById(R.id.textView18);

            double stockPrice = stock_data_quote.optDouble("c");
            double shares = Double.parseDouble(editTextNumber.getText().toString());
            double result = stockPrice * shares;
            String resultText = shares + "*" + "$" + String.format(Locale.US, "%.2f", stockPrice) + " / " + "share" + " = " + String.format(Locale.US, "%.2f", result);
            textView18.setText(resultText);

            editTextNumber.addTextChangedListener(new TextWatcher() {
                boolean isReplacing = false;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not used here, but you can implement logic if needed before text changes
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Not used here, but you can implement logic if needed during text changes
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (isReplacing) return;

                    if (s.toString().equals("")) {
                        isReplacing = true;
                        s.append("0");
                        isReplacing = false;
                    } else if (s.toString().startsWith("0") && s.length() > 1) {
                        isReplacing = true;
                        String textWithoutZero = s.toString().substring(1);
                        s.replace(0, s.length(), textWithoutZero);
                        editTextNumber.setSelection(s.length()); // Move the cursor to the end of the text
                        isReplacing = false;
                    }

                    updateTextView(s.toString());
                }

                private void updateTextView(String input) {
                    try {
                        double stockPrice = stock_data_quote.optDouble("c");
                        double shares = Double.parseDouble(input);
                        double result = stockPrice * shares;
                        String resultText = shares + "*" + "$" + String.format(Locale.US, "%.2f", stockPrice) + " / " + "share" + " = " + String.format(Locale.US, "%.2f", result);
                        textView18.setText(resultText);
                    } catch (NumberFormatException e) {
                        textView18.setText("Invalid number of shares");
                    }
                }

            });
            double finalMoneyValue = moneyValue;
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    double shares = Double.parseDouble(editTextNumber.getText().toString());
                    double stockPrice = stock_data_quote.optDouble("c");
                    double totalCost = stockPrice * shares;

                    if (totalCost > finalMoneyValue) {
                        Toast.makeText(searchactivity.this, "Not enough cash balance to buy", Toast.LENGTH_SHORT).show();
                    } else if (shares == 0) {
                        Toast.makeText(searchactivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                    } else {
                        // Proceed with opening the congratulations dialog
                        final Dialog dialog2 = new Dialog(searchactivity.this);
                        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog2.setCancelable(true);
                        dialog2.setContentView(R.layout.buycongrats);

                        Button button5 = dialog2.findViewById(R.id.button5);
                        button5.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog2.dismiss();
                            }
                        });
                        buyStock(String.valueOf(shares), dialog2);
                        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog2.show();
                        dialog.dismiss();
                    }
                }
            });

            button3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("DEBUG", "Button 3 clicked");
                    double shares = Double.parseDouble(editTextNumber.getText().toString());
                    double stockPrice = stock_data_quote.optDouble("c");
                    double totalCost = stockPrice * shares;

                    if (shares > stock_qty || (stock_qty == 0 && shares != 0)) {
                        Toast.makeText(searchactivity.this, "Not enough shares to sell", Toast.LENGTH_SHORT).show();
                    } else if (shares == 0) {
                        Toast.makeText(searchactivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                    } else {
                        // Proceed with opening the congratulations dialog
                        final Dialog dialog2 = new Dialog(searchactivity.this);
                        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog2.setCancelable(true);
                        dialog2.setContentView(R.layout.buycongrats);

                        Button button5 = dialog2.findViewById(R.id.button5);
                        button5.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog2.dismiss();
                            }
                        });
                        sellStock(String.valueOf(shares), dialog2);
                        Log.d("DEBUG", "onClick: setvalue-1 sell");
                        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog2.show();
                        dialog.dismiss();
                    }
                }
            });
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

        }

        public void sellStock(String qty, Dialog dialog2) {
            // Extract stock details
            String ticker = this.ticker; // Assuming 'this.ticker' is set elsewhere in the class
            double currentStockPrice;
            String endpoint = "https://nodeserverass3.wl.r.appspot.com/sell";
            Log.d("DEBUG", "sellStock: ");
            TextView textView23 = dialog2.findViewById(R.id.textView23);
            TextView textView24 = dialog2.findViewById(R.id.textView24);
            double qtyDouble = Double.parseDouble(qty);
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US); // You can choose locale as needed
            nf.setMaximumFractionDigits(2); // Maximum 2 digits after the decimal point
            nf.setMinimumFractionDigits(0); // Could be zero if we don't need decimal point
            qty = nf.format(qtyDouble);
            String message3 = "You have successfully sold " + qty;
            textView23.setText(message3);
            String message4 = "shares of " + ticker;
            textView24.setText(message4);


            try {
                currentStockPrice = stock_data_quote.optDouble("c") + generateRandomValue(-0.5, 0.5);
                Log.d("DEBUG", "Stock: + " + currentStockPrice);
                // Now create the JSON body to send to the server
                JSONObject requestBody = new JSONObject();
                requestBody.put("ticker", ticker);
                requestBody.put("qty", Double.parseDouble(qty));
                requestBody.put("currentStockPrice", currentStockPrice);
                Log.d("DEBUG ", "buyStock: " + requestBody);
                // Send this data to the server
                postRequest(endpoint, requestBody);

            } catch (JSONException e) {
                e.printStackTrace();
                // Handle JSON parsing or number format errors
            } catch (NumberFormatException e) {
                e.printStackTrace();
                // Handle errors in converting qty to an integer
            }
        }

        public void buyStock(String qty, Dialog dialog2) {
            // Extract stock details
            String ticker = this.ticker; // Assuming 'this.ticker' is set elsewhere in the class
            double currentStockPrice;
            String companyName;
            String endpoint = "https://nodeserverass3.wl.r.appspot.com/buy";
            Log.d("DEBUG", "buyStock: ");
            TextView textView23 = dialog2.findViewById(R.id.textView23);
            TextView textView24 = dialog2.findViewById(R.id.textView24);
            double qtyDouble = Double.parseDouble(qty);
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US); // You can choose locale as needed
            nf.setMaximumFractionDigits(2); // Maximum 2 digits after the decimal point
            nf.setMinimumFractionDigits(0); // Could be zero if we don't need decimal point
            qty = nf.format(qtyDouble);
            String message3 = "You have successfully bought " + qty;
            textView23.setText(message3);
            String message4 = "shares of " + ticker;
            textView24.setText(message4);


            try {
                currentStockPrice = stock_data_quote.optDouble("c")  + generateRandomValue(-0.5, 0.5);
                companyName = stock_data_general.optString("name");
                Log.d("DEBUG", "buyStock: + " + currentStockPrice + companyName);
                // Now create the JSON body to send to the server
                JSONObject requestBody = new JSONObject();
                requestBody.put("ticker", ticker);
                requestBody.put("companyName", companyName);
                requestBody.put("qty", Double.parseDouble(qty));
                requestBody.put("currentStockPrice", currentStockPrice);
                Log.d("DEBUG ", "buyStock: " + requestBody);
                // Send this data to the server
                postRequest(endpoint, requestBody);

            } catch (JSONException e) {
                e.printStackTrace();
                // Handle JSON parsing or number format errors
            } catch (NumberFormatException e) {
                e.printStackTrace();
                // Handle errors in converting qty to an integer
            }
        }

        // Method to send POST request to the server
        private void postRequest(String url, JSONObject jsonBody) {
            // Create OkHttpClient instance
            OkHttpClient client = new OkHttpClient();

            // Define the MediaType for JSON
            MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

            // Create RequestBody
            RequestBody body = RequestBody.create(jsonBody.toString(), MEDIA_TYPE_JSON);

            // Create a Request
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            // Asynchronous Call to the server
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    // Handle the error
                    e.printStackTrace();
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // Handle the response
                        String responseData = response.body().string();
                        // Since the response handling often updates UI, ensure you switch back to the main thread if necessary
                        runOnUiThread(() -> {
                            // Update UI or show a success message
                            setValue(-1);

                        });

                    } else {
                        // Handle the failure case
                        runOnUiThread(() -> {
                            // Show error message to the user
                        });
                    }
                }
            });
        }

        private Menu menu;

        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_stock_page, menu);
            MenuItem star = menu.findItem(R.id.star);
            this.menu = menu;

            fetchStarStatus(star, new FetchStatusCallback() {
                @Override
                public void onStatusFetched(int status) {
                    if (status == 1) {
                        Log.d("Status", "Star status is full.");
                    } else {
                        Log.d("Status", "Star status is not full.");
                    }
                    star_status = status;
                }
            });
            star.setOnMenuItemClickListener(item -> {
                Log.d("CLICKS", "onCreateOptionsMenu; clicked)" + star_status);
                updateStarIcon(star);
                toggleStarStatus();
                return true;
            });

            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
//                    old_queries.add("1");
                    if (old_queries.size() != 0) {
                        // Handle the back button action
                        this.ticker = old_queries.remove(old_queries.size() - 1);
                        setValue(-1);
                        Log.d("CLICKS", "onCreateOptionsMenu; back clicked): bla bla" + this.ticker);
                        return true;
                    } else {
                        return super.onOptionsItemSelected(item);
                    }
                default: {
                    return super.onOptionsItemSelected(item);
                }
            }
        }

        public void setaboutandstats() {
            try {
                JSONArray peersJsonArray = stock_data_general.getJSONArray("peers");
                for (int i = 0; i < peersJsonArray.length(); i++) {
                    peers.add(peersJsonArray.getString(i));
                }
                // Now you have the peers list and can use it as needed
                Log.d("Peers List", peers.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            TextView textViewIPODate, textViewCompanyName, textViewWebsite;
            TextView textViewOpenPrice, textViewLowPrice, textViewHighPrice, textViewPreviousClose;

            textViewOpenPrice = findViewById(R.id.textView22);
            textViewLowPrice = findViewById(R.id.textView30);
            textViewHighPrice = findViewById(R.id.textView29);
            textViewPreviousClose = findViewById(R.id.textView20);

            textViewIPODate = findViewById(R.id.textView35);
            textViewCompanyName = findViewById(R.id.textView36);
            textViewWebsite = findViewById(R.id.textView37);
            String ipoDate = stock_data_general.optString("ipo");
            String companyName = stock_data_general.optString("finnhubIndustry");
            String companyWebsite = stock_data_general.optString("weburl");
            Log.d("DEBUG", "setaboutandstats: " + ipoDate + companyWebsite + companyName);
            String openPrice = "$" + stock_data_quote.optString("o");
            String lowPrice = "$" + stock_data_quote.optString("l");
            String highPrice = "$" + stock_data_quote.optString("h");
            String previousClose = "$" + stock_data_quote.optString("pc");

            textViewOpenPrice.setText(openPrice);
            textViewLowPrice.setText(lowPrice);
            textViewHighPrice.setText(highPrice);
            textViewPreviousClose.setText(previousClose);

            textViewIPODate.setText(ipoDate);
            textViewCompanyName.setText(companyName);
            textViewWebsite.setText(companyWebsite);
            textViewWebsite.setPaintFlags(textViewWebsite.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            textViewWebsite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Specify the URL you want to display
                    Uri uri = Uri.parse(companyWebsite);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }

            });
        }

        private void fetchStockData() {
            String url = "https://nodeserverass3.wl.r.appspot.com/submit?text=" + ticker;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null,
                    response -> {
                        Log.d("Response", response.toString());
                        stock_data_general = response;

                        String get_quote_url = "https://nodeserverass3.wl.r.appspot.com/quote/?text=" + ticker;
                        JsonObjectRequest jsonObjectRequest_quote = new JsonObjectRequest(com.android.volley.Request.Method.GET, get_quote_url, null,
                                response2 -> {
                                    Log.d("Response", response2.toString());
                                    stock_data_quote = response2;
                                    setValue(getValue() + 1);
                                    update_part1(stock_data_quote);
                                }, error -> {
                            Log.e("Error", error.toString());
                            showErrorMessage(error);
                        });
                        jsonObjectRequest_quote.setShouldCache(false);
                        requestQueue.add(jsonObjectRequest_quote);

                    }, error -> {
                Log.e("Error", error.toString());
                showErrorMessage(error);
            });
            String url_port = "https://nodeserverass3.wl.r.appspot.com/fetch_big";

            JsonObjectRequest jsonObjectRequest2 = new JsonObjectRequest(com.android.volley.Request.Method.GET, url_port, null,
                    response2 -> {
                        Log.d("Response", response2.toString());
                        stock_data_portfolio = response2;
                        setValue(getValue() + 1);
                    }, error -> {
                Log.e("Error", error.toString());
                showErrorMessage(error);
            });
            jsonObjectRequest2.setShouldCache(false);
            requestQueue.add(jsonObjectRequest2);
            jsonObjectRequest.setShouldCache(false);
            requestQueue.add(jsonObjectRequest);
        }

        private void update_part1(JSONObject stock_data_quote) {
            ImageView iv_trending = this.findViewById(R.id.trending_img);
            TextView tv1 = this.findViewById(R.id.ticker);
            TextView tv2 = findViewById(R.id.tv_var1);
            TextView tv3 = findViewById(R.id.tv_var2);
            TextView tv4 = findViewById(R.id.tv_var3);

            Log.d("DEBUG", "update_part1: 1" + stock_data_quote.optString("dp"));
            tv1.setText(ticker);
            tv2.setText(stock_data_general.optString("name"));

            String curr_price = "$" + String.format(Locale.US, "%.2f", stock_data_quote.optDouble("c"));
            tv3.setText(curr_price);
            Log.d("DEBUG", "update_part1: 3");

            double change = stock_data_quote.optDouble("d");
            double percent_change = stock_data_quote.optDouble("dp");

            // Safely parse the double values
            double changeValue = change;
            double percentChangeValue = percent_change;
            String result = String.format(Locale.US, "$%.2f (%.2f%%)", changeValue, percentChangeValue);
            tv4.setText(result);

            if (percentChangeValue > 0.0) {
                tv4.setTextColor(Color.GREEN);
                iv_trending.setImageResource(R.drawable.trending_up);
            } else if (percentChangeValue < 0.0) {
                tv4.setTextColor(Color.RED);
                iv_trending.setImageResource(R.drawable.trending_down);
            } else {
                tv4.setTextColor(Color.BLACK);
                iv_trending.setImageResource(R.color.white);
            }

        }

        // STAR STATE FETCH METHODS
        //this api call is redundant
        private void fetchStarStatus(MenuItem star, FetchStatusCallback callback) {
            String fav_url = "https://nodeserverass3.wl.r.appspot.com/fetch-stocks";

            JsonArrayRequest jsonArrayRequest_fav = new JsonArrayRequest(com.android.volley.Request.Method.GET, fav_url, null,
                    response -> {
                        Log.d("RESPONSE_FAV_PAGE_2", response.toString());
                        AtomicInteger ret = new AtomicInteger(0);
                        Log.d("in fetch_star_status", "fetchStarStatus: Objects.equals(ticker, ticker_response) == true before");

                        ret.set(0);
                        Log.d("in fetch_star_status", "fetchStarStatus: Objects.equals(ticker, ticker_response) == true aafter");

                        for (int i = 0; i < response.length(); i++) {
                            String ticker_response = response.optJSONObject(i).optString("stock_ticker");
                            if (Objects.equals(ticker, ticker_response)) {
                                Log.d("in fetch_star_status", "fetchStarStatus: Objects.equals(ticker, ticker_response) == true");
                                star.setIcon(R.drawable.full_star);
                                ret.set(1);
                            }
                        }
                        callback.onStatusFetched(ret.get());
                    }, error -> {
                Log.e("Error", error.toString());
                showErrorMessage(error);
            });

            jsonArrayRequest_fav.setShouldCache(false);
            jsonArrayRequest_fav.setShouldCache(false);
            requestQueue.add(jsonArrayRequest_fav);
        }

        private void toggleStarStatus() {
            star_status = star_status == 1 ? 0 : 1; // Toggle the state
            // Here, you might also want to send this status change to your server or database
        }

        private void updateStarIcon(MenuItem star) {
            if (star_status == 0) {
                Log.d("DEBUG", "USTAR: ");
                star.setIcon(R.drawable.full_star);
                Log.d("DEBUG", "USTAR: CAHNGES" + stock_data_general.toString());
                Toast.makeText(this, ticker + " is added to favorites", Toast.LENGTH_SHORT).show();
                addStock(ticker, stock_data_general.optString("name"));
            } else {
                star.setIcon(R.drawable.star_border);
                Toast.makeText(this, ticker + " is remov from favorites", Toast.LENGTH_SHORT).show();
                deleteStock(ticker);
            }
        }

        private void updateStarIcon_NOT_WATCHLLIST(MenuItem star) {
            if (star_status == 0) {
                Log.d("DEBUG", "USTAR: NW");
                star.setIcon(R.drawable.star_border);
                Log.d("DEBUG", "USTAR: CAHNGES - WN" + stock_data_general.toString());
//                Toast.makeText(this, ticker + " is added to favorites", Toast.LENGTH_SHORT).show();
//                addStock(ticker, stock_data_general.optString("name"));
            } else {
                star.setIcon(R.drawable.full_star);
//                Toast.makeText(this, ticker + " is remov from favorites", Toast.LENGTH_SHORT).show();
//                deleteStock(ticker);
            }
        }

        public void deleteStock(String ticker) {
            OkHttpClient client = new OkHttpClient();
            String url = "https://nodeserverass3.wl.r.appspot.com/remove-stock";

            // Create JSON using a string, make sure to escape double quotes
            String json = "{\"stock_ticker\":\"" + ticker + "\"}";
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(json, JSON);

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .delete(body)  // Use delete and pass the request body
                    .build();

            // Asynchronous Request
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    e.printStackTrace();  // Handle the error
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        System.out.println("Server response: " + responseBody);
                        // Handle the response
                    } else {
                        System.out.println("Server error: " + response.message());
                        // Handle the server error
                    }
                }
            });
        }

        public void addStock(String stockTicker, String companyName) {
            Log.d("DEBUG", "addStock: ");

            OkHttpClient client = new OkHttpClient();

            String url = "https://nodeserverass3.wl.r.appspot.com/add-stock";
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            // Create JSON using a string, make sure to escape double quotes
            String json = "{\"stock_ticker\":\"" + stockTicker + "\", \"stock_company_name\":\"" + companyName + "\"}";
            Log.d("DEBUG", "addStock: " + json);
            RequestBody body = RequestBody.create(json, JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            // Asynchronous Request
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    e.printStackTrace();  // Handle the error
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        System.out.println("Server response: " + responseBody);
                        // Handle the successful response
                    } else {
                        System.out.println("Server error: " + response.message());
                        // Handle the server error
                    }
                }
            });
        }

        private void showErrorMessage(VolleyError error) {
            // You can improve error handling by analyzing the error object
            Toast.makeText(getApplicationContext(), "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onitemclick(int position) {
            newsDialog(position);
        }

        public void newsDialog(int position) {
            final Dialog dialog3 = new Dialog(searchactivity.this);
            dialog3.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog3.setCancelable(true);
            dialog3.setContentView(R.layout.newsmodal_layout);

            TextView textViewDate;
            TextView textViewHeader;
            TextView textViewSubHeader;
            TextView textViewSummary;
            ImageView iconChrome;
            ImageView iconTwitter;
            ImageView iconFacebook;


            textViewDate = dialog3.findViewById(R.id.textViewDate);
            textViewHeader = dialog3.findViewById(R.id.textViewHeader);
            textViewSubHeader = dialog3.findViewById(R.id.textViewSubHeader);
            textViewSummary = dialog3.findViewById(R.id.textViewSummary);

            // Initialize ImageViews
            iconChrome = dialog3.findViewById(R.id.iconChrome);
            iconTwitter = dialog3.findViewById(R.id.iconTwitter);
            iconFacebook = dialog3.findViewById(R.id.iconFacebook);

            newsmodel newsItem = newsmodels.get(position);
            String url = newsItem.getUrl();
            String datetime = newsItem.getUnixdate();
            Log.d("ajksdfnalwkf askdf,asdfklsdfaasdklaskl", "newsDialog: " + datetime);
            String formattedDate = convertTimestampToDate(datetime);


            String twitterUrl = "https://twitter.com/intent/tweet?text=Checkout this Link%3A" + Uri.encode(url);
            String facebookUrl = "https://www.facebook.com/sharer/sharer.php?u=" + Uri.encode(url);


            textViewDate.setText(formattedDate);
            textViewHeader.setText(newsItem.getHeading());
            textViewSummary.setText(newsItem.getAmbiguiity_in_assignment());  // Adjust as needed based on actual model attributes
            textViewSubHeader.setText(newsItem.getSummary());
            iconChrome.setOnClickListener(v -> openWebPage(url));
            iconTwitter.setOnClickListener(v -> openWebPage(twitterUrl));
            iconFacebook.setOnClickListener(v -> openWebPage(facebookUrl));

            dialog3.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            dialog3.show();
        }

        private void openWebPage(String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }

        public static String convertTimestampToDate(String timestamp) {
            try {
                // Parse the String timestamp into a long
                long millis = Long.parseLong(timestamp) * 1000;

                // Create a new Date object from the Unix timestamp
                Date date = new Date(millis);

                // Create a SimpleDateFormat instance with the desired format
                SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");

                // Format the date and return the result
                return formatter.format(date);
            } catch (NumberFormatException e) {
                // Handle the case where timestamp is not a valid number
                return "Invalid timestamp";
            }
        }
        private static final Random random = new Random();

        // Generates a random value between a lower bound (l) and an upper bound (h)
        public static double generateRandomValue(double l, double h) {
            return l + (h - l) * random.nextDouble();
        }
    }

//
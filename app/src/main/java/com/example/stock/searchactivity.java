    package com.example.stock;

    import android.app.Dialog;
    import android.graphics.Color;
    import android.graphics.drawable.ColorDrawable;
    import android.os.Bundle;
    import android.text.Editable;
    import android.text.TextWatcher;
    import android.util.Log;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.Window;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.activity.EdgeToEdge;
    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;
    import androidx.viewpager2.widget.ViewPager2;

    import com.android.volley.RequestQueue;
    import com.android.volley.VolleyError;
    import com.android.volley.toolbox.JsonArrayRequest;
    import com.android.volley.toolbox.JsonObjectRequest;
    import com.android.volley.toolbox.Volley;
    import com.google.android.material.tabs.TabLayout;
    import com.google.android.material.tabs.TabLayoutMediator;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import java.io.IOException;
    import java.text.NumberFormat;
    import java.util.Locale;
    import java.util.Objects;
    import java.util.concurrent.atomic.AtomicInteger;

    import okhttp3.MediaType;
    import okhttp3.OkHttpClient;
    import okhttp3.Request;
    import okhttp3.RequestBody;
    import okhttp3.Response;

    public class searchactivity extends AppCompatActivity {
        private RequestQueue requestQueue;  // Declare the RequestQueue.
        private String ticker;
        private int value = -1;
        int star_status;
        private int stock_qty;
        JSONObject stock_data_general, stock_data_quote, stock_data_portfolio;
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
                            if(i == 0) {
                                tab.setIcon(R.drawable.chart_hour);
                            }
                            else if(i == 1){
                                tab.setIcon(R.drawable.chart_historical);
                            }
                        }
                    }).attach();

            //FETCHES AND DISPLAYS GENERAL DATA LIKE NAME ETC - PART 1
            fetchStockData();
            // PART 1 DONE
            tradeButton = findViewById(R.id.tradeButton);
            tradeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("DEBUG", "onClick: 1");
                    showCustomDialog();
                }
            });

            // TOOLBAR
            Toolbar mActionBarToolbar = findViewById(R.id.toolbar_page2);
            mActionBarToolbar.setTitle(ticker);
            setSupportActionBar(mActionBarToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
            if(newValue == 1) {
                setPortfolio();
            }
            else if(newValue == -1) {
                fetchStockData();
            }
//            else {
//            }
            // You can add more logic here as needed
        }
        public void setPortfolio(){
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
                if(notFound){
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
            if(stock != null)
            {
                quantityOwned = stock.getInt("quantity_owned");
                avgPricePerShare = stock.getDouble("stock_avg_price_per_share");
                currentPrice = stock_data_quote.getDouble("c");
                marketValue = quantityOwned * currentPrice;
                priceChange = (currentPrice - avgPricePerShare)*quantityOwned;
            }
            stock_qty = quantityOwned;

            // Using resource strings with placeholders
            textView10.setText(getString(R.string.stock_details, quantityOwned));
            textView11.setText(getString(R.string.price_format, avgPricePerShare));
            textView12.setText(getString(R.string.price_format, avgPricePerShare*quantityOwned));
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

                        if (shares >= stock_qty || (stock_qty == 0 && shares != 0)){
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
                currentStockPrice = stock_data_quote.optDouble("c");
                Log.d("DEBUG", "buyStock: + " + currentStockPrice);
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
                currentStockPrice = stock_data_quote.optDouble("c");
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
                        });
                        setValue(-1);

                    } else {
                        // Handle the failure case
                        runOnUiThread(() -> {
                            // Show error message to the user
                        });
                    }
                }
            });
        }
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_stock_page, menu);
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
                        stock_data_portfolio =  response2;
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

            jsonArrayRequest_fav.setShouldCache(false);jsonArrayRequest_fav.setShouldCache(false);
            requestQueue.add(jsonArrayRequest_fav);
        }
        private void toggleStarStatus() {
            star_status = star_status == 1 ? 0: 1; // Toggle the state
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
    }


package com.example.stock;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private RequestQueue requestQueue;  // Declare the RequestQueue.
    private ProgressBar progressBar; // Declare the ProgressBar.
    ArrayList<portfoliomodel> portfoliomodels = new ArrayList<>();
    ArrayList<favouritesmodel> favouritesmodels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
//        RecyclerView recyclerView = findViewById(R.id.rvportfolio);
        // Initialize the RequestQueue
        requestQueue = Volley.newRequestQueue(this);
        adapter_home adapter = new adapter_home(this,favouritesmodels);
        adapter_home_port adapter2 = new adapter_home_port(this,portfoliomodels);


        // Fetch data from server
        fetchPortfolioData(adapter, adapter2);

//        recyclerView.setAdapter(adapter);
//
//        recyclerView.setLayoutManager( new LinearLayoutManager(this));


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView linkTextView = findViewById(R.id.finnhublink);
        linkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Specify the URL you want to display
                Uri uri = Uri.parse("https://www.finnhub.io/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        Toolbar mActionBarToolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(mActionBarToolbar); // Correct usage
        mActionBarToolbar.setTitle("My title");

        String date_n = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        TextView date  = findViewById(R.id.curdate);
        date.setText(date_n);
    }

    private void fetchPortfolioData(adapter_home adapter,adapter_home_port adapter2) {
        String url = "https://nodeserverass3.wl.r.appspot.com/fetch_big";
        String fav_url = "https://nodeserverass3.wl.r.appspot.com/fetch-stocks";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("Response", response.toString());
                    updateUI(response, adapter2);
                }, error -> {
            Log.e("Error", error.toString());
            progressBar.setVisibility(View.GONE); // Ensure the ProgressBar is hidden on error.
            showErrorMessage(error);
        });

        JsonArrayRequest jsonArrayRequest_fav = new JsonArrayRequest(Request.Method.GET, fav_url, null,
                response -> {
                    Log.d("Response_fav", response.toString());
                    updateUI_fav(response, adapter);
                }, error -> {
            Log.e("Error", error.toString());

            progressBar.setVisibility(View.GONE); // Ensure the ProgressBar is hidden on error.
            showErrorMessage(error);
        });

        jsonObjectRequest.setShouldCache(false);
        jsonArrayRequest_fav.setShouldCache(false); // Consider enabling caching based on your use case.
        requestQueue.add(jsonObjectRequest);
        requestQueue.add(jsonArrayRequest_fav);
    }

    private void updateUI_fav(JSONArray response, adapter_home adapter) {
        String get_quote_url = "https://nodeserverass3.wl.r.appspot.com/quote/?text=";
        for (int i = 0; i < response.length(); i++) {
            String ticker = response.optJSONObject(i).optString("stock_ticker");
            String company_name = response.optJSONObject(i).optString("stock_company_name");

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, get_quote_url + ticker, null,
                    response_quote -> {
                        Log.d("Response_quote_fav", response_quote.toString());
                        String curr_price = "$" + response_quote.optString("c");
                        String change = response_quote.optString("d");
                        String percent_change = response_quote.optString("dp");
                        String result = String.format(Locale.US, "$%.2f (%.2f%%)", Double.parseDouble(change), Double.parseDouble(percent_change));
                        favouritesmodels.add(new favouritesmodel(company_name, ticker, result, curr_price, R.drawable.trending_up));
                        RecyclerView recyclerView = findViewById(R.id.rvfavourite);

                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager( new LinearLayoutManager(this));

                    }, error -> {
                Log.e("Error", error.toString());
                progressBar.setVisibility(View.GONE); // Ensure the ProgressBar is hidden on error.
                showErrorMessage(error);
            });

            jsonObjectRequest.setShouldCache(false);
            requestQueue.add(jsonObjectRequest);
        }
    }

    private void updateUI(JSONObject response, adapter_home_port adapter2) {
        TextView networth = findViewById(R.id.networth);
        TextView cashbalance = findViewById(R.id.cashbalance);
        try {
            parseAndDisplayPortfolio(response, networth, adapter2);
            parseAndDisplayCashBalance(response, cashbalance);
        } catch (JSONException e) {
            throw new RuntimeException("JSON Parsing Error", e);
        }
    }

    private void parseAndDisplayPortfolio(JSONObject response, TextView networth, adapter_home_port adapter2) throws JSONException {
        double moneyInWallet = response.optDouble("moneyInWallet", 0);
        JSONArray stocks = response.optJSONArray("stocks");
        double netWorth = calculateNetWorth(stocks, moneyInWallet, adapter2);
        networth.setText(String.format(Locale.US, "%.2f", netWorth));
    }

    private double calculateNetWorth(JSONArray stocks, double initialNetWorth, adapter_home_port adapter2) throws JSONException {
        double netWorth = initialNetWorth;
        if (stocks != null && stocks.length() > 0) {
            for (int i = 0; i < stocks.length(); i++) {
                JSONObject stock = stocks.getJSONObject(i);
                double quantityOwned = stock.optDouble("quantity_owned");
                double currentPrice = stock.optDouble("currentPrice");
                double avgPricePerShare = stock.optDouble("stock_avg_price_per_share");

                double value = quantityOwned * currentPrice;
                netWorth += value;

                double valueChange = (currentPrice - avgPricePerShare) * quantityOwned;
                double percentChange = (valueChange / (avgPricePerShare * quantityOwned)) * 100;

                String result = String.format(Locale.US, "$%.2f (%.2f%%)", valueChange, percentChange);
                portfoliomodels.add(new portfoliomodel(String.valueOf(quantityOwned) + " shares", stock.optString("stock_ticker"), "$" + result, "$" + String.valueOf(value), R.drawable.trending_down));
                RecyclerView recyclerView = findViewById(R.id.rvportfolio);

                recyclerView.setAdapter(adapter2);
                recyclerView.setLayoutManager( new LinearLayoutManager(this){
                    @Override
                    public boolean canScrollVertically(){
                        return false;
                    }
                });
            }
        }
        return netWorth;
    }

    private void parseAndDisplayCashBalance(JSONObject response, TextView cashbalance) {
        double moneyInWallet = response.optDouble("moneyInWallet", 0);
        cashbalance.setText(String.format(Locale.US, "%.2f", moneyInWallet));
    }

    private void showErrorMessage(VolleyError error) {
        // You can improve error handling by analyzing the error object
        Toast.makeText(getApplicationContext(), "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show();
    }

    //FOR HANDLING THE PROGRESS BAR
//    public void disableAllExceptToolbar() {
//        ViewGroup mainLayout = findViewById(R.id.main);
//        for (int i = 0; i < mainLayout.getChildCount(); i++) {
//            View child = mainLayout.getChildAt(i);
//            if (child.getId() != R.id.toolbar2) { // Skip the toolbar
//                child.setEnabled(false);
//            }
//        }
//    }
//
//    public void enableAllExceptToolbar() {
//        ViewGroup mainLayout = findViewById(R.id.main);
//        for (int i = 0; i < mainLayout.getChildCount(); i++) {
//            View child = mainLayout.getChildAt(i);
//            if (child.getId() != R.id.toolbar2) { // Skip the toolbar
//                child.setEnabled(true);
//            }
//        }
//    }

}


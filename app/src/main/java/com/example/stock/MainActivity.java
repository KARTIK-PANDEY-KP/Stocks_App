package com.example.stock;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
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
    private Handler handler = new Handler();
    private Runnable runnable;
    private int value = -1;

    private RequestQueue requestQueue;  // Declare the RequestQueue.
    private ProgressBar progressBar; // Declare the ProgressBar.
    ArrayList<portfoliomodel> portfoliomodels = new ArrayList<>();
    ArrayList<favouritesmodel> favouritesmodels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Initialize the RequestQueue
        requestQueue = Volley.newRequestQueue(this);
        adapter_home adapter = new adapter_home(this,favouritesmodels);
        adapter_home_port adapter2 = new adapter_home_port(this,portfoliomodels);

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
        mActionBarToolbar.setTitle("Stocks");
        setSupportActionBar(mActionBarToolbar); // Correct usage
        Toolbar mActionBarToolbar_spinner = findViewById(R.id.toolbar);
        mActionBarToolbar_spinner.setTitle("Stocks");

        String date_n = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        TextView date  = findViewById(R.id.curdate);
        date.setText(date_n);

        setVisibilityForAllViews(View.GONE);

        fetchPortfolioData(adapter, adapter2);
//        value.setValue(2);
    }
    public void setValue(int newValue) {
        if (newValue != this.value) {
            this.value = newValue;
            onValueChanged(newValue);
        }
    }

    public int getValue() {
        return value;
    }

    private void onValueChanged(int newValue) {
        // Code to execute when the value changes
        System.out.println("Value changed to: " + newValue);
        if(newValue == 2) {
            setVisibilityForAllViews(View.VISIBLE);
        }
        else if(newValue == -1) {
            setVisibilityForAllViews(View.GONE);
        }
        else {
        }
        // You can add more logic here as needed
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu); // Inflate the menu with the search item
        MenuItem searchItem = menu.findItem(R.id.action_search);

//        SearchView searchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchItem.getActionView();

// Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        setupSearchView(searchView);


        return true;
    }

    private void setupSearchView(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform final search
                Log.d("SUBMIT", query);
                Intent callsearchactivity = new Intent(MainActivity.this, searchactivity.class);
                callsearchactivity.putExtra("query", query);
                startActivity(callsearchactivity);
                searchView.clearFocus(); // Add this line
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                handler.removeCallbacks(runnable);
                runnable = () -> {
                    if (!newText.isEmpty()) {
                        flushAndFetchSearchSuggestions(newText, searchView);
                    }
                    else{
                        ExampleCursorAdapter adapter = (ExampleCursorAdapter) searchView.getSuggestionsAdapter();
                        if (adapter != null) {
                            adapter.flushData();  // Flush data if text is empty
                        }
                    }
                };
                handler.postDelayed(runnable, 0); // Delay the execution by 300 milliseconds
                return true;
//                if (!newText.isEmpty()) {
//                    fetchSearchSuggestions(newText, searchView);
//                }
//                return true;
            }
        });
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }
            @Override
            public boolean onSuggestionClick(int position) {
                // Get the selected suggestion's text or data
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                int indexColumnSuggestion = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
                String suggestion = cursor.getString(indexColumnSuggestion);

                // Create an Intent to start the searchactivity with the selected suggestion
                Intent intent = new Intent(MainActivity.this, searchactivity.class);
                Log.d("SUBMIT_SUGGESTION", suggestion.split("\\|")[0].trim()); // Outputs: NVDA from NVDA | NVIDIA CORP
                intent.putExtra("query", suggestion.split("\\|")[0].trim()); // Outputs: NVDA from NVDA | NVIDIA CORP
                startActivity(intent);

                // Clear focus and close the search view
                searchView.clearFocus();
                return true;
            }
        });
    }

    private void flushAndFetchSearchSuggestions(String text, SearchView searchView) {
        // Close and clear any existing suggestions before fetching new ones
        ExampleCursorAdapter adapter = (ExampleCursorAdapter) searchView.getSuggestionsAdapter();
        if (adapter != null) {
            adapter.flushData();
        }

        fetchSearchSuggestions(text, searchView);
    }
    private void fetchSearchSuggestions(String text, SearchView searchView) {
        String suggestionUrl = "https://nodeserverass3.wl.r.appspot.com/search?q=" + Uri.encode(text);
        MatrixCursor cursor = new MatrixCursor(new String[] { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1 });
        searchView.setSuggestionsAdapter(new ExampleCursorAdapter(this, cursor));

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, suggestionUrl, null,
                response -> {
                    Log.d("Suggestions", response.toString());
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            String suggestion = response.getString(i);
                            cursor.addRow(new Object[]{i, suggestion});
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    searchView.setSuggestionsAdapter(new ExampleCursorAdapter(this, cursor));
//                    updateSuggestions(response, searchView);
                },
                error -> Log.e("Error", error.toString())
        );

        // Assuming you have a RequestQueue initialized somewhere in your activity
        requestQueue.add(jsonArrayRequest);
    }
//    private void updateSuggestions(JSONArray suggestions, SearchView searchView) {
//        MatrixCursor cursor = new MatrixCursor(new String[] { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1 });
//        try {
//            for (int i = 0; i < suggestions.length(); i++) {
//                String suggestion = suggestions.getString(i);
//                cursor.addRow(new Object[]{i, suggestion});
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        searchView.setSuggestionsAdapter(new ExampleCursorAdapter(this, cursor));
//    }


    private void fetchPortfolioData(adapter_home adapter,adapter_home_port adapter2) {
        String url = "https://nodeserverass3.wl.r.appspot.com/fetch_big";
        String fav_url = "https://nodeserverass3.wl.r.appspot.com/fetch-stocks";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("Response", response.toString());
                    updateUI_port(response, adapter2);
                    setValue(getValue() + 1);
                }, error -> {
            Log.e("Error", error.toString());
            showErrorMessage(error);
        });

        JsonArrayRequest jsonArrayRequest_fav = new JsonArrayRequest(Request.Method.GET, fav_url, null,
                response -> {
                    Log.d("Response_fav", response.toString());
                    updateUI_fav(response, adapter);
                    setValue(getValue() + 1);

                }, error -> {
            Log.e("Error", error.toString());


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
                        String curr_price = "$" + String.format(Locale.US, "%.2f", response_quote.optDouble("c"));
                        String change = response_quote.optString("d");
                        String percent_change = response_quote.optString("dp");
                        String result = String.format(Locale.US, "$%.2f (%.2f%%)", Double.parseDouble(change), Double.parseDouble(percent_change));
                        // Trending up or down sign logic
                        if(Double.parseDouble(percent_change)>0) {
                            favouritesmodels.add(new favouritesmodel(company_name, ticker, result, curr_price, R.drawable.trending_up, 2));
                        }
                        else if(Double.parseDouble(percent_change)<0) {
                            favouritesmodels.add(new favouritesmodel(company_name, ticker, result, curr_price, R.drawable.trending_down, 1));
                        }
                        else {
                            favouritesmodels.add(new favouritesmodel(company_name, ticker, result, curr_price, R.color.white, 0));
                        }
                        // ------------------------------

                        RecyclerView recyclerView = findViewById(R.id.rvfavourite);

                        ItemTouchHelper.Callback callback = new swipeAndDeleteHelper(adapter, this);
                        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
                        adapter.setTouchHelper(itemTouchHelper);
                        itemTouchHelper.attachToRecyclerView(recyclerView);

                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager( new LinearLayoutManager(this));
                        setValue(getValue() + 1);
                    }, error -> {
                Log.e("Error", error.toString());

                showErrorMessage(error);
            });

            jsonObjectRequest.setShouldCache(false);
            requestQueue.add(jsonObjectRequest);
        }
    }

    private void updateUI_port(JSONObject response, adapter_home_port adapter2) {
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
        networth.setText(String.format(Locale.US, "$%.2f", netWorth));
    }

    private double calculateNetWorth(JSONArray stocks, double initialNetWorth, adapter_home_port adapter2) throws JSONException {
        double netWorth = initialNetWorth;
        if (stocks != null && stocks.length() > 0) {
            for (int i = 0; i < stocks.length(); i++) {
                JSONObject stock = stocks.getJSONObject(i);
                int quantityOwned = stock.optInt("quantity_owned");
                double currentPrice = stock.optDouble("currentPrice");
                double avgPricePerShare = stock.optDouble("stock_avg_price_per_share");

                double value = quantityOwned * currentPrice;
                netWorth += value;

                double valueChange = (currentPrice - avgPricePerShare) * quantityOwned;
                double percentChange = (valueChange / (avgPricePerShare * quantityOwned)) * 100;

                String result = String.format(Locale.US, "%.2f (%.2f%%)", valueChange, percentChange);

                // Trending up or down sign logic
                if(percentChange>0) {
                    portfoliomodels.add(new portfoliomodel(String.valueOf(quantityOwned) + " shares", stock.optString("stock_ticker"), "$" + result, "$" + String.format(Locale.US, "%.2f", value), R.drawable.trending_up, 2));
                }
                else if(percentChange<0) {
                    portfoliomodels.add(new portfoliomodel(String.valueOf(quantityOwned) + " shares", stock.optString("stock_ticker"), "$" + result, "$" + String.format(Locale.US, "%.2f", value), R.drawable.trending_down, 1));
                }
                else {
                    portfoliomodels.add(new portfoliomodel(String.valueOf(quantityOwned) + " shares", stock.optString("stock_ticker"), "$" + result, "$" + String.format(Locale.US, "%.2f", value), R.color.white, 0));
                }
                // ------------------------------
                RecyclerView recyclerView = findViewById(R.id.rvportfolio);

                ItemTouchHelper.Callback callback = new swipeAndDeleteHelper(adapter2, this);
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
                adapter2.setTouchHelper(itemTouchHelper);
                itemTouchHelper.attachToRecyclerView(recyclerView);

                recyclerView.setAdapter(adapter2);
                recyclerView.setLayoutManager( new LinearLayoutManager(this)
//                { //TODO: SCROLLER
//                    @Override
//                    public boolean canScrollVertically(){
//                        return false;
//                    }
//                }
                );
            }
        }
        return netWorth;
    }

    private void parseAndDisplayCashBalance(JSONObject response, TextView cashbalance) {
        double moneyInWallet = response.optDouble("moneyInWallet", 0);
        cashbalance.setText(String.format(Locale.US, "$%.2f", moneyInWallet));
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
    private void setVisibilityForAllViews(int visibility) {
        findViewById(R.id.divider).setVisibility(visibility);
        findViewById(R.id.curdate).setVisibility(visibility);
        findViewById(R.id.Portfolio_title).setVisibility(visibility);
        findViewById(R.id.textView3).setVisibility(visibility);
        findViewById(R.id.textView4).setVisibility(visibility);
        findViewById(R.id.networth).setVisibility(visibility);
        findViewById(R.id.cashbalance).setVisibility(visibility);
        findViewById(R.id.textView5).setVisibility(visibility);
        findViewById(R.id.finnhublink).setVisibility(visibility);
        findViewById(R.id.rvportfolio).setVisibility(visibility);
        findViewById(R.id.rvfavourite).setVisibility(visibility);
        findViewById(R.id.toolbar2).setVisibility(visibility);
        int oppositeVisibility = (visibility == View.VISIBLE) ? View.GONE : View.VISIBLE;
        findViewById(R.id.toolbar2).setVisibility(oppositeVisibility);
        findViewById(R.id.divider3).setVisibility(oppositeVisibility);

        final View imageview = findViewById(R.id.imageView);
        final View progressbar = findViewById(R.id.progressBar);

        if(oppositeVisibility == View.GONE){
            progressbar.setVisibility(oppositeVisibility);
            imageview.setVisibility(oppositeVisibility);
        }
        else {
            progressbar.setVisibility(visibility);
            imageview.setVisibility(oppositeVisibility);

            // Handler to add delay for the divider visibility change
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Set visibility of divider after 200ms delay
                    imageview.setVisibility(visibility);
                    progressbar.setVisibility(oppositeVisibility);
                }
            },  800); // Delay in milliseconds
        }
    }
}


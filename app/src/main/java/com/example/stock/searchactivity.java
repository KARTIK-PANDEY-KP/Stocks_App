    package com.example.stock;

    import android.os.Bundle;
    import android.util.Log;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.widget.Toast;

    import androidx.activity.EdgeToEdge;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;

    import com.android.volley.RequestQueue;
    import com.android.volley.VolleyError;
    import com.android.volley.toolbox.JsonArrayRequest;
    import com.android.volley.toolbox.JsonObjectRequest;
    import com.android.volley.toolbox.Volley;

    import org.json.JSONObject;

    import java.io.IOException;
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
        int star_status;
        JSONObject stock_data_general;


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

            //FETCHES GENERAL DATA LIKE NAME ETC
            fetchPortfolioData();


                // TOOLBAR
            Toolbar mActionBarToolbar = findViewById(R.id.toolbar_page2);
            mActionBarToolbar.setTitle(ticker);
            setSupportActionBar(mActionBarToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        private void fetchPortfolioData() {
            String url = "https://nodeserverass3.wl.r.appspot.com/submit?text=" + ticker;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null,
                    response -> {
                        Log.d("Response", response.toString());
                        stock_data_general = response;
                    }, error -> {
                Log.e("Error", error.toString());
                showErrorMessage(error);
            });



            jsonObjectRequest.setShouldCache(false);
            requestQueue.add(jsonObjectRequest);
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

            jsonArrayRequest_fav.setShouldCache(false);
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

                addStock(ticker, stock_data_general.optString("name"));
            } else {
                star.setIcon(R.drawable.star_border);
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


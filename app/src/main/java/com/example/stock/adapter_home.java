package com.example.stock;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

//import com.android.volley.Request;
//import com.android.volley.Response;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;

public class adapter_home extends RecyclerView.Adapter<adapter_home.MyViewHolder> implements swipeAndDeleteAdapterHelper {
    Context context;
    ArrayList<favouritesmodel> favouritesmodels;
    private static ItemTouchHelper touchelper;

    public adapter_home(Context context, ArrayList<favouritesmodel> favouritesmodels) {
        this.context = context;
        this.favouritesmodels = favouritesmodels;
    }

    @NonNull
    @Override
    public adapter_home.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.rvfav_row, parent, false);
        return new adapter_home.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull adapter_home.MyViewHolder holder, int position) {
        holder.tv1.setText(favouritesmodels.get(position).getTicker());
        holder.tv2.setText(favouritesmodels.get(position).getCompany_name());
        holder.tv3.setText(favouritesmodels.get(position).getCurr_price());
        holder.tv4.setText(favouritesmodels.get(position).getResult());
        if(favouritesmodels.get(position).isRed() == 1) {
            holder.tv4.setTextColor(Color.RED);
        } else if (favouritesmodels.get(position).isRed() == 2) {
            holder.tv4.setTextColor(Color.GREEN);
        }
        else{
            holder.tv4.setTextColor(Color.BLACK);
        }
        holder.iv_trending.setImageResource(favouritesmodels.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return favouritesmodels.size()  ;
    }

    @Override
    public void onItemMove(int from, int to) {
        favouritesmodel temp = favouritesmodels.get(from);
        favouritesmodels.remove(from);
        favouritesmodels.add(to, temp);
        notifyItemMoved(from, to);
    }

    @Override
    public void onItemSwiped(int position) {
        favouritesmodel item = favouritesmodels.get(position);
        Log.d("DELETE", "Removing ticker: " + item.getTicker());  // Print the ticker
        favouritesmodels.remove(position);
        deleteStock(item.getTicker());  // Delete the stock on the server

        notifyItemRemoved(position);
    }

    public void deleteStock(String ticker) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://nodeserverass3.wl.r.appspot.com/remove-stock";

        // Create JSON using a string, make sure to escape double quotes
        String json = "{\"stock_ticker\":\"" + ticker + "\"}";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
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



    public void setTouchHelper(ItemTouchHelper sdHelper){
        this.touchelper = sdHelper;
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, GestureDetector.OnGestureListener {

        ImageView iv_trending;
        TextView tv1, tv2, tv3, tv4;
        GestureDetector mGestureDetector;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_trending = itemView.findViewById(R.id.trending_img);
            tv1 = itemView.findViewById(R.id.ticker_port);
            tv2 = itemView.findViewById(R.id.tv_var1_port);
            tv3 = itemView.findViewById(R.id.tv_var2_port);
            tv4 = itemView.findViewById(R.id.tv_var3_port);

            mGestureDetector = new GestureDetector(itemView.getContext(), this);
            itemView.setOnTouchListener(this);
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(@NonNull MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            return true; // TODO: back to false if error comes
        }

        @Override
        public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            touchelper.startDrag(this); //TODO: ERROR WARNING
        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            return true; // TODO: back to false if error comes
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mGestureDetector.onTouchEvent(event);
            return true;
        }
    }
}

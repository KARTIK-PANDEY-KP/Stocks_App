package com.example.stock;

import android.content.Context;
import android.graphics.Color;
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

import java.util.ArrayList;

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
        favouritesmodels.remove(position);
        notifyItemRemoved(position);
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

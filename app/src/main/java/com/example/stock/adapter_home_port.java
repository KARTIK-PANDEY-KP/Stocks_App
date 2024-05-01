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

import java.util.ArrayList;

public class adapter_home_port extends RecyclerView.Adapter<adapter_home_port.MyViewHolder> implements swipeAndDeleteAdapterHelper {
    private final adapterinterface adapterinterface;

    Context context;
    ArrayList<portfoliomodel> portfoliomodels;
    private static ItemTouchHelper touchelper;

    public adapter_home_port(Context context, ArrayList<portfoliomodel> portfoliomodels, adapterinterface adapterinterface) {
        this.context = context;
        this.portfoliomodels = portfoliomodels;
        this.adapterinterface = adapterinterface;
    }

    @NonNull
    @Override
    public adapter_home_port.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.rvport_row, parent, false);
        return new adapter_home_port.MyViewHolder(view, adapterinterface);
    }

    @Override
    public void onBindViewHolder(@NonNull adapter_home_port.MyViewHolder holder, int position) {
        holder.tv1.setText(portfoliomodels.get(position).getTicker());
        holder.tv2.setText(portfoliomodels.get(position).getQty_owned());
        holder.tv3.setText(portfoliomodels.get(position).getMarket_value());
        holder.tv4.setText(portfoliomodels.get(position).getResult());
        if(portfoliomodels.get(position).getRed() == 1) {
            holder.tv4.setTextColor(Color.RED);
        } else if (portfoliomodels.get(position).getRed() == 2) {
            holder.tv4.setTextColor(Color.GREEN);
        }
        else{
            holder.tv4.setTextColor(Color.BLACK);
        }
        holder.iv_trending.setImageResource(portfoliomodels.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return portfoliomodels.size()  ;
    }

    @Override
    public void onItemMove(int from, int to) {
        portfoliomodel temp = portfoliomodels.get(from);
        portfoliomodels.remove(from);
        portfoliomodels.add(to, temp);
        notifyItemMoved(from, to);
    }

    @Override
    public void onItemSwiped(int position) {
        portfoliomodels.remove(position);
        notifyItemRemoved(position);
    }
    public void setTouchHelper(ItemTouchHelper sdHelper){
        this.touchelper = sdHelper;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, GestureDetector.OnGestureListener {
        ImageView iv_trending;
        TextView tv1, tv2, tv3, tv4;
        GestureDetector mGestureDetector;

        public MyViewHolder(@NonNull View itemView, adapterinterface adapterinterface) {
            super(itemView);

            iv_trending = itemView.findViewById(R.id.trending_img);
            tv1 = itemView.findViewById(R.id.ticker);
            tv2 = itemView.findViewById(R.id.tv_var1);
            tv3 = itemView.findViewById(R.id.tv_var2);
            tv4 = itemView.findViewById(R.id.tv_var3);

            mGestureDetector = new GestureDetector(itemView.getContext(), this);

            itemView.setOnTouchListener(this);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(adapterinterface != null){
                        int pos = getBindingAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION){
                            adapterinterface.itemclickclick(pos);
                        }
                    }

                }
            });

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
            return true;// TODO: back to false if error comes
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
            Log.d("TEST", "onTouch: " + event);
            mGestureDetector.onTouchEvent(event);
            return false;
        }
    }
}

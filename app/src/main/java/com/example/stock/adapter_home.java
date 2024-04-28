package com.example.stock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class adapter_home extends RecyclerView.Adapter<adapter_home.MyViewHolder> {
    Context context;
    ArrayList<favouritesmodel> favouritesmodels;

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
        holder.iv_trending.setImageResource(favouritesmodels.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return favouritesmodels.size()  ;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_trending;
        TextView tv1, tv2, tv3, tv4;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_trending = itemView.findViewById(R.id.trending_img);
            tv1 = itemView.findViewById(R.id.ticker_port);
            tv2 = itemView.findViewById(R.id.tv_var1_port);
            tv3 = itemView.findViewById(R.id.tv_var2_port);
            tv4 = itemView.findViewById(R.id.tv_var3_port);

        }
    }
}

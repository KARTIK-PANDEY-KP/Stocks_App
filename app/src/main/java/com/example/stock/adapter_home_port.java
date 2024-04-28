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

public class adapter_home_port extends RecyclerView.Adapter<adapter_home_port.MyViewHolder> {
    Context context;
    ArrayList<portfoliomodel> portfoliomodels;

    public adapter_home_port(Context context, ArrayList<portfoliomodel> portfoliomodels) {
        this.context = context;
        this.portfoliomodels = portfoliomodels;
    }

    @NonNull
    @Override
    public adapter_home_port.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.rvport_row, parent, false);
        return new adapter_home_port.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull adapter_home_port.MyViewHolder holder, int position) {
        holder.tv1.setText(portfoliomodels.get(position).getTicker());
        holder.tv2.setText(portfoliomodels.get(position).getQty_owned());
        holder.tv3.setText(portfoliomodels.get(position).getMarket_value());
        holder.tv4.setText(portfoliomodels.get(position).getResult());
        holder.iv_trending.setImageResource(portfoliomodels.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return portfoliomodels.size()  ;
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

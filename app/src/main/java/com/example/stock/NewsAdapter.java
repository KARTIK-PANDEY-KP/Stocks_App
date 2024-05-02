package com.example.stock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {
    private final newsModelViewInterface newsModelViewInterface;
    Context context;
    ArrayList<newsmodel> newsmodels;


    public NewsAdapter(Context context, ArrayList<newsmodel> newsmodels, newsModelViewInterface newsModelViewInterface) {
        this.context = context;
        this.newsmodels = newsmodels;
        this.newsModelViewInterface = newsModelViewInterface;
    }

    @NonNull
    @Override
    public NewsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.newsitem_layout, parent, false);
        return new NewsAdapter.MyViewHolder(view, newsModelViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsAdapter.MyViewHolder holder, int position) {
        holder.tv1.setText(newsmodels.get(position).getHeading());
        holder.tv2.setText(newsmodels.get(position).getDatetime());
        holder.tv3.setText(newsmodels.get(position).getSummary());
        Picasso.get().load(newsmodels.get(position).getImgurl()).into(holder.imgview);

    }

    @Override
    public int getItemCount() {
        return newsmodels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgview;
        TextView tv1, tv2, tv3;

        public MyViewHolder(@NonNull View itemView, newsModelViewInterface newsModelViewInterface) {
            super(itemView);
            imgview = itemView.findViewById(R.id.imgf);
            tv1 = itemView.findViewById(R.id.headlinef);
            tv2 = itemView.findViewById(R.id.datef);
            tv3 = itemView.findViewById(R.id.summf);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(newsModelViewInterface != null){
                        int pos = getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION){
                            newsModelViewInterface.onitemclick(pos);
                        }
                    }
                }
            } );
        }
    }
}
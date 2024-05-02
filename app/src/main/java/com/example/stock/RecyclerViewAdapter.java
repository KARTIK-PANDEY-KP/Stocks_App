package com.example.stock;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private ArrayList<String> names = new ArrayList<>();
    private Context context;
    public ObservableField<String> selectedName = new ObservableField<>();

    public RecyclerViewAdapter(Context context, ArrayList<String> names) {
        this.names = names;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fun, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.name.setText(names.get(position));
        holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        holder.name.setOnClickListener(v -> {
            Log.d("SUBMIT", names.get(position));
            selectedName.setValue(names.get(position));
//            Intent callSearchActivity = new Intent(context, searchactivity.class);
//            callSearchActivity.putExtra("query", names.get(position));
//            callSearchActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            context.startActivity(callSearchActivity);
        });
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.fun);
        }
    }
}

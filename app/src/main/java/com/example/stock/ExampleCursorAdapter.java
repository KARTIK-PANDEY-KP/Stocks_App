package com.example.stock;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

public class ExampleCursorAdapter extends CursorAdapter {
    public ExampleCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, false);
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view;
        int columnIndex = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
        if (columnIndex != -1) {
            textView.setText(cursor.getString(columnIndex));
        } else {
            textView.setText("Column not found");  // or handle the error in a way that suits your app
        }
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        TextView textView = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        // Adjust the text view layout parameters if necessary
        ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        textView.setLayoutParams(layoutParams);
        return textView;
    }

    public void flushData() {
        Cursor oldCursor = getCursor();
        if (oldCursor != null) {
            oldCursor.close();  // Close the old cursor to release its resources
        }
        changeCursor(null);  // Optionally, set the cursor to null to clear the adapter's reference
    }

    // Method to refresh the adapter with new data
    public void refreshData(Cursor newCursor) {
        changeCursor(newCursor);  // Change to a new cursor and automatically close the old one
    }

}

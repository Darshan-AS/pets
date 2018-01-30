package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetsContract;


public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View petView, Context context, Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex(PetsContract.PetEntry.COLUMN_PET_NAME));
        String summary = cursor.getString(cursor.getColumnIndex(PetsContract.PetEntry.COLUMN_PET_BREED));
        if (summary.isEmpty())
            summary = context.getString(R.string.unknown_breed);

        TextView nameView = petView.findViewById(R.id.name);
        nameView.setText(name);
        TextView summaryView = petView.findViewById(R.id.summary);
        summaryView.setText(summary);
    }
}

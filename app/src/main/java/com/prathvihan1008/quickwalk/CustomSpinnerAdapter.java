package com.prathvihan1008.quickwalk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

// CustomSpinnerAdapter.java
public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    public CustomSpinnerAdapter(Context context, int custom_spinner_dropdown_item, List<String> items) {
        super(context, R.layout.spinner_item_layout, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }


    private View getCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_layout, parent, false);
        }

        ImageView iconImageView = convertView.findViewById(R.id.spinnerItemIcon);
       TextView textTextView = convertView.findViewById(R.id.spinnerItemText);

        // Get the current item from the data list
        String currentItem = getItem(position);

        // Customize the item based on the position or data
        if (currentItem != null) {
            textTextView.setText(currentItem);

            // Set icon based on the current item
            if (currentItem.equals("Walking")) {
                iconImageView.setImageResource(R.drawable.icon1);
            } else if (currentItem.equals("Jogging")) {
                iconImageView.setImageResource(R.drawable.icon2);
            } else if (currentItem.equals("Biking")) {
                iconImageView.setImageResource(R.drawable.icon3);
            } else {
                // Set a default icon or handle other cases
                iconImageView.setImageResource(R.drawable.icon2);
            }
        }

        return convertView;
    }


}

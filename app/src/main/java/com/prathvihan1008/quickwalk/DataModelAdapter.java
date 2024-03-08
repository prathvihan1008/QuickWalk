package com.prathvihan1008.quickwalk;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DataModelAdapter extends RecyclerView.Adapter<DataModelAdapter.ViewHolder> {

    private List<DataModel> dataModels;

    public DataModelAdapter(List<DataModel> dataModels) {
        this.dataModels = dataModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataModel dataModel = dataModels.get(position);
        holder.bindData(dataModel);
    }

    @Override
    public int getItemCount() {
        return dataModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTime;
        TextView textViewSteps;
        TextView textViewDistance;
        TextView textViewCalories;
        TextView textViewDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewSteps = itemView.findViewById(R.id.textViewSteps);
            textViewDistance = itemView.findViewById(R.id.textViewDistance);
            textViewCalories = itemView.findViewById(R.id.textViewCalories);
           // textViewDate = itemView.findViewById(R.id.textViewDate);
        }

        public void bindData(DataModel dataModel) {
            textViewTime.setText("Time Spent:               " + dataModel.getTime()+" (min)");
            textViewSteps.setText("Steps:                         " + dataModel.getSteps()+"");
            textViewDistance.setText("Distance Moved:       " + dataModel.getDistance()+" (miles)");
            textViewCalories.setText("Calories Burned:       " + dataModel.getCalories());
            //textViewDate.setText("Date: " + dataModel.getDate());
        }
    }
}

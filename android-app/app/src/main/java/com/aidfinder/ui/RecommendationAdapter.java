package com.aidfinder.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aidfinder.R;
import com.aidfinder.RecommendationResult;

import java.util.ArrayList;
import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.VH> {

    private final List<RecommendationResult> items = new ArrayList<>();

    public void submit(List<RecommendationResult> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        RecommendationResult item = items.get(position);
        holder.name.setText(item.product.name);
        holder.why.setText(item.why);
        holder.description.setText(item.product.description);

        // Screen readers announce the card as one unit instead of three fragments
        holder.itemView.setContentDescription(
                item.product.name + ". " + item.why + " " + item.product.description);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name, why, description;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.productName);
            why = itemView.findViewById(R.id.why);
            description = itemView.findViewById(R.id.description);
        }
    }
}

package com.aidfinder.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aidfinder.R;
import com.aidfinder.RecommendationResult;
import com.aidfinder.auth.db.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(RecommendationResult item);
    }

    private final List<RecommendationResult> items = new ArrayList<>();
    private final DatabaseHelper databaseHelper;
    private final String username;
    private Runnable onFavoriteChanged;
    private OnItemClickListener onItemClick;

    public RecommendationAdapter(DatabaseHelper databaseHelper, String username) {
        this.databaseHelper = databaseHelper;
        this.username = username;
    }

    /** Called after a favorite is added or removed, so a caller showing only favorites can refresh. */
    public void setOnFavoriteChanged(Runnable listener) {
        this.onFavoriteChanged = listener;
    }

    /** Called when the card body (not the star button) is tapped - e.g. to open its device journal. */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClick = listener;
    }

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

        bindFavoriteButton(holder.favoriteButton, item);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) {
                onItemClick.onItemClick(item);
            }
        });
    }

    private void bindFavoriteButton(ImageButton button, RecommendationResult item) {
        boolean favorited = databaseHelper.isFavorite(username, item.product.id);
        setFavoriteIcon(button, favorited);

        button.setOnClickListener(v -> {
            boolean nowFavorited = !databaseHelper.isFavorite(username, item.product.id);
            if (nowFavorited) {
                databaseHelper.addFavorite(username, item.product.id, item.product.name, item.why);
            } else {
                databaseHelper.removeFavorite(username, item.product.id);
            }
            setFavoriteIcon(button, nowFavorited);
            if (onFavoriteChanged != null) {
                onFavoriteChanged.run();
            }
        });
    }

    private void setFavoriteIcon(ImageButton button, boolean favorited) {
        button.setImageResource(favorited ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        button.setContentDescription(button.getResources().getString(
                favorited ? R.string.cd_remove_favorite : R.string.cd_add_favorite));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name, why, description;
        final ImageButton favoriteButton;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.productName);
            why = itemView.findViewById(R.id.why);
            description = itemView.findViewById(R.id.description);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
}

package com.aidfinder.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aidfinder.R;
import com.aidfinder.auth.model.JournalEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Read-only history list for one device's journal - newest entry first. */
public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.VH> {

    private static final int MAX_STARS = 5;
    private final List<JournalEntry> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    /** Pass entries oldest-first (as getJournalEntries returns them) - this reverses for display. */
    public void submit(List<JournalEntry> entriesOldestFirst) {
        items.clear();
        for (int i = entriesOldestFirst.size() - 1; i >= 0; i--) {
            items.add(entriesOldestFirst.get(i));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journal_entry, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        JournalEntry entry = items.get(position);
        holder.date.setText(dateFormat.format(new Date(entry.getCreatedAtMillis())));
        holder.text.setText(entry.getEntryText());

        holder.starsRow.removeAllViews();
        for (int i = 1; i <= MAX_STARS; i++) {
            ImageView star = new ImageView(holder.starsRow.getContext());
            int sizePx = (int) (16 * holder.starsRow.getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
            params.leftMargin = i == 1 ? 0 : (int) (2 * holder.starsRow.getResources().getDisplayMetrics().density);
            star.setLayoutParams(params);
            boolean filled = i <= entry.getRating();
            star.setImageResource(filled ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            star.setColorFilter(ContextCompat.getColor(star.getContext(),
                    filled ? R.color.accent : R.color.line));
            holder.starsRow.addView(star);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView date, text;
        final LinearLayout starsRow;

        VH(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.tvEntryDate);
            text = itemView.findViewById(R.id.tvEntryText);
            starsRow = itemView.findViewById(R.id.entryRatingStars);
        }
    }
}

package com.aidfinder.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aidfinder.R;
import com.aidfinder.auth.UserSession;
import com.aidfinder.auth.db.DatabaseHelper;
import com.aidfinder.auth.model.JournalEntry;

import java.util.ArrayList;
import java.util.List;

/** One device's log: a fresh 1-5 rating + a note each time, plus the history of past entries. */
public class DeviceJournalActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "PRODUCT_ID";
    public static final String EXTRA_PRODUCT_NAME = "PRODUCT_NAME";

    private static final int MAX_STARS = 5;

    private DatabaseHelper databaseHelper;
    private String username;
    private String productId;
    private String productName;

    private EditText etEntryText;
    private LinearLayout ratingStarsRow;
    private final ImageView[] ratingStars = new ImageView[MAX_STARS];
    private int currentRating = 0;

    private TextView tvNoEntries;
    private RecyclerView rvEntries;
    private JournalEntryAdapter entryAdapter;

    private TextView tvTimelineTitle;
    private FrameLayout timelineContainer;
    private RatingTimelineView timelineView;

    public static Intent newIntent(Context context, String productId, String productName) {
        Intent intent = new Intent(context, DeviceJournalActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, productId);
        intent.putExtra(EXTRA_PRODUCT_NAME, productName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_journal);

        productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
        username = UserSession.getUsername(this);
        databaseHelper = new DatabaseHelper(this);

        TextView tvDeviceName = findViewById(R.id.tvDeviceName);
        tvDeviceName.setText(productName);

        etEntryText = findViewById(R.id.etEntryText);
        ratingStarsRow = findViewById(R.id.ratingStars);
        Button btnSaveEntry = findViewById(R.id.btnSaveEntry);

        tvNoEntries = findViewById(R.id.tvNoEntries);
        rvEntries = findViewById(R.id.rvEntries);
        entryAdapter = new JournalEntryAdapter();
        rvEntries.setLayoutManager(new LinearLayoutManager(this));
        rvEntries.setAdapter(entryAdapter);

        tvTimelineTitle = findViewById(R.id.tvTimelineTitle);
        timelineContainer = findViewById(R.id.timelineContainer);
        timelineView = new RatingTimelineView(this);
        timelineContainer.addView(timelineView);

        buildRatingPicker();
        btnSaveEntry.setOnClickListener(v -> saveEntry());

        loadEntries();
    }

    private void buildRatingPicker() {
        ratingStarsRow.removeAllViews();
        int sizePx = (int) (32 * getResources().getDisplayMetrics().density);
        int marginPx = (int) (4 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < MAX_STARS; i++) {
            int starValue = i + 1;
            ImageView star = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
            params.leftMargin = i == 0 ? 0 : marginPx;
            star.setLayoutParams(params);
            star.setContentDescription(getString(R.string.cd_rating_star, starValue));
            star.setOnClickListener(v -> {
                currentRating = starValue;
                renderRatingPicker();
            });
            ratingStars[i] = star;
            ratingStarsRow.addView(star);
        }
        renderRatingPicker();
    }

    /** currentRating starts at 0 every time this screen opens - never pre-filled from a past entry. */
    private void renderRatingPicker() {
        for (int i = 0; i < MAX_STARS; i++) {
            boolean filled = (i + 1) <= currentRating;
            ratingStars[i].setImageResource(filled ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            ratingStars[i].setColorFilter(ContextCompat.getColor(this, filled ? R.color.accent : R.color.line));
        }
    }

    private void saveEntry() {
        String text = etEntryText.getText() == null ? "" : etEntryText.getText().toString().trim();

        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, R.string.err_entry_text_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentRating < 1) {
            Toast.makeText(this, R.string.err_rating_required, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = databaseHelper.addJournalEntry(username, productId, productName, text, currentRating);
        if (!success) {
            Toast.makeText(this, "Couldn't save that entry. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        etEntryText.setText("");
        currentRating = 0;
        renderRatingPicker();
        loadEntries();
    }

    private void loadEntries() {
        List<JournalEntry> entries = databaseHelper.getJournalEntries(username, productId);
        entryAdapter.submit(entries);

        boolean hasEntries = !entries.isEmpty();
        tvNoEntries.setVisibility(hasEntries ? View.GONE : View.VISIBLE);
        rvEntries.setVisibility(hasEntries ? View.VISIBLE : View.GONE);

        // A single point can't show a trend - only worth drawing with 2+ entries.
        boolean hasTrend = entries.size() >= 2;
        tvTimelineTitle.setVisibility(hasTrend ? View.VISIBLE : View.GONE);
        timelineContainer.setVisibility(hasTrend ? View.VISIBLE : View.GONE);
        if (hasTrend) {
            List<Integer> ratings = new ArrayList<>();
            for (JournalEntry entry : entries) {
                ratings.add(entry.getRating());
            }
            timelineView.setRatings(ratings);
        }
    }
}

package com.example.flashcard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.widget.NestedScrollView;

import com.example.flashcard.R;
import com.example.flashcard.data.TestResult;
import com.example.flashcard.ui.adapter.TestResultAdapter;
import com.example.flashcard.viewmodel.FlashcardViewModel;

import java.util.List;

public class TestHistoryActivity extends AppCompatActivity {

    private FlashcardViewModel viewModel;
    private TestResultAdapter recentAdapter;
    private TestResultAdapter permanentAdapter;
    private View tvEmptyState;
    private TextView tvRecentHeader;
    private TextView tvPermanentHeader;
    private RecyclerView recyclerViewRecent;
    private RecyclerView recyclerViewPermanent;
    private NestedScrollView historyScrollView;

    private boolean hasRecent = false;
    private boolean hasPermanent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_history);

        com.example.flashcard.utils.FeatureTutorial.showOnce(this, "test_history",
                com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_history_title, R.string.guide_history_message));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Test History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        tvEmptyState = findViewById(R.id.llEmptyState);
        View btnEmptyTakeTest = findViewById(R.id.btnEmptyTakeTest);
        btnEmptyTakeTest.setOnClickListener(v -> {
            startActivity(new Intent(TestHistoryActivity.this, TestSetupActivity.class));
            finish();
        });
        tvRecentHeader = findViewById(R.id.tvRecentHeader);
        tvPermanentHeader = findViewById(R.id.tvPermanentHeader);
        recyclerViewRecent = findViewById(R.id.recyclerViewRecent);
        recyclerViewPermanent = findViewById(R.id.recyclerViewPermanent);
        historyScrollView = findViewById(R.id.historyScrollView);

        recyclerViewRecent.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPermanent.setLayoutManager(new LinearLayoutManager(this));

        TestResultAdapter.OnItemClickListener listener = new TestResultAdapter.OnItemClickListener() {
            @Override
            public void onViewDetails(TestResult result) {
                Intent intent = new Intent(TestHistoryActivity.this, TestResultDetailsActivity.class);
                intent.putExtra("TEST_RESULT_ID", result.id);
                startActivity(intent);
            }

            @Override
            public void onDelete(TestResult result) {
                new AlertDialog.Builder(TestHistoryActivity.this)
                        .setTitle("Delete Result")
                        .setMessage("Are you sure you want to delete this test result from history?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            viewModel.deleteTestResult(result.id);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        };

        recentAdapter = new TestResultAdapter(listener);
        permanentAdapter = new TestResultAdapter(listener);

        recyclerViewRecent.setAdapter(recentAdapter);
        recyclerViewPermanent.setAdapter(permanentAdapter);

        viewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);

        viewModel.getLatest5Results().observe(this, results -> {
            hasRecent = results != null && !results.isEmpty();
            if (hasRecent) {
                tvRecentHeader.setVisibility(View.VISIBLE);
                recyclerViewRecent.setVisibility(View.VISIBLE);
                recentAdapter.setResults(results);
            } else {
                tvRecentHeader.setVisibility(View.GONE);
                recyclerViewRecent.setVisibility(View.GONE);
            }
            updateEmptyState();
        });

        viewModel.getPermanentlySavedResults().observe(this, results -> {
            hasPermanent = results != null && !results.isEmpty();
            if (hasPermanent) {
                tvPermanentHeader.setVisibility(View.VISIBLE);
                recyclerViewPermanent.setVisibility(View.VISIBLE);
                permanentAdapter.setResults(results);
            } else {
                tvPermanentHeader.setVisibility(View.GONE);
                recyclerViewPermanent.setVisibility(View.GONE);
            }
            updateEmptyState();
        });
    }

    private void updateEmptyState() {
        if (!hasRecent && !hasPermanent) {
            tvEmptyState.setVisibility(View.VISIBLE);
            historyScrollView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            historyScrollView.setVisibility(View.VISIBLE);
        }
    }
}

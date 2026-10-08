package com.example.flashcard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.flashcard.R;
import com.example.flashcard.data.ExamPlan;
import com.example.flashcard.viewmodel.FlashcardViewModel;

import java.util.List;

public class ExamPlansActivity extends AppCompatActivity {

    @Override
    protected void onStop() {
        super.onStop();
        com.example.flashcard.widget.FlashcardWidgetProvider.requestUpdate(this);
    }

    private FlashcardViewModel flashcardViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_plans);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_exam_plans);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_exam_plans);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ExamPlanAdapter adapter = new ExamPlanAdapter();
        recyclerView.setAdapter(adapter);

        TextView textEmpty = findViewById(R.id.text_empty_plans);

        flashcardViewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);
        flashcardViewModel.getAllExamPlans().observe(this, new Observer<List<ExamPlan>>() {
            @Override
            public void onChanged(@Nullable List<ExamPlan> plans) {
                adapter.submitList(plans);
                if (plans == null || plans.isEmpty()) {
                    textEmpty.setVisibility(View.VISIBLE);
                } else {
                    textEmpty.setVisibility(View.GONE);
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab_add_exam_plan);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExamPlansActivity.this, CreateEditPlanActivity.class);
                startActivity(intent);
            }
        });

        adapter.setOnItemClickListener(new ExamPlanAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ExamPlan plan) {
                // For now, let's open PlanDetailsActivity (which we will create next)
                Intent intent = new Intent(ExamPlansActivity.this, PlanDetailsActivity.class);
                intent.putExtra(PlanDetailsActivity.EXTRA_PLAN_ID, plan.id);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

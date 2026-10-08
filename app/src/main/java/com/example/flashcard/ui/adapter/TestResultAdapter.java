package com.example.flashcard.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.data.TestResult;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TestResultAdapter extends ListAdapter<TestResult, TestResultAdapter.TestResultViewHolder> {

    private final OnItemClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault());

    public interface OnItemClickListener {
        void onViewDetails(TestResult result);
        void onDelete(TestResult result);
    }

    public TestResultAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<TestResult>() {
            @Override
            public boolean areItemsTheSame(@NonNull TestResult oldItem, @NonNull TestResult newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull TestResult oldItem, @NonNull TestResult newItem) {
                return oldItem.completedAt == newItem.completedAt &&
                       oldItem.score == newItem.score &&
                       oldItem.totalCards == newItem.totalCards &&
                       oldItem.correctAnswers == newItem.correctAnswers &&
                       oldItem.incorrectAnswers == newItem.incorrectAnswers &&
                       oldItem.isPermanentlySaved == newItem.isPermanentlySaved;
            }
        });
        this.listener = listener;
    }

    // Keep setResults for backward compatibility with the Activity that calls it
    public void setResults(java.util.List<TestResult> results) {
        submitList(results);
    }

    @NonNull
    @Override
    public TestResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test_result, parent, false);
        return new TestResultViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TestResultViewHolder holder, int position) {
        TestResult result = getItem(position);

        holder.tvDate.setText(dateFormat.format(new Date(result.completedAt)));
        holder.tvScore.setText(result.score + "%");

        String subjectText = "Subject: " + (result.subjectName != null ? result.subjectName : "All Subjects");
        subjectText += " | Diff: " + (result.difficulty != null ? result.difficulty : "All");
        holder.tvSubject.setText(subjectText);

        String detailsStr = "Cards: " + result.totalCards + " (+" + result.correctAnswers + ", -" + result.incorrectAnswers;
        if (result.unanswered > 0) {
            detailsStr += ", Unans: " + result.unanswered;
        }
        detailsStr += ")";

        if (result.timeUsedMillis > 0) {
            detailsStr += " | Time: " + formatTime(result.timeUsedMillis);
        }

        holder.tvDetails.setText(detailsStr);

        holder.btnViewDetails.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if(pos != RecyclerView.NO_POSITION) {
                listener.onViewDetails(getItem(pos));
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if(pos != RecyclerView.NO_POSITION) {
                listener.onDelete(getItem(pos));
            }
        });
    }

    private String formatTime(long millis) {
        int m = (int) (millis / 1000) / 60;
        int s = (int) (millis / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", m, s);
    }

    static class TestResultViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvScore;
        TextView tvSubject;
        TextView tvDetails;
        MaterialButton btnViewDetails;
        MaterialButton btnDelete;

        public TestResultViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
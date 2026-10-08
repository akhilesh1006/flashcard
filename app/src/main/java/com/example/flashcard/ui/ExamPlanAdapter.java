package com.example.flashcard.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.data.ExamPlan;

public class ExamPlanAdapter extends ListAdapter<ExamPlan, ExamPlanAdapter.ExamPlanHolder> {

    private OnItemClickListener listener;

    public ExamPlanAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ExamPlan> DIFF_CALLBACK = new DiffUtil.ItemCallback<ExamPlan>() {
        @Override
        public boolean areItemsTheSame(@NonNull ExamPlan oldItem, @NonNull ExamPlan newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ExamPlan oldItem, @NonNull ExamPlan newItem) {
            return oldItem.name.equals(newItem.name) &&
                   oldItem.examDate.equals(newItem.examDate);
        }
    };

    @NonNull
    @Override
    public ExamPlanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exam_plan, parent, false);
        return new ExamPlanHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamPlanHolder holder, int position) {
        ExamPlan currentPlan = getItem(position);
        holder.textViewName.setText(currentPlan.name);
        holder.textViewDates.setText("Started: " + currentPlan.startDate + " - Exam: " + currentPlan.examDate);
        if (currentPlan.archived) {
            holder.textViewStatus.setText("ARCHIVED");
        } else if (currentPlan.completedAt > 0) {
            holder.textViewStatus.setText("COMPLETED");
        } else {
            holder.textViewStatus.setText("ACTIVE");
        }
    }

    class ExamPlanHolder extends RecyclerView.ViewHolder {
        private TextView textViewName;
        private TextView textViewDates;
        private TextView textViewStatus;

        public ExamPlanHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_plan_name);
            textViewDates = itemView.findViewById(R.id.text_plan_dates);
            textViewStatus = itemView.findViewById(R.id.text_plan_status);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(getItem(position));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ExamPlan plan);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}

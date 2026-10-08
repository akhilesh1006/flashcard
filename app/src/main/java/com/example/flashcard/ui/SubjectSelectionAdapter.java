package com.example.flashcard.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flashcard.R;
import com.example.flashcard.data.SubjectWithCount;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubjectSelectionAdapter extends RecyclerView.Adapter<SubjectSelectionAdapter.SubjectHolder> {

    private List<SubjectWithCount> subjects = new ArrayList<>();
    private Set<Integer> selectedSubjectIds = new HashSet<>();

    public void setSubjects(List<SubjectWithCount> subjects) {
        this.subjects = subjects;
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedSubjectIds() {
        return new ArrayList<>(selectedSubjectIds);
    }

    @NonNull
    @Override
    public SubjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject_selection, parent, false);
        return new SubjectHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectHolder holder, int position) {
        SubjectWithCount currentSubject = subjects.get(position);
        holder.textViewName.setText(currentSubject.subject.getName());
        holder.textViewCount.setText(currentSubject.flashcardCount + " cards");

        holder.checkBox.setChecked(selectedSubjectIds.contains(currentSubject.subject.getId()));
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    class SubjectHolder extends RecyclerView.ViewHolder {
        private TextView textViewName;
        private TextView textViewCount;
        private CheckBox checkBox;

        public SubjectHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_subject_name);
            textViewCount = itemView.findViewById(R.id.text_subject_count);
            checkBox = itemView.findViewById(R.id.checkbox_subject);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        SubjectWithCount currentSubject = subjects.get(position);
                        if (selectedSubjectIds.contains(currentSubject.subject.getId())) {
                            selectedSubjectIds.remove(currentSubject.subject.getId());
                        } else {
                            selectedSubjectIds.add(currentSubject.subject.getId());
                        }
                        notifyItemChanged(position);
                    }
                }
            });
        }
    }
}

package com.example.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.data.Subject;
import com.example.flashcard.data.SubjectWithCount;
import com.example.flashcard.viewmodel.FlashcardViewModel;
import com.example.flashcard.utils.FlashcardColorUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SubjectsActivity extends AppCompatActivity {

    private FlashcardViewModel flashcardViewModel;
    private SubjectAdapter adapter;
    private RecyclerView rvSubjects;
    private LinearLayout llEmptyStateSubjects;
    private FloatingActionButton fabAddSubject;
    private MaterialButton btnEmptyAddSubject;
    private TextInputEditText etSearchSubject;

    private List<SubjectWithCount> allSubjectsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_subjects);

        com.example.flashcard.utils.FeatureTutorial.showOnce(this, "subjects",
                com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_subjects_title, R.string.guide_subjects_message));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rvSubjects), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            findViewById(android.R.id.content).setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvSubjects = findViewById(R.id.rvSubjects);
        llEmptyStateSubjects = findViewById(R.id.llEmptyStateSubjects);
        fabAddSubject = findViewById(R.id.fabAddSubject);
        btnEmptyAddSubject = findViewById(R.id.btnEmptyAddSubject);
        etSearchSubject = findViewById(R.id.etSearchSubject);

        rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubjectAdapter();
        rvSubjects.setAdapter(adapter);

        flashcardViewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);

        flashcardViewModel.getAllSubjectsWithCount().observe(this, subjects -> {
            if (subjects != null) {
                allSubjectsList = subjects;
                filterSubjects(etSearchSubject.getText().toString());
            }
        });

        fabAddSubject.setOnClickListener(v -> showAddEditSubjectDialog(null));
        btnEmptyAddSubject.setOnClickListener(v -> showAddEditSubjectDialog(null));

        etSearchSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSubjects(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void filterSubjects(String query) {
        if (allSubjectsList == null || allSubjectsList.isEmpty()) {
            rvSubjects.setVisibility(View.GONE);
            llEmptyStateSubjects.setVisibility(View.VISIBLE);
            fabAddSubject.setVisibility(View.GONE);
            return;
        }

        List<SubjectWithCount> filteredList = new ArrayList<>();
        if (TextUtils.isEmpty(query)) {
            filteredList.addAll(allSubjectsList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (SubjectWithCount subject : allSubjectsList) {
                if (subject.subject.getName().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(subject);
                }
            }
        }

        if (filteredList.isEmpty() && !TextUtils.isEmpty(query)) {
            // Still show empty state, but maybe we can just say "no results"
            // For now, let's just use the normal empty state but keep fab
            rvSubjects.setVisibility(View.GONE);
            llEmptyStateSubjects.setVisibility(View.VISIBLE);
            fabAddSubject.setVisibility(View.VISIBLE);
            TextView emptyTitle = findViewById(R.id.tvEmptyTitle);
            TextView emptyDesc = findViewById(R.id.tvEmptyDesc);
            emptyTitle.setText("No matches found");
            emptyDesc.setText("Try a different search term");
            btnEmptyAddSubject.setVisibility(View.GONE);
        } else if (filteredList.isEmpty()) {
            rvSubjects.setVisibility(View.GONE);
            llEmptyStateSubjects.setVisibility(View.VISIBLE);
            fabAddSubject.setVisibility(View.GONE);
            TextView emptyTitle = findViewById(R.id.tvEmptyTitle);
            TextView emptyDesc = findViewById(R.id.tvEmptyDesc);
            emptyTitle.setText("No subjects yet");
            emptyDesc.setText("Create a subject to organize your flashcards.");
            btnEmptyAddSubject.setVisibility(View.VISIBLE);
        } else {
            rvSubjects.setVisibility(View.VISIBLE);
            llEmptyStateSubjects.setVisibility(View.GONE);
            fabAddSubject.setVisibility(View.VISIBLE);
        }
        
        adapter.setSubjects(filteredList);
    }

    private void showAddEditSubjectDialog(Subject subjectToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_subject, null);

        TextView tvDialogTitle = viewInflated.findViewById(R.id.tvDialogTitle);
        TextInputEditText etSubjectName = viewInflated.findViewById(R.id.etSubjectName);
        MaterialButton btnDialogDiffEasy = viewInflated.findViewById(R.id.btnDialogDiffEasy);
        MaterialButton btnDialogDiffMedium = viewInflated.findViewById(R.id.btnDialogDiffMedium);
        MaterialButton btnDialogDiffHard = viewInflated.findViewById(R.id.btnDialogDiffHard);

        final String[] selectedDiff = new String[]{"MEDIUM"};

        View.OnClickListener diffClickListener = v -> {
            btnDialogDiffEasy.setChecked(false);
            btnDialogDiffMedium.setChecked(false);
            btnDialogDiffHard.setChecked(false);

            if (v.getId() == R.id.btnDialogDiffEasy) {
                btnDialogDiffEasy.setChecked(true);
                selectedDiff[0] = "EASY";
            } else if (v.getId() == R.id.btnDialogDiffMedium) {
                btnDialogDiffMedium.setChecked(true);
                selectedDiff[0] = "MEDIUM";
            } else if (v.getId() == R.id.btnDialogDiffHard) {
                btnDialogDiffHard.setChecked(true);
                selectedDiff[0] = "HARD";
            }
        };

        btnDialogDiffEasy.setOnClickListener(diffClickListener);
        btnDialogDiffMedium.setOnClickListener(diffClickListener);
        btnDialogDiffHard.setOnClickListener(diffClickListener);

        if (subjectToEdit != null) {
            tvDialogTitle.setText("Edit Subject");
            etSubjectName.setText(subjectToEdit.getName());
            selectedDiff[0] = subjectToEdit.getDifficulty();
        } else {
            tvDialogTitle.setText("Add Subject");
            selectedDiff[0] = "MEDIUM";
        }

        // Set initial checked state
        btnDialogDiffEasy.setChecked("EASY".equals(selectedDiff[0]));
        btnDialogDiffMedium.setChecked("MEDIUM".equals(selectedDiff[0]));
        btnDialogDiffHard.setChecked("HARD".equals(selectedDiff[0]));

        builder.setView(viewInflated);
        builder.setPositiveButton(subjectToEdit != null ? "Save" : "Add", (dialog, which) -> { });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etSubjectName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(SubjectsActivity.this, "Subject name cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                boolean exists = false;
                for (SubjectWithCount sc : allSubjectsList) { // Check against ALL subjects
                    if (sc.subject.getName().equalsIgnoreCase(name) &&
                        (subjectToEdit == null || sc.subject.getId() != subjectToEdit.getId())) {
                        exists = true;
                        break;
                    }
                }

                if (exists) {
                    Toast.makeText(SubjectsActivity.this, "Subject already exists", Toast.LENGTH_SHORT).show();
                } else {
                    if (subjectToEdit != null) {
                        subjectToEdit.setName(name);
                        subjectToEdit.setDifficulty(selectedDiff[0]);
                        flashcardViewModel.updateSubject(subjectToEdit);
                    } else {
                        flashcardViewModel.insertSubject(new Subject(name, selectedDiff[0]));
                    }
                    dialog.dismiss();
                }
            }
        });
    }

    private class SubjectAdapter extends androidx.recyclerview.widget.ListAdapter<SubjectWithCount, SubjectAdapter.SubjectHolder> {

        public SubjectAdapter() {
            super(new androidx.recyclerview.widget.DiffUtil.ItemCallback<SubjectWithCount>() {
                @Override
                public boolean areItemsTheSame(@NonNull SubjectWithCount oldItem, @NonNull SubjectWithCount newItem) {
                    return oldItem.subject.getId() == newItem.subject.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull SubjectWithCount oldItem, @NonNull SubjectWithCount newItem) {
                    return oldItem.subject.getName().equals(newItem.subject.getName()) &&
                           oldItem.subject.getDifficulty().equals(newItem.subject.getDifficulty()) &&
                           oldItem.flashcardCount == newItem.flashcardCount;
                }
            });
        }

        public void setSubjects(List<SubjectWithCount> subjects) {
            submitList(subjects);
        }

        @NonNull
        @Override
        public SubjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_subject, parent, false);
            return new SubjectHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SubjectHolder holder, int position) {
            SubjectWithCount currentSubjectWithCount = getItem(position);
            Subject currentSubject = currentSubjectWithCount.subject;
            holder.tvSubjectName.setText(currentSubject.getName());
            holder.tvSubjectCount.setText(currentSubjectWithCount.flashcardCount + (currentSubjectWithCount.flashcardCount == 1 ? " Flashcard" : " Flashcards"));

            holder.tvSubjectDifficulty.setText("Difficulty: " + currentSubject.getDifficulty());
            if ("EASY".equals(currentSubject.getDifficulty())) {
                holder.tvSubjectDifficulty.setTextColor(ContextCompat.getColor(SubjectsActivity.this, R.color.diff_easy));
            } else if ("HARD".equals(currentSubject.getDifficulty())) {
                holder.tvSubjectDifficulty.setTextColor(ContextCompat.getColor(SubjectsActivity.this, R.color.diff_hard));
            } else {
                holder.tvSubjectDifficulty.setTextColor(ContextCompat.getColor(SubjectsActivity.this, R.color.diff_medium));
            }

            int color = FlashcardColorUtils.getColorForString(currentSubject.getName());
            ((MaterialCardView) holder.itemView).setStrokeColor(color);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(SubjectsActivity.this, FlashcardActivity.class);
                intent.putExtra("SUBJECT_ID", currentSubject.getId());
                startActivity(intent);
            });

            holder.btnEditSubject.setOnClickListener(v -> showAddEditSubjectDialog(currentSubject));

            holder.btnDeleteSubject.setOnClickListener(v -> {
                new AlertDialog.Builder(SubjectsActivity.this)
                        .setTitle("Delete Subject?")
                        .setMessage("Deleting this subject will also delete all flashcards inside it.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            flashcardViewModel.deleteSubject(currentSubject);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        class SubjectHolder extends RecyclerView.ViewHolder {
            private TextView tvSubjectName;
            private TextView tvSubjectCount;
            private TextView tvSubjectDifficulty;
            private ImageButton btnEditSubject;
            private ImageButton btnDeleteSubject;

            public SubjectHolder(@NonNull View itemView) {
                super(itemView);
                tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
                tvSubjectCount = itemView.findViewById(R.id.tvSubjectCount);
                tvSubjectDifficulty = itemView.findViewById(R.id.tvSubjectDifficulty);
                btnEditSubject = itemView.findViewById(R.id.btnEditSubject);
                btnDeleteSubject = itemView.findViewById(R.id.btnDeleteSubject);
            }
        }
    }
}

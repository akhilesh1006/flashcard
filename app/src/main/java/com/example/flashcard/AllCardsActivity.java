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
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.data.FlashcardWithSubjectName;
import com.example.flashcard.viewmodel.FlashcardViewModel;
import com.example.flashcard.utils.FlashcardColorUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AllCardsActivity extends AppCompatActivity {

    private FlashcardViewModel flashcardViewModel;
    private AllCardsAdapter adapter;
    private RecyclerView rvAllCards;
    private LinearLayout llEmptyStateAllCards;
    private TextView tvAllCardsCount;
    private TextInputEditText etSearchCards;
    private ImageButton btnFilterCards;

    private List<FlashcardWithSubjectName> allCardsList = new ArrayList<>();
    private String currentDifficultyFilter = "ALL"; // ALL, EASY, MEDIUM, HARD

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_cards);

        com.example.flashcard.utils.FeatureTutorial.showOnce(this, "all_cards",
                com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_all_cards_title, R.string.guide_all_cards_message));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rvAllCards), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            findViewById(android.R.id.content).setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvAllCards = findViewById(R.id.rvAllCards);
        llEmptyStateAllCards = findViewById(R.id.llEmptyStateAllCards);
        tvAllCardsCount = findViewById(R.id.tvAllCardsCount);
        etSearchCards = findViewById(R.id.etSearchCards);
        btnFilterCards = findViewById(R.id.btnFilterCards);

        rvAllCards.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AllCardsAdapter();
        rvAllCards.setAdapter(adapter);

        flashcardViewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);

        View btnEmptyGoToSubjects = findViewById(R.id.btnEmptyGoToSubjects);
        btnEmptyGoToSubjects.setOnClickListener(v -> {
            startActivity(new Intent(AllCardsActivity.this, SubjectsActivity.class));
            finish();
        });

        flashcardViewModel.getAllFlashcardsWithSubjectName().observe(this, cards -> {
            if (cards != null) {
                allCardsList = cards;
                filterCards(etSearchCards.getText().toString());
            }
        });

        etSearchCards.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCards(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnFilterCards.setOnClickListener(v -> showFilterMenu());
    }

    private void showFilterMenu() {
        PopupMenu popup = new PopupMenu(this, btnFilterCards);
        popup.getMenu().add(0, 0, 0, "All Difficulties").setCheckable(true);
        popup.getMenu().add(0, 1, 1, "Easy").setCheckable(true);
        popup.getMenu().add(0, 2, 2, "Medium").setCheckable(true);
        popup.getMenu().add(0, 3, 3, "Hard").setCheckable(true);
        // We could also do "Needs review" filter eventually 

        // Set checked state
        switch (currentDifficultyFilter) {
            case "EASY": popup.getMenu().getItem(1).setChecked(true); break;
            case "MEDIUM": popup.getMenu().getItem(2).setChecked(true); break;
            case "HARD": popup.getMenu().getItem(3).setChecked(true); break;
            default: popup.getMenu().getItem(0).setChecked(true); break;
        }

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: currentDifficultyFilter = "EASY"; break;
                case 2: currentDifficultyFilter = "MEDIUM"; break;
                case 3: currentDifficultyFilter = "HARD"; break;
                default: currentDifficultyFilter = "ALL"; break;
            }
            // Add a visual indicator if filtered
            if (!"ALL".equals(currentDifficultyFilter)) {
                btnFilterCards.setColorFilter(ContextCompat.getColor(this, R.color.goal_color));
            } else {
                btnFilterCards.clearColorFilter();
            }
            filterCards(etSearchCards.getText().toString());
            return true;
        });

        popup.show();
    }

    private void filterCards(String query) {
        if (allCardsList == null || allCardsList.isEmpty()) {
            rvAllCards.setVisibility(View.GONE);
            llEmptyStateAllCards.setVisibility(View.VISIBLE);
            tvAllCardsCount.setText("0 cards total");
            return;
        }

        List<FlashcardWithSubjectName> filteredList = new ArrayList<>();
        String lowerQuery = query != null ? query.toLowerCase().trim() : "";

        for (FlashcardWithSubjectName card : allCardsList) {
            // Check text
            boolean textMatch = TextUtils.isEmpty(lowerQuery) || 
                                card.question.toLowerCase().contains(lowerQuery) || 
                                card.answer.toLowerCase().contains(lowerQuery);
            
            // Check difficulty
            boolean diffMatch = "ALL".equals(currentDifficultyFilter) || 
                                currentDifficultyFilter.equals(card.difficulty);

            if (textMatch && diffMatch) {
                filteredList.add(card);
            }
        }

        if (filteredList.isEmpty()) {
            rvAllCards.setVisibility(View.GONE);
            llEmptyStateAllCards.setVisibility(View.VISIBLE);
            TextView emptyTitle = findViewById(R.id.tvEmptyTitleAllCards);
            TextView emptyDesc = findViewById(R.id.tvEmptyDescAllCards);
            View btnEmptyGoToSubjects = findViewById(R.id.btnEmptyGoToSubjects);

            if (!TextUtils.isEmpty(lowerQuery) || !"ALL".equals(currentDifficultyFilter)) {
                emptyTitle.setText("No matches found");
                emptyDesc.setText("Try a different search or filter");
                btnEmptyGoToSubjects.setVisibility(View.GONE);
            } else {
                emptyTitle.setText("No flashcards available.");
                emptyDesc.setText("Add flashcards from a subject to start studying.");
                btnEmptyGoToSubjects.setVisibility(View.VISIBLE);
            }
        } else {
            rvAllCards.setVisibility(View.VISIBLE);
            llEmptyStateAllCards.setVisibility(View.GONE);
        }

        adapter.setCards(filteredList);
        tvAllCardsCount.setText(filteredList.size() + " cards" + (!"ALL".equals(currentDifficultyFilter) ? " (filtered)" : " total"));
    }

    private class AllCardsAdapter extends androidx.recyclerview.widget.ListAdapter<FlashcardWithSubjectName, AllCardsAdapter.CardHolder> {

        public AllCardsAdapter() {
            super(new androidx.recyclerview.widget.DiffUtil.ItemCallback<FlashcardWithSubjectName>() {
                @Override
                public boolean areItemsTheSame(@NonNull FlashcardWithSubjectName oldItem, @NonNull FlashcardWithSubjectName newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull FlashcardWithSubjectName oldItem, @NonNull FlashcardWithSubjectName newItem) {
                    return oldItem.question.equals(newItem.question) &&
                           oldItem.answer.equals(newItem.answer) &&
                           oldItem.difficulty.equals(newItem.difficulty) &&
                           (oldItem.comment == null ? newItem.comment == null : oldItem.comment.equals(newItem.comment)) &&
                           (oldItem.subjectName == null ? newItem.subjectName == null : oldItem.subjectName.equals(newItem.subjectName));
                }
            });
        }

        public void setCards(List<FlashcardWithSubjectName> cards) {
            submitList(cards);
        }

        @NonNull
        @Override
        public CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_all_cards, parent, false);
            return new CardHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull CardHolder holder, int position) {
            FlashcardWithSubjectName currentCard = getItem(position);
            holder.tvCardQuestion.setText(currentCard.question);
            holder.tvCardSubjectName.setText(currentCard.subjectName);

            // Setup Difficulty Badge
            holder.tvCardDifficulty.setText(currentCard.difficulty);
            if ("EASY".equals(currentCard.difficulty)) {
                holder.tvCardDifficulty.setTextColor(ContextCompat.getColor(AllCardsActivity.this, R.color.diff_easy));
            } else if ("HARD".equals(currentCard.difficulty)) {
                holder.tvCardDifficulty.setTextColor(ContextCompat.getColor(AllCardsActivity.this, R.color.diff_hard));
            } else {
                holder.tvCardDifficulty.setTextColor(ContextCompat.getColor(AllCardsActivity.this, R.color.diff_medium));
            }

            // Setup Comment Preview
            if (TextUtils.isEmpty(currentCard.comment)) {
                holder.tvCardComment.setVisibility(View.GONE);
            } else {
                holder.tvCardComment.setVisibility(View.VISIBLE);
                holder.tvCardComment.setText("Note: " + currentCard.comment);
            }

            // Background dynamic color
            int bgColor = FlashcardColorUtils.getColorForId(currentCard.id);
            int primaryText = FlashcardColorUtils.getPrimaryTextColor(bgColor);
            int secondaryText = FlashcardColorUtils.getSecondaryTextColor(bgColor);
            ((MaterialCardView) holder.itemView).setCardBackgroundColor(bgColor);
            holder.tvCardQuestion.setTextColor(primaryText);
            holder.tvCardSubjectName.setTextColor(primaryText);

            holder.tvCardTap.setTextColor(secondaryText);
            holder.tvCardComment.setTextColor(secondaryText);

            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if(pos != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(AllCardsActivity.this, FlashcardActivity.class);
                    intent.putExtra("SUBJECT_ID", -1);
                    // Pass the global index by finding it in the original list
                    // Since the current list is filtered, the position is the filtered position.
                    // We need the start index in the FULL list for FlashcardActivity to work correctly
                    // because FlashcardActivity fetches all cards or subject cards directly without search filters!
                    // Oh, actually in FlashcardMode of AllCards, we want to go straight to this specific flashcard,
                    // but the FlashcardActivity's "All Cards" mode (SUBJECT_ID = -1) just pulls all flashcards.
                    // Let's find the original index.
                    int targetId = currentCard.id;
                    int originalIndex = 0;
                    for (int i = 0; i < allCardsList.size(); i++) {
                        if (allCardsList.get(i).id == targetId) {
                            originalIndex = i;
                            break;
                        }
                    }
                    intent.putExtra("START_INDEX", originalIndex);
                    startActivity(intent);
                }
            });
        }

        class CardHolder extends RecyclerView.ViewHolder {
            private TextView tvCardQuestion;
            private TextView tvCardSubjectName;
            private TextView tvCardDifficulty;
            private TextView tvCardComment;
            private TextView tvCardTap;

            public CardHolder(@NonNull View itemView) {
                super(itemView);
                tvCardQuestion = itemView.findViewById(R.id.tvCardQuestion);
                tvCardSubjectName = itemView.findViewById(R.id.tvCardSubjectName);
                tvCardDifficulty = itemView.findViewById(R.id.tvCardDifficulty);
                tvCardComment = itemView.findViewById(R.id.tvCardComment);
                tvCardTap = itemView.findViewById(R.id.tvCardTap);
            }
        }
    }
}

package com.example.flashcard.ui;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.flashcard.R;
import com.example.flashcard.utils.ReminderPreferences;
import com.example.flashcard.utils.ReminderScheduler;
import com.example.flashcard.viewmodel.FlashcardViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 2001;

    private FlashcardViewModel viewModel;
    private MaterialSwitch switchReminder;
    private MaterialCardView cardReminderTime;
    private TextView tvReminderTime;

    private final ActivityResultLauncher<String> createCsvLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/csv"), uri -> {
                if (uri != null) {
                    writeCsvToUri(uri);
                }
            }
    );

    private final ActivityResultLauncher<String> createJsonLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"), uri -> {
                if (uri != null) {
                    writeJsonToUri(uri);
                }
            }
    );

    private final ActivityResultLauncher<String[]> openJsonLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    readJsonFromUriAndRestore(uri);
                }
            }
    );

    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    enableReminder();
                } else {
                    switchReminder.setChecked(false);
                    Toast.makeText(this,
                            "Notification permission is required for study reminders",
                            Toast.LENGTH_LONG).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        com.example.flashcard.utils.FeatureTutorial.showOnce(this, "settings",
                com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_settings_title, R.string.guide_settings_message),
                com.example.flashcard.utils.FeatureTutorial.step(R.string.guide_settings_data_title, R.string.guide_settings_data_message));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cardExportCsv), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            findViewById(android.R.id.content).setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);

        // --- Reminder section ---
        switchReminder = findViewById(R.id.switchReminder);
        cardReminderTime = findViewById(R.id.cardReminderTime);
        tvReminderTime = findViewById(R.id.tvReminderTime);

        boolean reminderOn = ReminderPreferences.isReminderEnabled(this);
        switchReminder.setChecked(reminderOn);
        cardReminderTime.setVisibility(reminderOn ? View.VISIBLE : View.GONE);
        updateReminderTimeLabel();

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Check notification permission on API 33+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                        notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS);
                        return;
                    }
                }
                enableReminder();
            } else {
                disableReminder();
            }
        });

        cardReminderTime.setOnClickListener(v -> showTimePicker());

        // --- Data & Backup section ---
        MaterialCardView cardExportCsv = findViewById(R.id.cardExportCsv);
        MaterialCardView cardBackupJson = findViewById(R.id.cardBackupJson);
        MaterialCardView cardRestoreJson = findViewById(R.id.cardRestoreJson);
        MaterialCardView cardClearAll = findViewById(R.id.cardClearAll);

        cardExportCsv.setOnClickListener(v -> {
            createCsvLauncher.launch("Flashcards_Export.csv");
        });

        cardBackupJson.setOnClickListener(v -> {
            createJsonLauncher.launch("Flashcards_Backup.json");
        });

        cardRestoreJson.setOnClickListener(v -> {
            openJsonLauncher.launch(new String[]{"application/json", "*/*"});
        });

        cardClearAll.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Clear All Data")
                    .setMessage("Are you sure you want to delete all subjects, flashcards, and history? This cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.clearAllData(() -> {
                            Toast.makeText(this, "All data cleared successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, com.example.flashcard.MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    // --- Reminder helpers ---

    private void enableReminder() {
        ReminderPreferences.setReminderEnabled(this, true);
        ReminderScheduler.scheduleDaily(this);
        cardReminderTime.setVisibility(View.VISIBLE);
        updateReminderTimeLabel();
        Toast.makeText(this, "Daily study reminder enabled", Toast.LENGTH_SHORT).show();
    }

    private void disableReminder() {
        ReminderPreferences.setReminderEnabled(this, false);
        ReminderScheduler.cancel(this);
        cardReminderTime.setVisibility(View.GONE);
        Toast.makeText(this, "Daily study reminder disabled", Toast.LENGTH_SHORT).show();
    }

    private void showTimePicker() {
        int hour = ReminderPreferences.getReminderHour(this);
        int minute = ReminderPreferences.getReminderMinute(this);

        new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            ReminderPreferences.setReminderTime(this, selectedHour, selectedMinute);
            updateReminderTimeLabel();

            // Re-schedule with the new time
            if (ReminderPreferences.isReminderEnabled(this)) {
                ReminderScheduler.scheduleDaily(this);
                Toast.makeText(this, "Reminder time updated", Toast.LENGTH_SHORT).show();
            }
        }, hour, minute, false).show();
    }

    private void updateReminderTimeLabel() {
        int hour = ReminderPreferences.getReminderHour(this);
        int minute = ReminderPreferences.getReminderMinute(this);
        String amPm = hour >= 12 ? "PM" : "AM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        tvReminderTime.setText(String.format(Locale.US,
                "Remind me at %d:%02d %s", displayHour, minute, amPm));
    }

    // --- CSV / JSON export-import ---

    private void writeCsvToUri(Uri uri) {
        viewModel.generateCsvBackup(csvData -> {
            if (csvData != null) {
                try {
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    if (os != null) {
                        os.write(csvData.getBytes(StandardCharsets.UTF_8));
                        os.close();
                        Toast.makeText(this, "CSV exported successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to write CSV", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to generate CSV data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void writeJsonToUri(Uri uri) {
        viewModel.generateJsonBackup(jsonData -> {
            if (jsonData != null) {
                try {
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    if (os != null) {
                        os.write(jsonData.getBytes(StandardCharsets.UTF_8));
                        os.close();
                        Toast.makeText(this, "Backup exported successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to write backup", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to generate backup data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readJsonFromUriAndRestore(Uri uri) {
        new Thread(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                if (is != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    String jsonString = sb.toString();
                    viewModel.restoreDatabase(jsonString, () -> {
                        Toast.makeText(SettingsActivity.this, "Database restored successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SettingsActivity.this, com.example.flashcard.MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this, "Failed to read backup file", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}

package com.example.flashcard.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        Flashcard.class,
        Subject.class,
        TestResult.class,
        TestResultItem.class,
        StudyDay.class,
        ExamPlan.class,
        ExamPlanSubject.class,
        ExamPlanCard.class,
        ExamPlanDay.class
}, version = 7, exportSchema = true)
public abstract class FlashcardDatabase extends RoomDatabase {

    public abstract FlashcardDao flashcardDao();
    public abstract SubjectDao subjectDao();
    public abstract TestResultDao testResultDao();
    public abstract StudyDayDao studyDayDao();
    public abstract ExamPlanDao examPlanDao();

    private static volatile FlashcardDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE flashcard_table ADD COLUMN comment TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE flashcard_table ADD COLUMN difficulty TEXT NOT NULL DEFAULT 'MEDIUM'");
            database.execSQL("ALTER TABLE subject_table ADD COLUMN difficulty TEXT NOT NULL DEFAULT 'MEDIUM'");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `test_results` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`subjectId` INTEGER NOT NULL, " +
                    "`subjectName` TEXT, " +
                    "`difficulty` TEXT, " +
                    "`totalCards` INTEGER NOT NULL, " +
                    "`correctAnswers` INTEGER NOT NULL, " +
                    "`incorrectAnswers` INTEGER NOT NULL, " +
                    "`unanswered` INTEGER NOT NULL, " +
                    "`score` INTEGER NOT NULL, " +
                    "`timeLimitMillis` INTEGER NOT NULL, " +
                    "`timeUsedMillis` INTEGER NOT NULL, " +
                    "`completedAt` INTEGER NOT NULL, " +
                    "`wasTimeUp` INTEGER NOT NULL, " +
                    "`isPermanentlySaved` INTEGER NOT NULL)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `test_result_items` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`testResultId` INTEGER NOT NULL, " +
                    "`flashcardId` INTEGER NOT NULL, " +
                    "`question` TEXT, " +
                    "`result` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`testResultId`) REFERENCES `test_results`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_test_result_items_testResultId` ON `test_result_items` (`testResultId`)");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `study_days` (" +
                    "`date` TEXT NOT NULL, " +
                    "`cardsStudied` INTEGER NOT NULL, " +
                    "`testsCompleted` INTEGER NOT NULL, " +
                    "`goalCompleted` INTEGER NOT NULL, " +
                    "`dailyGoalAtTheTime` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`date`))");
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE flashcard_table ADD COLUMN repetition INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE flashcard_table ADD COLUMN interval INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE flashcard_table ADD COLUMN easinessFactor REAL NOT NULL DEFAULT 2.5");
            database.execSQL("ALTER TABLE flashcard_table ADD COLUMN nextReviewDate INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `exam_plans` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT, " +
                    "`startDate` TEXT, " +
                    "`examDate` TEXT, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`archived` INTEGER NOT NULL, " +
                    "`completedAt` INTEGER NOT NULL)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `exam_plan_subjects` (" +
                    "`planId` INTEGER NOT NULL, " +
                    "`subjectId` INTEGER NOT NULL, " +
                    "`subjectNameSnapshot` TEXT NOT NULL, " +
                    "PRIMARY KEY(`planId`, `subjectId`), " +
                    "FOREIGN KEY(`planId`) REFERENCES `exam_plans`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_exam_plan_subjects_planId` ON `exam_plan_subjects` (`planId`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `exam_plan_cards` (" +
                    "`planId` INTEGER NOT NULL, " +
                    "`flashcardId` INTEGER NOT NULL, " +
                    "`questionSnapshot` TEXT NOT NULL, " +
                    "`subjectNameSnapshot` TEXT NOT NULL, " +
                    "`covered` INTEGER NOT NULL, " +
                    "`firstCoveredAt` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`planId`, `flashcardId`), " +
                    "FOREIGN KEY(`planId`) REFERENCES `exam_plans`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_exam_plan_cards_planId` ON `exam_plan_cards` (`planId`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `exam_plan_days` (" +
                    "`planId` INTEGER NOT NULL, " +
                    "`date` TEXT NOT NULL, " +
                    "`recommendedTargetAtTheTime` INTEGER NOT NULL, " +
                    "`uniqueCardsCoveredThatDay` INTEGER NOT NULL, " +
                    "`reviewsCompletedThatDay` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`planId`, `date`), " +
                    "FOREIGN KEY(`planId`) REFERENCES `exam_plans`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_exam_plan_days_planId` ON `exam_plan_days` (`planId`)");
        }
    };

    // To gracefully upgrade from version 1 to 2 if any users have version 1
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Version 2 didn't add schema changes in this version, it's just what it was previously set to.
            // But we should define it just in case fallback isn't needed.
            // In the previous codebase it had version 2 with exportSchema false.
        }
    };

    public static FlashcardDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FlashcardDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FlashcardDatabase.class, "flashcard_database")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                SubjectDao sDao = INSTANCE.subjectDao();
                FlashcardDao fDao = INSTANCE.flashcardDao();

                Subject defaultSubject = new Subject("General", "MEDIUM");
                sDao.insert(defaultSubject);
            });
        }
    };
}
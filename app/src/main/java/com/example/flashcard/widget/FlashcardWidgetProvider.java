package com.example.flashcard.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.example.flashcard.AllCardsActivity;
import com.example.flashcard.FlashcardActivity;
import com.example.flashcard.MainActivity;
import com.example.flashcard.R;
import com.example.flashcard.data.ExamPlan;
import com.example.flashcard.data.ExamPlanDao;
import com.example.flashcard.data.FlashcardDatabase;
import com.example.flashcard.ui.ExamPlansActivity;
import com.example.flashcard.ui.PlanDetailsActivity;
import com.example.flashcard.utils.ExamPlanUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlashcardWidgetProvider extends AppWidgetProvider {
    private static final ExecutorService WIDGET_EXECUTOR = Executors.newSingleThreadExecutor();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Context appContext = context.getApplicationContext();
        WIDGET_EXECUTOR.execute(() -> updateWidgets(appContext, appWidgetManager, appWidgetIds));
    }

    public static void requestUpdate(Context context) {
        Context appContext = context.getApplicationContext();
        AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
        int[] widgetIds = manager.getAppWidgetIds(
                new ComponentName(appContext, FlashcardWidgetProvider.class));
        if (widgetIds.length > 0) {
            WIDGET_EXECUTOR.execute(() -> updateWidgets(appContext, manager, widgetIds));
        }
    }

    private static void updateWidgets(Context context, AppWidgetManager manager, int[] widgetIds) {
        int dueCount = 0;
        ExamPlan plan = null;
        int planTotal = 0;
        int planCovered = 0;

        try {
            FlashcardDatabase database = FlashcardDatabase.getDatabase(context);
            dueCount = database.flashcardDao().getCardsDueCountSync(System.currentTimeMillis());
            ExamPlanDao planDao = database.examPlanDao();
            plan = planDao.getNearestActivePlanSync();
            if (plan != null) {
                planTotal = planDao.getCardCountForPlanSync(plan.id);
                planCovered = planDao.getCoveredCardCountForPlanSync(plan.id);
            }
        } catch (RuntimeException ignored) {
            // Keep the widget usable with empty values if local data cannot be read.
        }

        for (int widgetId : widgetIds) {
            RemoteViews views = buildViews(context, widgetId, dueCount, plan, planTotal, planCovered);
            manager.updateAppWidget(widgetId, views);
        }
    }

    private static RemoteViews buildViews(Context context, int widgetId, int dueCount,
                                          ExamPlan plan, int planTotal, int planCovered) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_flashcard);
        views.setTextViewText(R.id.tvWidgetDueCount,
                context.getString(R.string.widget_due_count, dueCount));

        PendingIntent openApp = activityIntent(context, widgetId,
                new Intent(context, MainActivity.class));
        views.setOnClickPendingIntent(R.id.widgetRoot, openApp);

        Intent dueIntent = new Intent(context, FlashcardActivity.class)
                .putExtra("DUE_TODAY_MODE", true);
        PendingIntent openDue = activityIntent(context, widgetId + 10_000, dueIntent);
        views.setOnClickPendingIntent(R.id.widgetDueSection, openDue);

        PendingIntent studyIntent;
        if (plan != null) {
            views.setTextViewText(R.id.tvWidgetPlanName, plan.name);
            views.setTextViewText(R.id.tvWidgetPlanDate, examDateLabel(context, plan.examDate));
            views.setTextViewText(R.id.tvWidgetPlanProgress,
                    context.getString(R.string.widget_plan_progress, planCovered, planTotal));
            views.setViewVisibility(R.id.tvWidgetPlanProgress, View.VISIBLE);

            Intent planIntent = new Intent(context, PlanDetailsActivity.class)
                    .putExtra(PlanDetailsActivity.EXTRA_PLAN_ID, plan.id);
            PendingIntent openPlan = activityIntent(context, widgetId + 20_000, planIntent);
            views.setOnClickPendingIntent(R.id.widgetPlanSection, openPlan);
            Intent studyPlanIntent = new Intent(context, FlashcardActivity.class)
                    .putExtra(PlanDetailsActivity.EXTRA_PLAN_ID, plan.id);
            studyIntent = activityIntent(context, widgetId + 30_000, studyPlanIntent);
        } else {
            views.setTextViewText(R.id.tvWidgetPlanName, context.getString(R.string.widget_no_plan));
            views.setTextViewText(R.id.tvWidgetPlanDate, context.getString(R.string.widget_create_plan));
            views.setViewVisibility(R.id.tvWidgetPlanProgress, View.GONE);
            PendingIntent openPlans = activityIntent(context, widgetId + 20_000,
                    new Intent(context, ExamPlansActivity.class));
            views.setOnClickPendingIntent(R.id.widgetPlanSection, openPlans);
            studyIntent = dueCount > 0
                    ? openDue
                    : activityIntent(context, widgetId + 30_000,
                            new Intent(context, AllCardsActivity.class));
        }

        views.setOnClickPendingIntent(R.id.btnWidgetStudyNow, studyIntent);
        views.setTextViewText(R.id.btnWidgetStudyNow,
                context.getString(dueCount == 0 && plan == null
                        ? R.string.widget_open_app
                        : R.string.widget_study_now));
        return views;
    }

    private static String examDateLabel(Context context, String examDate) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        if (examDate == null || examDate.compareTo(today) < 0) {
            return context.getString(R.string.widget_exam_overdue);
        }
        if (examDate.equals(today)) {
            return context.getString(R.string.widget_exam_today);
        }
        int daysRemaining = Math.max(0,
                ExamPlanUtils.getDaysBetweenInclusive(today, examDate) - 1);
        return context.getString(R.string.widget_days_remaining, daysRemaining);
    }

    private static PendingIntent activityIntent(Context context, int requestCode, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}

package sdmd.dailyprogramsreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class DataAccess {
    private static FeedReaderDbHelper mDbHelper;
    private static String[] projection;

    DataAccess(Context context){
        mDbHelper = new FeedReaderDbHelper(context.getApplicationContext());

        projection = new String[]{
                FeedReaderContract.FeedEntry._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DUE_DATE,
                FeedReaderContract.FeedEntry.COLUMN_NAME_ALARM_DATE,
                FeedReaderContract.FeedEntry.COLUMN_NAME_REPEAT,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DESCRIPTION,
                FeedReaderContract.FeedEntry.COLUMN_NAME_NEXT_ITERATION_CREATED,
                FeedReaderContract.FeedEntry.COLUMN_NAME_IS_COMPLETED
        };
    }

    public static void insertPlan(Plan plan) throws Exception {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, plan.getTitle());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DUE_DATE, plan.getDueDate().getTimeInMillis());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_ALARM_DATE, plan.getAlarmDate().getTimeInMillis());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_REPEAT,
                FrequencyHandler.frequencyToString(plan.getRepeat()));
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DESCRIPTION, plan.getDescription());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_NEXT_ITERATION_CREATED, plan.isNextIterationCreated()? 1 : 0);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_IS_COMPLETED, plan.isCompleted()? 1 : 0);

        long rowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);

        plan.setId(rowId);
    }

    public static void updatePlan(Plan plan) throws Exception {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, plan.getTitle());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DUE_DATE, plan.getDueDate().getTimeInMillis());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_ALARM_DATE, plan.getAlarmDate().getTimeInMillis());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_REPEAT,
                FrequencyHandler.frequencyToString(plan.getRepeat()));
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DESCRIPTION, plan.getDescription());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_NEXT_ITERATION_CREATED, plan.isNextIterationCreated()? 1 : 0);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_IS_COMPLETED, plan.isCompleted()? 1 : 0);

        String selection = FeedReaderContract.FeedEntry._ID + " = ?";
        String[] selectionArgs = { Long.toString(plan.getId()) };

        db.update(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public static void updatePlan(long planId, boolean isCompleted){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_IS_COMPLETED, isCompleted? 1 : 0);

        if(isCompleted){
            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_NEXT_ITERATION_CREATED, 1);
        }

        String selection = FeedReaderContract.FeedEntry._ID + " = ?";
        String[] selectionArgs = { Long.toString(planId) };

        db.update(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public static void deletePlan(long id){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String selection = FeedReaderContract.FeedEntry._ID + " = ?";

        String[] selectionArgs = { Long.toString(id) };

        db.delete(FeedReaderContract.FeedEntry.TABLE_NAME, selection, selectionArgs);
    }

    private static ArrayList<Plan> select(boolean isDated, boolean isUndone, Calendar from, Calendar to){
        ArrayList<Plan> _result = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String selection;
        ArrayList<String> selectionArgs = new ArrayList<>();

        String dateSign = " = ";
        String dateArg = "0";
        if(isDated){
            dateSign = " <> ";
        }

        if(from != null){
            from.set(Calendar.HOUR_OF_DAY, from.getMinimum(Calendar.HOUR_OF_DAY));
            from.set(Calendar.MINUTE, from.getMinimum(Calendar.MINUTE));
            from.set(Calendar.SECOND, from.getMinimum(Calendar.SECOND));
            from.set(Calendar.MILLISECOND, from.getMinimum(Calendar.MILLISECOND));

            dateSign = " > ";
            dateArg = String.valueOf(from.getTimeInMillis());
        }

        selection = FeedReaderContract.FeedEntry.COLUMN_NAME_DUE_DATE + dateSign + "?";
        selectionArgs.add(dateArg);

        if(to != null){
            to.set(Calendar.HOUR_OF_DAY, to.getMaximum(Calendar.HOUR_OF_DAY));
            to.set(Calendar.MINUTE, to.getMaximum(Calendar.MINUTE));
            to.set(Calendar.SECOND, to.getMaximum(Calendar.SECOND));
            to.set(Calendar.MILLISECOND, to.getMaximum(Calendar.MILLISECOND));

            dateSign = " < ";
            dateArg = String.valueOf(to.getTimeInMillis());

            selection += " AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_DUE_DATE + dateSign + "?";
            selectionArgs.add(dateArg);
        }

        if(isUndone){
            selection += " AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_IS_COMPLETED + " = ?";
            selectionArgs.add("0");
        }

        String sortOrder =
                FeedReaderContract.FeedEntry._ID + " DESC";

        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs.toArray(new String[selectionArgs.size()]),
                null,
                null,
                sortOrder
        );

        while(cursor.moveToNext()) {
            try {
                _result.add(new Plan(cursor));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();

        return _result;
    }

    public static ArrayList<Plan> selectPlans(boolean isDated, boolean isUndone){
        return select(isDated, isUndone, null, null);
    }

    public static ArrayList<Plan> selectPlans(boolean isDated, boolean isUndone, Calendar from, Calendar to){
        return select(isDated, isUndone, from, to);
    }

    public static ArrayList<Plan> selectTodayPlans(boolean isUndone){
        Calendar _today = Calendar.getInstance();
        return select(true, isUndone, _today, _today);
    }
}

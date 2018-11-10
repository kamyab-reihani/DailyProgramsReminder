package sdmd.dailyprogramsreminder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FeedReaderDbHelper extends SQLiteOpenHelper {
    private static final String CREATE_PLANS_TABLE_QUERY = "CREATE TABLE " +
            FeedReaderContract.FeedEntry.TABLE_NAME + " (" + FeedReaderContract.FeedEntry._ID +
            " INTEGER PRIMARY KEY, " + FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " TEXT, " +
            FeedReaderContract.FeedEntry.COLUMN_NAME_DUE_DATE + " TIMESTAMP, " +
            FeedReaderContract.FeedEntry.COLUMN_NAME_ALARM_DATE + " TIMESTAMP, " +
            FeedReaderContract.FeedEntry.COLUMN_NAME_REPEAT + " TEXT, " +
            FeedReaderContract.FeedEntry.COLUMN_NAME_DESCRIPTION + " TEXT, " +
            FeedReaderContract.FeedEntry.COLUMN_NAME_NEXT_ITERATION_CREATED + " SHORT, " +
            FeedReaderContract.FeedEntry.COLUMN_NAME_IS_COMPLETED + " SHORT)";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "DPR.db";

    FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_PLANS_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //This DB will not be upgraded
    }
}

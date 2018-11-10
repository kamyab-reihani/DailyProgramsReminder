package sdmd.dailyprogramsreminder;

import android.provider.BaseColumns;

public final class FeedReaderContract  {
    private FeedReaderContract(){}

    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "plans";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DUE_DATE = "dueDate";
        public static final String COLUMN_NAME_ALARM_DATE = "alarmDate";
        public static final String COLUMN_NAME_REPEAT = "repeat";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_NEXT_ITERATION_CREATED = "nextIterationCreated";
        public static final String COLUMN_NAME_IS_COMPLETED = "isCompleted";
    }
}

package sdmd.dailyprogramsreminder;

import java.util.Calendar;

public class SelectDetails {
    public final boolean isDated;
    public final boolean isUndone;
    public final Calendar fromDate;
    public final Calendar toDate;

    public final boolean hasDateRange;

    SelectDetails(boolean aIsDated, boolean aIsUndone){
        isDated = aIsDated;
        isUndone = aIsUndone;
        fromDate = Calendar.getInstance();
        toDate = Calendar.getInstance();
        hasDateRange = false;
    }

    SelectDetails(boolean aIsDated, boolean aIsUndone, Calendar aFromDate, Calendar aToDate){
        isDated = aIsDated;
        isUndone = aIsUndone;
        fromDate = aFromDate;
        toDate = aToDate;
        hasDateRange = true;
    }
}

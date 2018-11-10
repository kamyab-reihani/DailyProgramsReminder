package sdmd.dailyprogramsreminder;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Plan implements Parcelable{
    private long id;
    private String title;
    private Calendar dueDate;
    private Calendar alarmDate;
    private Frequency repeat;
    private String description;
    private boolean isCompleted;
    private boolean nextIterationCreated;
    private boolean hasDate;

    Plan(){
        id = -1;

        title = "";
        description = "";

        dueDate = NULL_DATE();
        alarmDate = NULL_DATE();
        repeat = Frequency.NEVER;
        isCompleted = false;
        nextIterationCreated = false;
        hasDate = false;
    }

    @Deprecated
    public Plan(String aTitle, Calendar aDueDate, Calendar aAlarmDate, Frequency aRepeat,
                String aDescription) throws Exception {
        if(aAlarmDate.after(aDueDate)){
            throw new Exception("Alarm Date cannot be after Due Date!");
        }

        id = -1;

        title = aTitle;
        dueDate = aDueDate;
        alarmDate = aAlarmDate;
        repeat = aRepeat;
        description = aDescription;

        isCompleted = false;
        nextIterationCreated = false;
        hasDate = true;
    }

    @Deprecated
    public Plan(String aTitle, String aDescription){
        id = -1;

        title = aTitle;
        description = aDescription;

        dueDate = NULL_DATE();
        alarmDate = NULL_DATE();
        repeat = Frequency.NEVER;
        isCompleted = false;
        nextIterationCreated = false;
        hasDate = false;
    }

    Plan(Plan aPlan){
        id = aPlan.getId();

        title = aPlan.getTitle();
        dueDate = (Calendar) aPlan.getDueDate().clone();
        alarmDate = (Calendar) aPlan.getAlarmDate().clone();
        repeat = aPlan.getRepeat();
        description = aPlan.getDescription();

        isCompleted = aPlan.isCompleted();
        nextIterationCreated = aPlan.isNextIterationCreated();
        hasDate = aPlan.hasDate();
    }

    Plan(final Cursor cursor) throws Exception {
        id = cursor.getLong(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry._ID));
        title = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE));
        dueDate = Calendar.getInstance();
        dueDate.setTimeInMillis(
                cursor.getLong(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_DUE_DATE)));
        alarmDate = Calendar.getInstance();
        alarmDate.setTimeInMillis(
                cursor.getLong(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_ALARM_DATE)));
        repeat = FrequencyHandler.stringToFrequency(
                cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_REPEAT))
        );
        description = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_DESCRIPTION));
        nextIterationCreated = cursor.getShort(
                cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_NEXT_ITERATION_CREATED)) == 1;
        isCompleted = cursor.getShort(
                cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_IS_COMPLETED)) == 1;

        hasDate = getDueDate().getTimeInMillis() != 0 || getAlarmDate().getTimeInMillis() != 0;
    }

    private Plan(Parcel in) {
        id = in.readLong();
        title = in.readString();
        dueDate = (Calendar) in.readSerializable();
        alarmDate = (Calendar) in.readSerializable();
        repeat = (Frequency) in.readSerializable();
        description = in.readString();
        isCompleted = in.readInt() == 1;
        nextIterationCreated = in.readInt() == 1;
        hasDate = in.readInt() == 1;
    }

    public void insertNextIteration(){
        if(!nextIterationCreated && hasDate() && getRepeat() != Frequency.NULL && getRepeat() != Frequency.NEVER){
            try {
                Plan _nextIteration = new Plan(this);
                _nextIteration.setDueDate(FrequencyHandler.getNextDate(dueDate, repeat));
                _nextIteration.setAlarmDate(FrequencyHandler.getNextDate(alarmDate, repeat));
                _nextIteration.notComplete();
                DataAccess.insertPlan(_nextIteration);

                nextIterationCreated = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String toString(){
        StringBuilder _result = new StringBuilder()
                .append(getTitle());

        if(hasDate()) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd - MM - yyyy' at 'hh:mm");
            _result.append(" (due on ")
                    .append(sdf.format(getDueDate().getTime()))
                    .append(")");
        }

        if(!getDescription().isEmpty()){
            _result.append(": ")
                    .append(getDescription());
        }

        _result.append("\n");

        return _result.toString();
    }

    public void setId(long value){
        id = value;
    }

    public long getId() {
        return id;
    }

    public void complete(){
        isCompleted = true;
    }

    public void notComplete(){
        isCompleted = false;
    }

    public void setAlarmDate(Calendar value){
        alarmDate = value;
    }

    public Calendar getAlarmDate() {
        return alarmDate;
    }

    public void setDueDate(Calendar value){
        dueDate = value;
    }

    public Calendar getDueDate() {
        return dueDate;
    }

    public void setRepeat(Frequency value){
        repeat = value;
    }

    public Frequency getRepeat() {
        return repeat;
    }

    public void setDescription(String value){
        description = value;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String value){
        title = value;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted(){
        return isCompleted;
    }

    public boolean hasDate(){
        return hasDate;
    }

    public void setHasDate(boolean value){
        hasDate = value;
    }

    public boolean isNextIterationCreated(){
        return nextIterationCreated;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(title);
        parcel.writeSerializable(dueDate);
        parcel.writeSerializable(alarmDate);
        parcel.writeSerializable(repeat);
        parcel.writeString(description);
        parcel.writeInt(isCompleted? 1 : 0);
        parcel.writeInt(nextIterationCreated? 1 : 0);
        parcel.writeInt(hasDate? 1 : 0);
    }

    public static final Creator<Plan> CREATOR = new Creator<Plan>() {
        @Override
        public Plan createFromParcel(Parcel in) {
            return new Plan(in);
        }

        @Override
        public Plan[] newArray(int size) {
            return new Plan[size];
        }
    };

    public static Calendar NULL_DATE(){
        Calendar _c = Calendar.getInstance();
        _c.setTimeInMillis(0);

        return _c;
    }
}

package sdmd.dailyprogramsreminder;

import java.util.Calendar;

public class FrequencyHandler {
    public static Calendar getNextDate(Calendar currentDate, Frequency frequency) throws Exception {
        Calendar _result = (Calendar) currentDate.clone();

        switch (frequency){
            case NULL:
            case NEVER:
                break;
            case DAILY:
                _result.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case WEEKLY:
                _result.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case FORTNIGHTLY:
                _result.add(Calendar.WEEK_OF_YEAR, 2);
                break;
            case MONTHLY:
                _result.add(Calendar.MONTH, 1);
                break;
            case YEARLY:
                _result.add(Calendar.YEAR, 2);
                break;
            default:
                throw new Exception("Frequency is not supported!");
        }

        return _result;
    }

    public static Frequency stringToFrequency(String frequency) throws Exception {
        switch (frequency.toLowerCase()){
            case "never":
                return Frequency.NEVER;
            case "daily":
                return Frequency.DAILY;
            case "weekly":
                return Frequency.WEEKLY;
            case "fortnightly":
                return Frequency.FORTNIGHTLY;
            case "monthly":
                return Frequency.MONTHLY;
            case "yearly":
                return Frequency.YEARLY;
            default:
                throw new Exception("Unable to parse String to Frequency!");
        }
    }

    public static String frequencyToString(Frequency frequency) throws Exception {
        switch (frequency){
            case NEVER:
                return "Never";
            case DAILY:
                return "Daily";
            case WEEKLY:
                return "Weekly";
            case FORTNIGHTLY:
                return "Fortnightly";
            case MONTHLY:
                return "Monthly";
            case YEARLY:
                return "Yearly";
            default:
                throw new Exception("Unable to parse Frequency to String!");
        }
    }

    public static Frequency indexToFrequency(int index) throws Exception {
        switch (index){
            case 0:
                return Frequency.NEVER;
            case 1:
                return Frequency.DAILY;
            case 2:
                return Frequency.WEEKLY;
            case 3:
                return Frequency.FORTNIGHTLY;
            case 4:
                return Frequency.MONTHLY;
            case 5:
                return Frequency.YEARLY;
            default:
                throw new Exception("Unable to parse String to Frequency!");
        }
    }

    public static int frequencyIndex(Frequency frequency) throws Exception {
        switch (frequency){
            case NULL:
                return -1;
            case NEVER:
                return 0;
            case DAILY:
                return 1;
            case WEEKLY:
                return 2;
            case FORTNIGHTLY:
                return 3;
            case MONTHLY:
                return 4;
            case YEARLY:
                return 5;
            default:
                throw new Exception("Unable to parse Frequency to String!");
        }
    }
}

package sdmd.dailyprogramsreminder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Calendar;
import java.util.Objects;

public class Receiver extends BroadcastReceiver {
    private static final String PLAN = "plan";
    private static final String CHANNEL_ID = "DailyProgramsReminder";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Plan _plan = (Plan) Objects.requireNonNull(
                Objects.requireNonNull(intent.getExtras()).getParcelableArrayList(PLAN)).get(0);

        String contentText = _plan.getTitle() + " will be due on " + _plan.getDueDate().get(Calendar.DAY_OF_MONTH) +
                " - " + (_plan.getDueDate().get(Calendar.MONTH) + 1) + " - " + _plan.getDueDate().get(Calendar.YEAR) +
                " at " + _plan.getDueDate().get(Calendar.HOUR_OF_DAY) + ":" + _plan.getDueDate().get(Calendar.MINUTE);

        // Create an explicit intent for an Activity in your app
        Intent notifyIntent = new Intent(context, PlanDetailsActivity.class);
        notifyIntent.putExtras(intent.getExtras());

        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(notifyIntent);

        // Get the PendingIntent containing the entire back stack
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("DPR Plan Due Alarm")
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        if (notificationManager != null) {
            notificationManager.notify((int)_plan.getId(), mBuilder.build());
        }
    }
}

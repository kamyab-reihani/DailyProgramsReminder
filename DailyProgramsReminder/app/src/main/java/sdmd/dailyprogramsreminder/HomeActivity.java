package sdmd.dailyprogramsreminder;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements PlansListFragment.OnFragmentInteractionListener {
    private static final int RESULT_DELETED = 5;
    private static final String PLANS = "plans";
    private static final String PLAN = "plan";
    private static final String IS_DATED = "is_dated";

    private static final String CHANNEL_ID = "DailyProgramsReminder";

    private ArrayList<Plan> todayPlans;
    private ArrayList<Plan> unDatedPlans;

    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();

        //initialise the DataAccess before everything
        new DataAccess(getApplicationContext());

        setContentView(R.layout.activity_home);

        initialiseUI();
    }

    @Override
    protected void onRestart() {
        new selectPlans().execute();

        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()){
            case R.id.sharePlans:
                sendEmail();
                return true;
            case R.id.addPlan:
                i = new Intent(this, PlanDetailsActivity.class);
                startActivityForResult(i, 0);
                return true;
            case R.id.viewPlans:
                i = new Intent(this, ViewPlansActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initialiseUI(){
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        todayPlans = new ArrayList<>();
        unDatedPlans = new ArrayList<>();

        new selectPlans().execute();
        setupViewPager();
    }

    private void setupViewPager(){
        final TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Today's Plans"));
        tabLayout.addTab(tabLayout.newTab().setText("Undated Plans"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mPager = findViewById(R.id.homePager);
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Create a tab listener that is called when the user changes tabs.
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onFragmentInteraction(Plan aPlan) {
        Intent i = new Intent(this, PlanDetailsActivity.class);
        Bundle data = new Bundle();
        ArrayList<Plan> _plans = new ArrayList<>();
        _plans.add(aPlan);
        data.putParcelableArrayList(PLAN, _plans);
        i.putExtras(data);
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0){
            if(resultCode == RESULT_OK){
                if(data != null){
                    Plan _plan = (Plan) Objects.requireNonNull(
                            Objects.requireNonNull(data.getExtras()).getParcelableArrayList(PLAN)).get(0);
                    try {
                        DataAccess.insertPlan(_plan);
                        Snackbar snack = Snackbar.make(findViewById(R.id.homeDisplay), "The new plan has been added!",
                                Snackbar.LENGTH_SHORT);
                        snack.show();

                        if(_plan.hasDate())
                            setNotification(_plan);

                        new selectPlans().execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else if(requestCode == 1){
            if(resultCode == RESULT_OK){
                if(data != null){
                    Plan _plan = (Plan) Objects.requireNonNull(
                            Objects.requireNonNull(data.getExtras()).getParcelableArrayList(PLAN)).get(0);
                    try {
                        DataAccess.updatePlan(_plan);
                        Snackbar snack = Snackbar.make(findViewById(R.id.homeDisplay), "The plan has been updated!",
                                Snackbar.LENGTH_SHORT);
                        snack.show();

                        if(_plan.hasDate())
                            setNotification(_plan);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else if(resultCode == RESULT_DELETED){
                Snackbar snack = Snackbar.make(findViewById(R.id.homeDisplay), "The plan has been removed!",
                        Snackbar.LENGTH_SHORT);
                snack.show();
            }
        }

        new selectPlans().execute();
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        int tabCount;

        ScreenSlidePagerAdapter(FragmentManager fm, int aTabCount) {
            super(fm);
            tabCount = aTabCount;
        }

        @Override
        public Fragment getItem(int position) {
            PlansListFragment fragment = new PlansListFragment();
            Bundle data = new Bundle();
            switch (position){
                case 0:
                    data.putBoolean(IS_DATED, true);
                    data.putParcelableArrayList(PLANS, todayPlans);
                    fragment.setArguments(data);
                    return fragment;
                case 1:
                    data.putBoolean(IS_DATED, false);
                    data.putParcelableArrayList(PLANS, unDatedPlans);
                    fragment.setArguments(data);
                    return fragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return tabCount;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }

    private void setNotification(Plan aPlan){
        Intent notifyIntent = new Intent(getApplicationContext(), Receiver.class);
        Bundle data = new Bundle();
        ArrayList<Plan> _plans = new ArrayList<>();
        _plans.add(aPlan);
        data.putParcelableArrayList(PLAN, _plans);
        notifyIntent.putExtras(data);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (this, 1, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(aPlan.getAlarmDate().getTimeInMillis());
            alarmTime.set(Calendar.SECOND, 0);
            alarmTime.set(Calendar.MILLISECOND, 0);

            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class selectPlans extends AsyncTask<Void, Void, ArrayList<ArrayList<Plan>>> {

        @Override
        protected ArrayList<ArrayList<Plan>> doInBackground(Void... voids) {
            ArrayList<ArrayList<Plan>> _result = new ArrayList<>();

            _result.add(DataAccess.selectTodayPlans(true));
            _result.add(DataAccess.selectPlans(false, true));

            return _result;
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<Plan>> plans) {
            todayPlans = plans.get(0);
            unDatedPlans = plans.get(1);

            //reload view pager
            Objects.requireNonNull(mPager.getAdapter()).notifyDataSetChanged();
        }
    }

    private void sendEmail() {
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = Objects.requireNonNull(cm).getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(!isConnected){
            Toast toast = Toast.makeText(getApplicationContext(),
                    "There is no network connection!", Toast.LENGTH_SHORT);
            View toastView = toast.getView();
            toastView.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            toast.show();

            return;
        }

        Calendar _today = Calendar.getInstance();
        String _date = _today.get(Calendar.DAY_OF_MONTH) + "-" + _today.get(Calendar.MONTH) + "-" +
                _today.get(Calendar.YEAR);

        StringBuilder _content = new StringBuilder("Dated Plans: \n");
        for(Plan p : todayPlans){
            _content.append(p.toString());
        }
        _content.append("\n\nUndated Plans: \n");
        for(Plan p : unDatedPlans){
            _content.append(p.toString());
        }

        Uri uri = Uri.parse("mailto:")
                .buildUpon()
                .appendQueryParameter("subject", "DPR Plans Summary Dated " + _date)
                .appendQueryParameter("body", _content.toString())
                .build();

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "There is no email client installed!", Toast.LENGTH_SHORT);
            View toastView = toast.getView();
            toastView.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            toast.show();
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}

package sdmd.dailyprogramsreminder;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class ViewPlansActivity extends AppCompatActivity implements
        PlansListFragment.OnFragmentInteractionListener, FilterFragment.OnFragmentInteractionListener {
    private static final int RESULT_DELETED = 5;
    private static final String PLANS = "plans";
    private static final String PLAN = "plan";
    private static final String IS_DATED = "is_dated";

    private ViewPager mPager;

    private ArrayList<Plan> datedPlans;
    private ArrayList<Plan> unDatedPlans;

    android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_plans);
        initialiseUI();
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

        //remove the view button from the action bar since it is already in the view activity
        MenuItem item = menu.findItem(R.id.viewPlans);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        item.setVisible(false);

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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initialiseUI(){
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        Toolbar toolbar = findViewById(R.id.toolbar_viewPlans);
        setSupportActionBar(toolbar);

        datedPlans = new ArrayList<>();
        unDatedPlans = new ArrayList<>();

        final Switch showUndoneSwitch = findViewById(R.id.showUndoneSwitch);
        showUndoneSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                new selectPlans().execute(new SelectDetails(true, b),
                        new SelectDetails(false, b));
            }
        });

        Button filtersBtn = findViewById(R.id.filtersBtn);
        filtersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout filterFragment = findViewById(R.id.filter_fragment);
                if(filterFragment.getVisibility() == View.VISIBLE)
                    filterFragment.setVisibility(View.GONE);
                else if(filterFragment.getVisibility() == View.GONE)
                    filterFragment.setVisibility(View.VISIBLE);
            }
        });

        Button clearBtn = findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new selectPlans().execute(new SelectDetails(true,false),
                        new SelectDetails(false,false));

                showUndoneSwitch.setChecked(false);

                TextView dateRangeText = findViewById(R.id.dateRangeText);
                dateRangeText.setText("");
            }
        });

        FilterFragment filterFragment = new FilterFragment();
        fm.beginTransaction().add(R.id.filter_fragment, filterFragment).commit();

        new selectPlans().execute(new SelectDetails(true,false),
                new SelectDetails(false,false));

        setupViewPager();
    }

    private void setupViewPager(){
        final TabLayout tabLayout = findViewById(R.id.tabLayout_viewPlans);
        tabLayout.addTab(tabLayout.newTab().setText("Dated Plans"));
        tabLayout.addTab(tabLayout.newTab().setText("Undated Plans"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mPager = findViewById(R.id.viewPlansPager);
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
                        Snackbar snack = Snackbar.make(findViewById(R.id.viewPlansDisplay), "The new plan has been added!",
                                Snackbar.LENGTH_SHORT);
                        snack.show();

                        if(_plan.hasDate())
                            setNotification(_plan);
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
                        Snackbar snack = Snackbar.make(findViewById(R.id.viewPlansDisplay), "The plan has been updated!",
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
                Snackbar snack = Snackbar.make(findViewById(R.id.viewPlansDisplay), "The plan has been removed!",
                        Snackbar.LENGTH_SHORT);
                snack.show();
            }
        }

        Button clearBtn = findViewById(R.id.clearBtn);
        clearBtn.performClick();
    }

    @Override
    public void onFragmentInteraction(Calendar fromDate, Calendar toDate) {
        Switch showUndoneSwitch = findViewById(R.id.showUndoneSwitch);

        new selectPlans().execute(new SelectDetails(true,showUndoneSwitch.isChecked(),
                fromDate, toDate));

        FrameLayout filterFragment = findViewById(R.id.filter_fragment);
        filterFragment.setVisibility(View.GONE);

        TextView dateRangeText = findViewById(R.id.dateRangeText);
        String _text = "FROM: " + fromDate.get(Calendar.DAY_OF_MONTH) + " - " +
                (fromDate.get(Calendar.MONTH) + 1) + " - " + fromDate.get(Calendar.YEAR) +
                "    TO: " + toDate.get(Calendar.DAY_OF_MONTH) + " - " +
                (toDate.get(Calendar.MONTH) + 1) + " - " + toDate.get(Calendar.YEAR);
        dateRangeText.setText(_text);
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
                    data.putParcelableArrayList(PLANS, datedPlans);
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
        Intent notifyIntent = new Intent(this, Receiver.class);
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

    /**
     * More than one SelectDetails can be passed to this thread and be processed.
     * The first SelectDetails must be for Dated Plans and the second must be for Undated Plans.
     */
    @SuppressLint("StaticFieldLeak")
    private class selectPlans extends AsyncTask<SelectDetails, Void,
            ArrayList<ArrayList<Plan>>> {

        @Override
        protected ArrayList<ArrayList<Plan>> doInBackground
                (SelectDetails... selectDetails) {
            ArrayList<ArrayList<Plan>> _result = new ArrayList<>();
            for(SelectDetails sd : selectDetails) {
                if (sd.hasDateRange) {
                    _result.add(DataAccess.selectPlans(sd.isDated, sd.isUndone,
                            sd.fromDate, sd.toDate));
                } else {
                    _result.add(DataAccess.selectPlans(sd.isDated, sd.isUndone));
                }
            }

            return _result;
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<Plan>> plans) {
            if(plans.size() > 0){
                datedPlans = plans.get(0);
            }

            if(plans.size() > 1){
                unDatedPlans = plans.get(1);
            }

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
        for(Plan p : datedPlans){
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
}

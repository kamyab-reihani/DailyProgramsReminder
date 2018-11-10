package sdmd.dailyprogramsreminder;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class PlanDetailsActivity extends AppCompatActivity implements DateTimePickerFragment.OnFragmentInteractionListener{
    private static final int RESULT_DELETED = 5;
    private static final String PLAN = "plan";
    private static final String DATE_TIME = "date_time";

    boolean inEditMode = false;
    Plan plan;

    boolean dueDateBtnClicked = false;
    boolean alarmDateBtnClicked = false;

    android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_details);
        initialiseUI();
    }

    @SuppressLint("SetTextI18n")
    private void initialiseUI(){
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        Toolbar toolbar = findViewById(R.id.toolbar_planDetails);
        setSupportActionBar(toolbar);

        TextView screenHeader = findViewById(R.id.screenHeader);

        Intent intent = getIntent();
        if(intent.getExtras() != null){
            plan = new Plan((Plan) Objects.requireNonNull(intent.getExtras().getParcelableArrayList(PLAN)).get(0));
            if(!plan.hasDate()){
                plan.setDueDate(Calendar.getInstance());
                plan.setAlarmDate(Calendar.getInstance());
            }
            inEditMode = true;

            screenHeader.setText("/" + plan.getTitle());
        }
        else {
            plan = new Plan();
            plan.setDueDate(Calendar.getInstance());
            plan.setAlarmDate(Calendar.getInstance());
            plan.setRepeat(Frequency.NEVER);
            inEditMode = false;

            screenHeader.setText("/New Plan");
        }

        EditText title = findViewById(R.id.titleTextEdit);
        title.setText(plan.getTitle());

        Switch hasDateSwitch = findViewById(R.id.hasDateSwitch);
        hasDateSwitch.setChecked(plan.hasDate());

        final LinearLayout hasDatePanel = findViewById(R.id.hasDatePanel);

        if(hasDateSwitch.isChecked()){
            hasDatePanel.setVisibility(View.VISIBLE);
        }
        else {
            hasDatePanel.setVisibility(View.GONE);
        }

        hasDateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    hasDatePanel.setVisibility(View.VISIBLE);
                }
                else {
                    hasDatePanel.setVisibility(View.GONE);
                }
            }
        });

        Button dueDateBtn = findViewById(R.id.dueDateBtn);
        dueDateBtn.setText(getDateString(plan.getDueDate()));

        dueDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dueDateBtnClicked = true;
                DateTimePickerFragment dateTimePickerFragment = new DateTimePickerFragment();
                Bundle data = new Bundle();
                data.putSerializable(DATE_TIME, plan.getDueDate());
                dateTimePickerFragment.setArguments(data);
                fm.beginTransaction().replace(R.id.dateTimeFragment, dateTimePickerFragment).commit();

                FrameLayout dateTimeFragment = findViewById(R.id.dateTimeFragment);
                dateTimeFragment.setVisibility(View.VISIBLE);
            }
        });

        Button alarmDateBtn = findViewById(R.id.alarmDateBtn);
        alarmDateBtn.setText(getDateString(plan.getAlarmDate()));

        alarmDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarmDateBtnClicked = true;
                DateTimePickerFragment dateTimePickerFragment = new DateTimePickerFragment();
                Bundle data = new Bundle();
                data.putSerializable(DATE_TIME, plan.getAlarmDate());
                dateTimePickerFragment.setArguments(data);
                fm.beginTransaction().replace(R.id.dateTimeFragment, dateTimePickerFragment).commit();

                FrameLayout dateTimeFragment = findViewById(R.id.dateTimeFragment);
                dateTimeFragment.setVisibility(View.VISIBLE);
            }
        });

        Spinner repeatSpinner = findViewById(R.id.repeatSpinner);
        String[] arraySpinner = new String[] {
                "Never", "Daily", "Weekly", "Fortnightly", "Yearly"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(adapter);

        try {
            repeatSpinner.setSelection(FrequencyHandler.frequencyIndex(plan.getRepeat()));
        } catch (Exception e) {
            e.printStackTrace();
        }

       repeatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               try {
                   plan.setRepeat(FrequencyHandler.indexToFrequency(i));
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }

           @Override
           public void onNothingSelected(AdapterView<?> adapterView) {
                adapterView.setSelection(0);
           }
       });

        EditText descriptionTextEdit = findViewById(R.id.descriptionTextEdit);
        descriptionTextEdit.setText(plan.getDescription());

        Button approveBtn = findViewById(R.id.approveBtn_activity_plan_details);
        if(inEditMode){
            approveBtn.setText("Update");
        }
        else {
            approveBtn.setText("Add");
        }

        approveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!finalisePlanDetails()){
                    return;
                }

                Intent i = new Intent();
                Bundle data = new Bundle();
                ArrayList<Plan> plans = new ArrayList<>();
                plans.add(plan);
                data.putParcelableArrayList(PLAN, plans);
                i.putExtras(data);

                setResult(RESULT_OK, i);

                finish();
            }
        });

        Button cancelBtn = findViewById(R.id.cancelBtn_activity_plan_details);
        if(inEditMode){
            cancelBtn.setText("Remove");
        }
        else {
            cancelBtn.setText("Cancel");
        }

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inEditMode){
                    Snackbar snack = Snackbar.make(findViewById(R.id.planDetailsDisplay), "Are you sure?",
                            Snackbar.LENGTH_LONG);
                    snack.setAction("yes", removePlanListener);
                    snack.show();
                }
                else{
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        });
    }

    View.OnClickListener removePlanListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DataAccess.deletePlan(plan.getId());
            cancelNotification();
            setResult(RESULT_DELETED);
            finish();
        }
    };

    private boolean finalisePlanDetails(){
        EditText titleTextEdit = findViewById(R.id.titleTextEdit);
        Switch hasDateSwitch = findViewById(R.id.hasDateSwitch);
        EditText descriptionTextEdit = findViewById(R.id.descriptionTextEdit);

        if(titleTextEdit.getText().toString().isEmpty()){
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Title must be provided!", Toast.LENGTH_LONG);
            View toastView = toast.getView();
            toastView.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            toast.show();

            return false;
        }
        else plan.setTitle(titleTextEdit.getText().toString());

        plan.setHasDate(hasDateSwitch.isChecked());
        if(!plan.hasDate()){
            plan.setDueDate(Plan.NULL_DATE());
            plan.setAlarmDate(Plan.NULL_DATE());
            plan.setRepeat(Frequency.NEVER);
        }

        plan.setDescription(descriptionTextEdit.getText().toString());

        return true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onFragmentInteraction(Calendar aDateTime) {
        Button dueDateBtn = findViewById(R.id.dueDateBtn);
        Button alarmDateBtn = findViewById(R.id.alarmDateBtn);

        if(dueDateBtnClicked){
            if(aDateTime != null)
                plan.setDueDate(aDateTime);

            if(plan.getDueDate().before(plan.getAlarmDate()))
                if (aDateTime != null) {
                    plan.setAlarmDate((Calendar) aDateTime.clone());
                }
        }
        else if (alarmDateBtnClicked){
            if(aDateTime != null) {
                if(!aDateTime.after(plan.getDueDate())) {
                    plan.setAlarmDate(aDateTime);
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Alarm Date Cannot be after the due date", Toast.LENGTH_LONG);
                    View toastView = toast.getView();
                    toastView.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                    toast.show();
                }
            }
        }
        dueDateBtn.setText(getDateString(plan.getDueDate()));
        alarmDateBtn.setText(getDateString(plan.getAlarmDate()));

        FrameLayout dateTimeFragment = findViewById(R.id.dateTimeFragment);
        dateTimeFragment.setVisibility(View.GONE);

        dueDateBtnClicked = false;
        alarmDateBtnClicked = false;
    }

    private String getDateString(Calendar c){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy - MM - dd' at 'hh:mm");
        return sdf.format(c.getTime());
    }

    private void cancelNotification(){
        Intent notifyIntent = new Intent(this, Receiver.class);
        Bundle data = new Bundle();
        ArrayList<Plan> _plans = new ArrayList<>();
        _plans.add(plan);
        data.putParcelableArrayList(PLAN, _plans);
        notifyIntent.putExtras(data);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (this, 1, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        pendingIntent.cancel();
    }
}

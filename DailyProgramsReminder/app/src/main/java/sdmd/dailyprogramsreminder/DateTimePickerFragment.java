package sdmd.dailyprogramsreminder;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Objects;

public class DateTimePickerFragment extends Fragment {
    private static final String DATE_TIME = "date_time";

    private Calendar dateTime;

    private OnFragmentInteractionListener mListener;

    public DateTimePickerFragment() {
        // Required empty public constructor
    }

    @Deprecated
    public static DateTimePickerFragment newInstance(Calendar aDateTime) {
        DateTimePickerFragment fragment = new DateTimePickerFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATE_TIME, aDateTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dateTime = Calendar.getInstance();
            dateTime.setTime(((Calendar) Objects.requireNonNull
                    (getArguments().getSerializable(DATE_TIME))).getTime());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_date_time_picker, container, false);
        initialiseUI(v);
        return v;
    }

    private void initialiseUI(View v){
        Button cancelBtn = v.findViewById(R.id.cancelBtn_fragment_date_time);
        Button approveBtn = v.findViewById(R.id.approveBtn_fragment_date_time);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentInteraction(null);
            }
        });

        approveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentInteraction(dateTime);
            }
        });

        DatePicker datePicker = v.findViewById(R.id.datePicker_fragment_date_time);
        datePicker.init(dateTime.get(Calendar.YEAR), dateTime.get(Calendar.MONTH),
                dateTime.get(Calendar.DAY_OF_MONTH), dateChangeHandler);

        TimePicker timePicker = v.findViewById(R.id.timePicker_fragment_date_time);
        timePicker.setCurrentHour(dateTime.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(dateTime.get(Calendar.MINUTE));
        timePicker.setOnTimeChangedListener(timeChangeHandler);

    }

    DatePicker.OnDateChangedListener dateChangeHandler = new DatePicker.OnDateChangedListener()
    {
        public void onDateChanged(DatePicker dp, int year, int monthOfYear, int dayOfMonth)
        {
            dateTime.set(Calendar.YEAR, year);
            dateTime.set(Calendar.MONTH, monthOfYear);
            dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }
    };

    TimePicker.OnTimeChangedListener timeChangeHandler = new TimePicker.OnTimeChangedListener() {
        @Override
        public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
            dateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateTime.set(Calendar.MINUTE, minute);
        }
    };

    @Deprecated
    public void onButtonPressed(Calendar aDateTime) {
        if (mListener != null) {
            mListener.onFragmentInteraction(aDateTime);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Calendar aDateTime);
    }
}

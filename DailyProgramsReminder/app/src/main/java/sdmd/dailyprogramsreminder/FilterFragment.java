package sdmd.dailyprogramsreminder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FilterFragment extends Fragment {
    private static final String FROM_DATE = "from_date";
    private static final String TO_DATE = "to_date";

    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MM-yyyy");

    private Calendar mFromDate;
    private Calendar mToDate;

    private OnFragmentInteractionListener mListener;

    public FilterFragment() {
        // Required empty public constructor
    }

    @Deprecated
    public static FilterFragment newInstance(Calendar fromDate, Calendar toDate) {
        FilterFragment fragment = new FilterFragment();
        Bundle args = new Bundle();
        args.putString(FROM_DATE, DATE_FORMATTER.format(fromDate.getTime()));
        args.putString(TO_DATE, DATE_FORMATTER.format(toDate.getTime()));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Calendar cal = Calendar.getInstance();
        if (getArguments() != null) {
            Date date;
            try {
                date = DATE_FORMATTER.parse(getArguments().getString(FROM_DATE));
                cal.setTime(date);
                mFromDate = (Calendar) cal.clone();

                date = DATE_FORMATTER.parse(getArguments().getString(TO_DATE));
                cal.setTime(date);
                mToDate = (Calendar) cal.clone();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else {
            mFromDate = (Calendar) cal.clone();
            mToDate = (Calendar) cal.clone();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_filter, container, false);
        initialiseUI(v);
        return v;
    }

    private void initialiseUI(View v){
        Button showResultBtn = v.findViewById(R.id.showResultBtn);
        DatePicker fromDatePicker = v.findViewById(R.id.fromDatePicker);
        DatePicker toDatePicker = v.findViewById(R.id.toDatePicker);

        Calendar c = Calendar.getInstance();
        fromDatePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), dateChangeHandler);
        toDatePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), dateChangeHandler);

        showResultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentInteraction(mFromDate, mToDate);
            }
        });
    }

    DatePicker.OnDateChangedListener dateChangeHandler = new DatePicker.OnDateChangedListener() {
        public void onDateChanged(DatePicker dp, int year, int monthOfYear, int dayOfMonth) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            if(dp.getId() == R.id.fromDatePicker) {
                mFromDate = c;
            }
            else if(dp.getId() == R.id.toDatePicker) {
                mToDate = c;
            }
        }
    };

    @Deprecated
    public void onButtonPressed(Calendar aFromDate, Calendar aToDate) {
        if (mListener != null) {
            mListener.onFragmentInteraction(aFromDate, aToDate);
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
        void onFragmentInteraction(Calendar fromDate, Calendar toDate);
    }
}

package sdmd.dailyprogramsreminder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class PlansListFragment extends Fragment {
    private static final String PLANS = "plans";
    private static final String IS_DATED = "is_dated";
    private static int WHITE = Color.rgb(250,250,250);
    private static int RED = Color.rgb(155,75,75);
    private static int GREEN = Color.rgb(90,155,75);

    private ArrayList<Plan> plans;
    private boolean isDated;

    RecyclerView.LayoutManager layoutManager;
    RVAdapter mAdapter;
    RecyclerView rvPlans;

    private OnFragmentInteractionListener mListener;

    public PlansListFragment() {
        // Required empty public constructor
    }

    @Deprecated
    public static PlansListFragment newInstance(ArrayList<Plan> aPlans, boolean aIsDated) {
        PlansListFragment fragment = new PlansListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(PLANS, aPlans);
        args.putBoolean(IS_DATED, aIsDated);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plans = getArguments().getParcelableArrayList(PLANS);
            isDated = getArguments().getBoolean(IS_DATED);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_plans_list, container, false);
        initialiseUI(v);
        return v;
    }

    private void initialiseUI(View v){
        rvPlans = v.findViewById(R.id.list);
        layoutManager = new LinearLayoutManager(v.getContext());
        rvPlans.setLayoutManager(layoutManager);
        mAdapter = new RVAdapter(plans.toArray(new Plan[plans.size()]));
        rvPlans.setAdapter(mAdapter);
    }

    @Deprecated
    public void onButtonPressed(Plan aPlan) {
        if (mListener != null) {
            mListener.onFragmentInteraction(aPlan);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Plan aPlan);
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> {
        Plan[] plans;

        RVAdapter(Plan[] aPlans){
            plans = aPlans;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v;
            if(isDated){
                v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.plan_row, parent, false);
            }
            else {
                v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.undated_plan_row, parent, false);
            }

            v.setOnClickListener(onItemClickListener);
            return new ViewHolder(v);
        }

        View.OnClickListener onItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = rvPlans.getChildLayoutPosition(view);
                mListener.onFragmentInteraction(plans[position]);
            }
        };

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            final Plan currentPlan = plans[position];

            holder.titleText.setText(currentPlan.getTitle());

            if(isDated){
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy - MM - dd");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormater = new SimpleDateFormat("hh:mm");

                holder.dueDateText.setText(dateFormater.format(currentPlan.getDueDate().getTime()));
                holder.dueTimeText.setText(timeFormater.format(currentPlan.getDueDate().getTime()));
            }

            Drawable _icon;
            if(currentPlan.isCompleted()){
                holder.view.setBackgroundColor(GREEN);
                _icon = getResources().getDrawable( R.drawable.ic_check);
            }
            else {
                if(currentPlan.hasDate()){
                    Calendar now = Calendar.getInstance();
                    if(currentPlan.getDueDate().before(now))
                        holder.view.setBackgroundColor(RED);
                }

                _icon = getResources().getDrawable( R.drawable.ic_cross);
            }

            holder.actionBtn.setCompoundDrawablesWithIntrinsicBounds(null, _icon, null, null);
            holder.actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Drawable _icon;
                    if(currentPlan.isCompleted()){
                        currentPlan.notComplete();

                        int bgColorCode = WHITE;
                        if(currentPlan.hasDate()){
                            Calendar now = Calendar.getInstance();
                            if(currentPlan.getDueDate().before(now))
                                bgColorCode = RED;
                        }
                        holder.view.setBackgroundColor(bgColorCode);

                        _icon = Objects.requireNonNull(getContext()).getResources().getDrawable( R.drawable.ic_cross);
                    }
                    else{
                        currentPlan.complete();
                        holder.view.setBackgroundColor(GREEN);
                        _icon = Objects.requireNonNull(getContext()).getResources().getDrawable( R.drawable.ic_check);

                       currentPlan.insertNextIteration();
                    }

                    holder.actionBtn.setCompoundDrawablesWithIntrinsicBounds(null, _icon, null, null);

                    DataAccess.updatePlan(currentPlan.getId(), currentPlan.isCompleted());
                }});
        }

        @Override
        public int getItemCount() {
            return plans.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public View view;
            public TextView titleText;
            public TextView dueDateText;
            public TextView dueTimeText;
            public Button actionBtn;

            ViewHolder(View v){
                super(v);
                view = v;
                titleText = v.findViewById(R.id.titleText);
                actionBtn = v.findViewById(R.id.actionBtn);

                if(isDated){
                    dueDateText = v.findViewById(R.id.dueDateText);
                    dueTimeText = v.findViewById(R.id.dueTimeText);
                }
            }
        }
    }
}

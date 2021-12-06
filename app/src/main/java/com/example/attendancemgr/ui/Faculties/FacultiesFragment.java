package com.example.attendancemgr.ui.Faculties;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendancemgr.R;
import com.example.attendancemgr.data.model.DepartmentsModel;
import com.example.attendancemgr.database.AgentCourse;
import com.example.attendancemgr.database.CourseViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;
import static com.example.attendancemgr.MainActivity2.COMPLETED_JOBS;
import static com.example.attendancemgr.MainActivity2.JOBS_FILE;
import static com.example.attendancemgr.MainActivity2.NO_DATA;

public class FacultiesFragment extends Fragment {

    private CourseViewModel mCourseViewModel;
    OnFragmentInteractionListener fragmentListener;
    LiveData<List<AgentCourse>> orgLiveData;
    RecyclerAdapter mAdapter;
    NavController controller;
    List<DepartmentsModel> itemsModelList = new ArrayList<>();
    Bundle args;
    public interface OnFragmentInteractionListener{

        void onBackPressed(boolean backPressed);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener){
            fragmentListener = (OnFragmentInteractionListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + getResources().getString(R.string.exception_message));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = getArguments();

        mCourseViewModel =
                new ViewModelProvider(this).get(CourseViewModel.class);

        OnBackPressedDispatcher dispatcher = getActivity().getOnBackPressedDispatcher();
        dispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                    fragmentListener.onBackPressed(true);
            }
        });

        NavHostFragment navHostFragment =(NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null)  controller = navHostFragment.getNavController();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.faculties, container, false);
        RecyclerView mRecyclerView = root.findViewById(R.id.facultiesRecycler);
        SearchView searchView = root.findViewById(R.id.facultiesSearchView);

        orgLiveData = mCourseViewModel.getAllAgentCourses();
        mAdapter = new RecyclerAdapter(getContext(), itemsModelList);

        orgLiveData.observe(getActivity(), agentCourses -> {
            if(agentCourses.size()>0) {
                itemsModelList.clear();
                Set<String> facSet = new HashSet<>();
                for (AgentCourse course : agentCourses) {
                    facSet.add(course.getFaculty());
                }

                for (String fac : facSet) {
                    itemsModelList.add(new DepartmentsModel(fac));
                }
                mAdapter.notifyDataSetChanged();
            }

        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mRecyclerView.setAdapter(mAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }

        });
        return root;
    }
    public boolean isWifiCxd() {
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo!=null && networkInfo.isConnected();

    }
    class RecyclerAdapter extends RecyclerView.Adapter<FacultiesFragment.RecyclerAdapter.ItemsViewHolder> implements Filterable {
        private final List<DepartmentsModel> departmentsModelList;
        private LayoutInflater mInflater;
        private List<DepartmentsModel> departmentsModelListFiltered;
        RecyclerAdapter(Context context, List<DepartmentsModel> departmentsModelList) {
            this.departmentsModelList = departmentsModelList;
            this.departmentsModelListFiltered = departmentsModelList;
            mInflater = LayoutInflater.from(context);
        }


        class ItemsViewHolder extends RecyclerView.ViewHolder{
            final FacultiesFragment.RecyclerAdapter mAdapter;
            TextView myDept;

            ItemsViewHolder(View itemView, FacultiesFragment.RecyclerAdapter adapter) {
                super(itemView);
                myDept = itemView.findViewById(R.id.deptName);
                this.mAdapter = adapter;
            }
        }
        @NonNull
        @Override
        public RecyclerAdapter.ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View mItemView = mInflater.inflate(R.layout.depts_list_view, parent, false);
            return new FacultiesFragment.RecyclerAdapter.ItemsViewHolder(mItemView,this);


        }

        @Override
        public void onBindViewHolder(@NonNull FacultiesFragment.RecyclerAdapter.ItemsViewHolder holder, int position) {
            final String mDept = departmentsModelListFiltered.get(position).getmDept();

            holder.myDept.setText(mDept);

            holder.itemView.setOnClickListener(v -> {
                if (controller != null){
                    Bundle bundle = new Bundle();
                    bundle.putString("fac", holder.myDept.getText().toString());
                    controller.navigate(R.id.departments, bundle);
                }
            });

        }

        @Override
        public int getItemCount() {
            return departmentsModelListFiltered !=null? departmentsModelListFiltered.size():0;
        }

        @Override
        public Filter getFilter() {

            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();

                    if (constraint == null || constraint.length() == 0) {
                        filterResults.count = departmentsModelList.size();
                        filterResults.values = departmentsModelList;
                    } else {
                        String searchStr = constraint.toString().toLowerCase();
                        List<DepartmentsModel> resultData = new ArrayList<>();

                        for (DepartmentsModel item : departmentsModelList) {
                            if (item.getmDept().toLowerCase().contains(searchStr)) {
                                resultData.add(item);
                            }

                            filterResults.count = resultData.size();
                            filterResults.values = resultData;
                        }
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    departmentsModelListFiltered = (List<DepartmentsModel>) results.values;
                    notifyDataSetChanged();
                }
            };
        }

    }
    private String getData(){
        SharedPreferences preferences = getActivity().getSharedPreferences(JOBS_FILE, MODE_PRIVATE);
        return preferences.getString(COMPLETED_JOBS, NO_DATA);
    }
}
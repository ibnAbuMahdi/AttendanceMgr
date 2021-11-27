package com.example.attendancemgr.ui.Departments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;

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
import static com.example.attendancemgr.MainActivity2.AVAILABLE_JOBS;
import static com.example.attendancemgr.MainActivity2.JOBS_FILE;
import static com.example.attendancemgr.MainActivity2.NO_DATA;

public class DepartmentsFragment extends Fragment {

    LiveData<List<AgentCourse>> orgLiveData;
    private CourseViewModel mCourseViewModel;
    List<DepartmentsModel> itemsModelList = new ArrayList<>();
    RecyclerAdapter mAdapter;
    String mFac;
    NavController controller;

    Bundle args;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = getArguments();
//        NavDestination destination = Navigation.findNavController(getView()).getGraph().findNode(R.id.courses);


        mCourseViewModel = new ViewModelProvider(this).get(CourseViewModel.class);
        orgLiveData = mCourseViewModel.getAllAgentCourses();

        NavHostFragment navHostFragment =(NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) controller = navHostFragment.getNavController();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.departments, container, false);
        RecyclerView mRecyclerView = root.findViewById(R.id.deptsRecycler);
        SearchView searchView = root.findViewById(R.id.deptsSearchView);
        mAdapter = new RecyclerAdapter(getContext(), itemsModelList);
        mFac = args.getString("fac");
        orgLiveData.observe(getViewLifecycleOwner(), agentCourses -> {
            if(agentCourses.size()>0) {
                if (mFac == null) {
                    itemsModelList.clear();
                    Set<String> depSet = new HashSet<>();
                    for (AgentCourse course : agentCourses) {
                        depSet.add(course.getDept());
                    }

                    for (String dep : depSet) {
                        itemsModelList.add(new DepartmentsModel(dep));
                    }
                    mAdapter.notifyDataSetChanged();
                } else {
                    itemsModelList.clear();
                    Set<String> depSet = new HashSet<>();

                    for (AgentCourse course : agentCourses) {
                        if(course.getFaculty().equals(mFac))
                            depSet.add(course.getDept());
                    }

                    for (String dep : depSet) {
                        itemsModelList.add(new DepartmentsModel(dep));
                    }
                    mAdapter.notifyDataSetChanged();
                }
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
    private String getData(){
        SharedPreferences preferences = getActivity().getSharedPreferences(JOBS_FILE, MODE_PRIVATE);
        return preferences.getString(AVAILABLE_JOBS, NO_DATA);
    }

     class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemsViewHolder> implements Filterable {
        private final List<DepartmentsModel> departmentsModelList;
        private LayoutInflater mInflater;
        private List<DepartmentsModel> departmentsModelListFiltered;
        RecyclerAdapter(Context context, List<DepartmentsModel> departmentsModelList) {
            this.departmentsModelList = departmentsModelList;
            this.departmentsModelListFiltered = departmentsModelList;
            mInflater = LayoutInflater.from(context);
        }


        class ItemsViewHolder extends RecyclerView.ViewHolder{
            final RecyclerAdapter mAdapter;
            TextView myDept;


            ItemsViewHolder(View itemView, RecyclerAdapter adapter) {
                super(itemView);
                myDept = itemView.findViewById(R.id.deptName);
                this.mAdapter = adapter;
            }
        }
        @NonNull
        @Override
        public RecyclerAdapter.ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View mItemView = mInflater.inflate(R.layout.depts_list_view, parent, false);
            return new ItemsViewHolder(mItemView,this);


        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerAdapter.ItemsViewHolder holder, int position) {
            final String mDept = departmentsModelListFiltered.get(position).getmDept();

            holder.myDept.setText(mDept);

            holder.itemView.setOnClickListener(v -> {
                if (controller != null){
                    Bundle bundle = new Bundle();
                    bundle.putString("dep", holder.myDept.getText().toString());
                    controller.navigate(R.id.courses, bundle);
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
}
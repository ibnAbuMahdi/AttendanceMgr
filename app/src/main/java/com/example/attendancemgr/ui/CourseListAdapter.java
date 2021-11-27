package com.example.attendancemgr.ui;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.attendancemgr.R;
import com.example.attendancemgr.database.AgentCourse;

import java.util.List;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.courseViewHolder> {
    private List<AgentCourse> mCourseList;
    private final LayoutInflater mInflater;

    class courseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView courseItemView;
        final CourseListAdapter mAdapter;

        /**
         * Creates a new custom view holder to hold the view to display in the RecyclerView.
         *
         * @param itemView The view in which to display the data.
         * @param adapter The adapter that manages the the data and views for the RecyclerView.
         */
        public courseViewHolder(View itemView, CourseListAdapter adapter) {
            super(itemView);
            courseItemView = (TextView) itemView.findViewById(R.id.course);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // All we do here is prepend "Clicked! " to the text in the view, to verify that
            // the correct item was clicked. The underlying data does not change.
            courseItemView.setText ("Clicked! "+ courseItemView.getText());
            mCourseList.remove(v.getId()); // remove course on click


        }
    }

    public CourseListAdapter(Context context, List<AgentCourse> courseList) {
        mInflater = LayoutInflater.from(context);
        this.mCourseList = courseList;
    }

    public void setAgentCourses(List<AgentCourse> courses) {
        mCourseList = courses;
        notifyDataSetChanged();
    }
    /**
     * Inflates an item view and returns a new view holder that contains it.
     * Called when the RecyclerView needs a new view holder to represent an item.
     *
     * @param parent The view group that holds the item views.
     * @param viewType Used to distinguish views, if more than one type of item view is used.
     * @return a view holder.
     */
    @Override
    public courseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(R.layout.courselist_item, parent, false);
        return new courseViewHolder(mItemView, this);
    }

    /**
     * Sets the contents of an item at a given position in the RecyclerView.
     * Called by RecyclerView to display the data at a specificed position.
     *
     * @param holder The view holder for that position in the RecyclerView.
     * @param position The position of the item in the RecycerView.
     */
    @Override
    public void onBindViewHolder(courseViewHolder holder, int position) {
        // Retrieve the data for that position.
        AgentCourse mCurrent = mCourseList.get(position);
        // Add the data to the view holder.
        holder.courseItemView.setText(mCurrent.getCourse());


    }

    /**
     * Returns the size of the container that holds the data.
     *
     * @return Size of the list of data.
     */
    @Override
    public int getItemCount() {
        return mCourseList.size();
    }
}



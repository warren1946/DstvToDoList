package com.dstv.todolist.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.dstv.todolist.R;
import com.dstv.todolist.dao.DbManager;
import com.dstv.todolist.pojo.Todo;
import com.dstv.todolist.utils.UserPreferenceHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.MyViewHolder>{
    private Context context;
    private List<Todo> todoList;
    private CheckBox checkBox;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TextView noNotesView;
    final private float PADDING = 45;
    private UserPreferenceHelper userPref;
    private DbManager dbManager;

    /*
    * Overloaded constructor
    * */
    public TodoListAdapter(Context context, List<Todo> todoList) {
        this.context = context;
        this.todoList = todoList;
        userPref = new UserPreferenceHelper(context);
        dbManager = new DbManager(context);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder  {
        public TextView note;
        public TextView time;
        public TextView timestamp;
        public CheckBox checkBox;
        public Toolbar toolbar;
        public Toolbar relativeLayout;
        public CardView cardView;
        public RelativeLayout l;
        public Toolbar taskIndicator;

        public MyViewHolder(final View view) {
            super(view);
            note = view.findViewById(R.id.TextTask);
            timestamp = view.findViewById(R.id.time);
            checkBox = view.findViewById(R.id.checkBox);
            toolbar = view.findViewById(R.id.toolbar22);
            relativeLayout = view.findViewById(R.id.toolbar);
            cardView = view.findViewById(R.id.card);
            l = view.findViewById(R.id.line1);
            taskIndicator = view.findViewById(R.id.taskIndicator);
        }}
    @Override
    public TodoListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.todo_list_row, parent, false);
        return new TodoListAdapter.MyViewHolder(itemView);        }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(final TodoListAdapter.MyViewHolder holder, final int position) {
        // Get the current task
        final Todo todo = todoList.get(position);

        // Check if the task has been completed or not
        if(todo.getStatus() == 1){
            // Mark as completed
            holder.note.setPaintFlags(holder.note.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskIndicator.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
            holder.timestamp.setTextColor(context.getResources().getColor(R.color.colorAccent));
        }
        // Set task name
        holder.note.setText(todo.getStrTodo());

        // Check calendar
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        String todayAsString = dateFormat.format(today);
        String date = todo.getDate_created();
        if (date.equals(todayAsString)){
            holder.timestamp.setText("Today");
        } else {
            holder.timestamp.setText(todo.getDate_created());
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTask(todoList.get(position), position);
            }
        });

        // Listen for checkbox click events
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if(holder.checkBox.isChecked()) {
                    // Update the status of the clicked tacked
                    if (todo.getStatus() == 0) {
                        todo.setStatus(1);
                    } else {
                        todo.setStatus(0);
                    }

                    int markAsDone = dbManager.updateToDo(todo);

                    if (markAsDone > 0) {
                        // Update the view
                        if(todo.getStatus() == 1){
                            // Mark as completed
                            holder.note.setPaintFlags(holder.note.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            holder.taskIndicator.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
                            holder.timestamp.setTextColor(context.getResources().getColor(R.color.colorAccent));
                        }else{
                            holder.note.setPaintFlags( holder.note.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                            holder.taskIndicator.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                            holder.timestamp.setTextColor(context.getResources().getColor(R.color.colorSecondaryText));
                        }

                        // Update progress bar
                        updateProgress();
                    }
                }
                // Uncheck the checkbox
                holder.checkBox.setChecked(false);
            }
        });
    }
    @Override
    public int getItemCount() { return todoList.size(); }

    /*
    * Clear all data from recycler
    * */
    public void clearAllData() {
        int size = todoList.size();
        todoList.clear();
        notifyItemRangeRemoved(0, size);
    }

    /*
    * Register callback to update the view when tasks are marked as done or not done.
    * ____________________________________________________________________________________________
    * Start
    * */
    public interface OnDataChangeListener{
        public void onDataChanged(int size);
    }

    OnDataChangeListener mOnDataChangeListener;
    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener){
        mOnDataChangeListener = onDataChangeListener;
    }

    private void updateProgress() {
        if(mOnDataChangeListener != null){
            mOnDataChangeListener.onDataChanged(todoList.size());
        }
    }
    /*
     * End
     * ____________________________________________________________________________________________
     *
     * Register callback to initiate task edit.
     * ____________________________________________________________________________________________
     * Start
     * */

    public interface OnEditListener{
        public void onEditTask(Todo todo, int todoPosition);
    }

    OnEditListener mOnEditListener;
    public void setOnEditListener(OnEditListener onEditListener){
        mOnEditListener = onEditListener;
    }

    private void editTask(Todo todo, int todoPosition) {
        if(mOnEditListener != null){
            mOnEditListener.onEditTask(todo, todoPosition);
        }
    }
    /*
     * End
     * ____________________________________________________________________________________________
     *
     * */
}

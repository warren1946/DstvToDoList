package com.dstv.todolist;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.TextRoundCornerProgressBar;
import com.dstv.todolist.adapter.TodoListAdapter;
import com.dstv.todolist.dao.DbManager;
import com.dstv.todolist.pojo.Todo;
import com.dstv.todolist.utils.UserPreferenceHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    // Bindable view elements
    @BindView(R.id.workThings) TextView workThings;
    @BindView(R.id.deleteAll) ImageButton deleteAll;
    @BindView(R.id.taskRemaining) TextView taskRemaining;
    @BindView(R.id.todoProgress) TextRoundCornerProgressBar todoProgress;
    @BindView(R.id.recycleTodo) RecyclerView recycleTodo;
    @BindView(R.id.welcomeMsg) LinearLayout welcomeMsg;
    @BindView(R.id.edtTodo) EditText edtTodo;
    @BindView(R.id.btnAddNew) ImageButton btnAddNew;

    // Database manager class
    private DbManager dbManager;
    // Share preference helper
    private UserPreferenceHelper userPref;
    // List adapter
    private TodoListAdapter listAdapter;
    // Date variables
    private int mYear, mMonth, mDay, mHour, mMinute;
    //Calendar
    private Calendar calendar;
    // Todo list
    private List<Todo> todoList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Cast the corresponding views in the layout
        ButterKnife.bind(this);

        //Initialize helper classes
        dbManager = new DbManager(MainActivity.this);
        userPref = new UserPreferenceHelper(MainActivity.this);
        calendar = Calendar.getInstance();

        // Underline "Work things complete" text
        workThings.setPaintFlags(workThings.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Check if there's any list items in the database and initialize screen
        checkDatabase();

        //Listen for button click events
        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the edit text is empty before adding anything to the database
                if(edtTodo.getText().toString().trim().length() == 0) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.err_todo_item), Toast.LENGTH_SHORT).show();
                    edtTodo.setError(getResources().getString(R.string.err_required));
                }else{
                    Todo todo = new Todo(0, edtTodo.getText().toString().trim(), "", 0, getDateTime());
                    ShowTodoDialog(false, todo, 0);
                }
            }
        });

        // Delete all tasks from the database and initialize screen
        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle(getResources().getString(R.string.msg_delete_all_warning));
                alertDialogBuilder.setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete all data
                                dbManager.deleteAllToDo();

                                //Clear recycler
                                listAdapter.clearAllData();

                                // Check if there's any list items in the database and initialize screen
                                checkDatabase();
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "Delete all successful!", Toast.LENGTH_SHORT).show();
                            }
                        });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    public void checkDatabase(){

        //Display number of tasks not complete
        taskRemaining.setText(dbManager.getTaskNotCompleteCount() + "");

        // Check if there's any list items in the database to display else display welcome message.
        if(dbManager.getAllTodos().size() > 0){
            deleteAll.setVisibility(View.VISIBLE);
        }else{
            welcomeMsg.setVisibility(View.VISIBLE);
            todoProgress.setProgressText(0 + "%");
            todoProgress.setProgress(0);
            deleteAll.setVisibility(View.GONE);
        }

        // Initialize screen
        initializeScreen();
    }

    public void refreshProgressbar(){
        //Display number of tasks not complete
        taskRemaining.setText(dbManager.getTaskNotCompleteCount() + "");

        // Display percentage of completed tasks
        int percentageDone = (int) dbManager.getCompletedPercentage();
        todoProgress.setProgressText(percentageDone + "%");
        todoProgress.setProgress(percentageDone);
    }

    public void initializeScreen(){
        // Refresh progress bar
        refreshProgressbar();

        //get todo list from the database and add it to the recycler
        todoList = dbManager.getAllTodos();
        listAdapter = new TodoListAdapter(MainActivity.this, todoList);

        // Listen for data change on recycler
        listAdapter.setOnDataChangeListener(new TodoListAdapter.OnDataChangeListener(){
            public void onDataChanged(int size){
                refreshProgressbar();
            }
        });

        // Edit task callback
        listAdapter.setOnEditListener(new TodoListAdapter.OnEditListener() {
            @Override
            public void onEditTask(Todo todo, int position) {
                ShowTodoDialog(true, todo, position);
            }
        });

        // Refresh recycler
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this);
        recycleTodo.setLayoutManager(mLayoutManager);
        recycleTodo.setAdapter(listAdapter);

    }

    public void refreshScreenData(int newTask){
        Todo newTodo = dbManager.getTodo(newTask);
        todoList.add(newTodo);
        listAdapter.notifyDataSetChanged();
        // Refresh progress bar
        refreshProgressbar();
        // Show delete all icon
        welcomeMsg.setVisibility(View.GONE);
        deleteAll.setVisibility(View.VISIBLE);
    }

    private void ShowTodoDialog(final boolean shouldUpdate, final Todo todo, final int position){
        // Create an alert dialog
         final AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_Light);
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(MainActivity.this);

        final View view = layoutInflaterAndroid.inflate(R.layout.new_todo_dialog, null);
        alertDialogBuilderUserInput.setView(view);

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        // Clear the edit text and remove focus
        edtTodo.setText("");
        edtTodo.clearFocus();

        // View elements to use
        final TextView pageHeader = view.findViewById(R.id.pageHeader);
        final ImageButton deleteTodo = view.findViewById(R.id.deleteTodo);
        final ImageButton closeDialog = view.findViewById(R.id.closeDialog);
        final EditText todoTask = view.findViewById(R.id.todoTask);
        final EditText todoDescription = view.findViewById(R.id.todoDescription);
        final TextView txtDate = view.findViewById((R.id.txtDate));
        final Button btnAdd = view.findViewById(R.id.btnAdd);

        // Check position to decide which title to use
        if(position == 0){
            pageHeader.setText(getResources().getText(R.string.addTask));
        }else{
            pageHeader.setText(getResources().getText(R.string.editTask));
        }

        // Listen for dialog close image button events
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.hide();
            }
        });

        // Enable delete button if user is viewing item
        if(shouldUpdate){
            deleteTodo.setVisibility(View.VISIBLE);
        }else{
            deleteTodo.setVisibility(View.GONE);
        }

        deleteTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle(getResources().getString(R.string.msg_delete_warning));
                alertDialogBuilder.setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete all data
                                dbManager.deleteToDo(todo.getId());
                                // Check if there's any list items in the database and initialize screen
                                checkDatabase();
                                dialog.dismiss();
                                alertDialog.hide();
                                Toast.makeText(MainActivity.this, "Delete successful!", Toast.LENGTH_SHORT).show();
                            }
                        });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        // Set todo string
        todoTask.setText(todo.getStrTodo());
        todoTask.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        todoTask.setRawInputType(InputType.TYPE_CLASS_TEXT);

        // Set todo description
        todoDescription.setText(todo.getStrTodo_Description());
        todoDescription.setImeOptions(EditorInfo.IME_ACTION_DONE);
        todoDescription.setRawInputType(InputType.TYPE_CLASS_TEXT);

        // Set todo date (default is today's date)
        txtDate.setText(todo.getDate_created());
        RelativeLayout rDate = view.findViewById(R.id.DateLayout);

        // Listen for date picker click listeners
        rDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, R.style.TimePickerDialogTheme,
                        new DatePickerDialog.OnDateSetListener() {

                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, monthOfYear);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                String myFormat = "dd MMM yyyy"; //In which you need put here
                                SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
                                txtDate.setText(sdf.format(calendar.getTime()));

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
                txtDate.setTextColor(Color.parseColor("#218be7"));
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                if (todoTask.getText().toString().trim().length() == 0) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.err_todo_item), Toast.LENGTH_SHORT).show();
                    todoTask.setError(getResources().getString(R.string.err_required));
                    return;
                }

                // Check if user is updating or adding a new task
                if (shouldUpdate) {
                    int update = dbManager.updateToDo(new Todo(todo.getId(), todoTask.getText().toString(), todoDescription.getText().toString(), todo.getStatus(), txtDate.getText().toString()));

                    if(update > 0){
                        checkDatabase();
                        Toast.makeText(MainActivity.this, "Update successful!",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(MainActivity.this, "Update Failed!",Toast.LENGTH_LONG).show();
                    }

                } else {

                    int add = (int)dbManager.createToDo(new Todo(0, todoTask.getText().toString(), todoDescription.getText().toString(), todo.getStatus(), txtDate.getText().toString()));

                    if(add > 0){

                        if(listAdapter == null){
                            checkDatabase();
                        }else{
                            refreshScreenData(add);
                        }

                        Toast.makeText(MainActivity.this, "Add successful!",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(MainActivity.this, "Add Failed!",Toast.LENGTH_LONG).show();
                    }
                }
                // Close the dialog
                alertDialog.dismiss();
            }
        });
    }

    /**
     * get today's datetime
     * */
    private String getDateTime() {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(
                "dd MMM yyyy", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}

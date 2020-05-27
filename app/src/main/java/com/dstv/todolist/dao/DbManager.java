package com.dstv.todolist.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dstv.todolist.pojo.Todo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DbManager extends SQLiteOpenHelper {
    // logcat tag
    private static final String LOG = DbManager.class.getName();
    // database version
    private static final int DATABASE_VERSION = 1;
    // database name
    private static final String DATABASE_NAME = "TodoDataHelper";
    // table name
    private static final String TABLE_TODO = "tbl_todo";
    // column names
    private static final String KEY_ID = "id";
    private static final String KEY_STR_TODO = "str_todo";
    private static final String KEY_STR_TODO_DESCRIPTION = "str_todo_description";
    private static final String KEY_STATUS = "status";
    private static final String KEY_DATE_CREATED = "created_at";
    // table create statement
    private static final String CREATE_TABLE_TODO = "CREATE TABLE "
            + TABLE_TODO + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_STR_TODO
            + " TEXT," + KEY_STR_TODO_DESCRIPTION + " TEXT," + KEY_STATUS + " INTEGER,"
            + KEY_DATE_CREATED + " DATETIME" + ")";
    // overloaded constructor
    public DbManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create tables
        db.execSQL(CREATE_TABLE_TODO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);// drop older tables on upgrade
        onCreate(db);// create new tables
    }
    /**
     * Create todo
     */
    public long createToDo(Todo todo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STR_TODO, todo.getStrTodo());
        values.put(KEY_STR_TODO_DESCRIPTION, todo.getStrTodo_Description());
        values.put(KEY_STATUS, todo.getStatus());
        values.put(KEY_DATE_CREATED, todo.getDate_created());

        // insert row
        long todo_id = db.insert(TABLE_TODO, null, values);

        return todo_id;
    }
    /**
     * Get todo
     */
    public Todo getTodo(long todo_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_TODO + " WHERE "
                + KEY_ID + " = " + todo_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Todo td = new Todo();
        td.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        td.setStrTodo((c.getString(c.getColumnIndex(KEY_STR_TODO))));
        td.setStrTodo_Description((c.getString(c.getColumnIndex(KEY_STR_TODO_DESCRIPTION))));
        td.setStatus((c.getInt(c.getColumnIndex(KEY_STATUS))));
        td.setDate_created(c.getString(c.getColumnIndex(KEY_DATE_CREATED)));

        return td;
    }
    /**
     * Get all todos
     * */
    public List<Todo> getAllTodos() {
        List<Todo> todoList = new ArrayList<Todo>();
        String selectQuery = "SELECT  * FROM " + TABLE_TODO;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // loop through all rows and add them to the list
        if (c.moveToFirst()) {
            do {
                Todo td = new Todo();
                td.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                td.setStrTodo((c.getString(c.getColumnIndex(KEY_STR_TODO))));
                td.setStrTodo_Description((c.getString(c.getColumnIndex(KEY_STR_TODO_DESCRIPTION))));
                td.setStatus((c.getInt(c.getColumnIndex(KEY_STATUS))));
                td.setDate_created(c.getString(c.getColumnIndex(KEY_DATE_CREATED)));

                // add to list
                todoList.add(td);
            } while (c.moveToNext());
        }

        return todoList;
    }
    /**
     * Get number of todos
     */
    public int getToDoCount() {
        String countQuery = "SELECT  * FROM " + TABLE_TODO;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }
    /**
     * Get percentage of completed tasks
     */
    public int getTaskNotCompleteCount() {
        String countQuery = "SELECT  * FROM " + TABLE_TODO + " WHERE " + KEY_STATUS + " = '0'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }
    /**
     * Get percentage of completed tasks
     */
    public float getCompletedPercentage() {
        String countQuery = "SELECT  * FROM " + TABLE_TODO + " WHERE " + KEY_STATUS + " = '1'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        int total = getToDoCount();
        float percentage = 0;
        if(total != 0){
            percentage = (float) ((count * 100)/total);
        }

        // return percentage
        return percentage;
    }
    /**
     * Update todo
     */
    public int updateToDo(Todo todo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STR_TODO, todo.getStrTodo());
        values.put(KEY_STR_TODO_DESCRIPTION, todo.getStrTodo_Description());
        values.put(KEY_STATUS, todo.getStatus());
        values.put(KEY_DATE_CREATED, todo.getDate_created());

        // updating row
        return db.update(TABLE_TODO, values, KEY_ID + " = ?",
                new String[] { String.valueOf(todo.getId()) });
    }
    /**
     * Delete todo
     */
    public void deleteToDo(long todo_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TODO, KEY_ID + " = ?",
                new String[] { String.valueOf(todo_id) });
    }
    /**
     * Delete all todo
     */
    public void deleteAllToDo() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TODO, null, null);
    }
    /**
     * Close database
     * */
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
    /**
     * Get today's datetime
     * */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}

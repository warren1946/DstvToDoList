package com.dstv.todolist.pojo;

public class Todo {
    /*
    * Global variables
    * */
    private int id;
    private int status;
    private String strTodo;
    private String strTodo_Description;
    private String date_created;
    /*
     * Default constructor
     * */
    public Todo(){}
    /*
     * Overloaded constructor
     * */
    public Todo(int id, String strTodo, String strTodo_Description, int status, String date_created){
        this.setId(id);
        this.setStrTodo(strTodo);
        this.setStrTodo_Description(strTodo_Description);
        this.setStatus(status);
        this.setDate_created(date_created);
    }
    /*
    * Getters and setters
    * */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStrTodo() {
        return strTodo;
    }

    public void setStrTodo(String strTodo) {
        this.strTodo = strTodo;
    }

    public String getStrTodo_Description() {
        return strTodo_Description;
    }

    public void setStrTodo_Description(String strTodo_description) {
        this.strTodo_Description = strTodo_description;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }
}

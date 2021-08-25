package com.juvenxu.mvnbook.account.persist;

public class Account {
    private String id;
    private String name;
    private String email;
    private String password;
    private boolean activated;

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    // getter and setter methods for email,password and activated
}

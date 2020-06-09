package com.google.sps.servlets;

public class Comment {
    private long id;
    private String name;
    private String email;
    private String subject;
    private String message;

    public Comment(long i, String n, String e, String s, String m) {
        id = i;
        name = n;
        email = e;
        subject = s;
        message = m;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }
}
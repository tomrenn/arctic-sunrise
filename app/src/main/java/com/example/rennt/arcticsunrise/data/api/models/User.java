package com.example.rennt.arcticsunrise.data.api.models;

/**
 * Represent a WSJ user.
 */
public class User {
    private String firstName;
    private String lastName;
    private String email;
    private boolean isPaidUser;
    private String oauthUUID;

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getEmail() { return email; }
    public boolean isPaidSubscriber(){ return isPaidUser; }

    public User(String firstName, String lastName, String email, boolean paidSubscriber){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.isPaidUser = paidSubscriber;
    }
}

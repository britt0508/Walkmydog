package com.example.britt.walkmydog;

/**
 * Created by britt on 10-1-2018.
 */

public class Owner {
    public String type;
    public String name;
    public String email;
    public String dog;
    public Boolean advert_status;

    public Owner() {}

    public Owner(String type, String name, String email, String dog, Boolean advert_status) {
        this.type = type;
        this.name = name;
        this.email = email;
        this.dog = dog;
        this.advert_status = advert_status;
    }
}

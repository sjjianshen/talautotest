package com.tal;

import lombok.Data;

@Data
public class User {
    public String name;
    private int age;
    private Address address;
}

package com.mg.dribbler.enums;

/**
 * Created by Admin on 4/8/2017.
 */

public enum SignupType {
    FacebookSignup("Facebook", 0),
    GoogleSingup("Google", 1),
    CommonSignup("Common", 2);

    private String stringValue;
    private int intValue;

    private SignupType(String toString, int value) {
        stringValue = toString;
        intValue = value;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}

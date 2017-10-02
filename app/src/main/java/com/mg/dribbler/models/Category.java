package com.mg.dribbler.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Admin on 5/2/2017.
 */

public class Category implements Serializable{
    public int category_id;
    public String category_title;
    public String thumbnail_url;
    public Double price;
    public Boolean premium;
    public Boolean unlocked;
    public ArrayList<Trick> tricks = new ArrayList<>();
}

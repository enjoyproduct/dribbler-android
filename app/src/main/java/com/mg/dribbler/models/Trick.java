package com.mg.dribbler.models;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by Admin on 5/2/2017.
 */

public class Trick {
    public int trick_id;
    public int category_id;
    public String trick_title;
    public ArrayList<String> tagArr;
    public String trick_tags;
    public String thumbnail_url;
    public String ld_video_url;
    public String hd_video_url;
    public ArrayList<TrickDescription> descriptions = new ArrayList<>();
}


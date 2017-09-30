package com.mg.dribbler.models;

import java.util.ArrayList;

/**
 * Created by Admin on 5/2/2017.
 */

public class Tag {
    public int tag_id;
    public String tag_name;

    public static Tag fetchTagWithName(ArrayList<Tag> tags, String tag_name) {
        for (Tag tag : tags) {
            if (tag.tag_name.equals(tag_name)) {
                return tag;
            }
        }

        return null;
    }

    public static ArrayList<Tag> appendWithoutDuplication(ArrayList<Tag> tags, Tag tag) {
        boolean isExist = false;
        for (Tag temp : tags) {
            if (temp.tag_id == tag.tag_id) {
                isExist = true;
                break;
            }
        }

        if (!isExist) {
            tags.add(tag);
        }

        return tags;
    }
}

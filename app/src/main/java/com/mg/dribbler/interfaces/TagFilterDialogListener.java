package com.mg.dribbler.interfaces;

import com.mg.dribbler.models.Tag;

import java.util.ArrayList;

/**
 * Created by Admin on 5/7/2017.
 */

public interface TagFilterDialogListener {

    void filteredTags(ArrayList<Tag> selectedTags);
}

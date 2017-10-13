package com.mg.dribbler.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.mg.dribbler.R;
import com.mg.dribbler.interfaces.TagFilterDialogListener;
import com.mg.dribbler.models.Global;
import com.mg.dribbler.models.Tag;

import java.util.ArrayList;

/**
 * Created by Admin on 5/7/2017.
 */

public class  TagFilterDialog extends Dialog {

    Context context;
    private ArrayList<Tag> mFilterTagArray;
    private TagFilterDialogListener dialogListener;

    public TagFilterDialog(Context context,  ArrayList<Tag> filterTagArray, TagFilterDialogListener listener) {
        super(context, R.style.CustomDialog);
        this.context = context;
        this.mFilterTagArray = filterTagArray;
        this.dialogListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_tag_filter);

        // list view
        ListView listView = (ListView) findViewById(R.id.listView);
        final ListViewAdapter adapter = new ListViewAdapter();
        listView.setAdapter(adapter);

        // Clear Button
        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilterTagArray.clear();
                adapter.notifyDataSetChanged();
            }
        });
        // Filter Button
        findViewById(R.id.btn_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListener.filteredTags(mFilterTagArray);
                dismiss();
            }
        });
        findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListener.filteredTags(mFilterTagArray);
                dismiss();
            }
        });
    }

    /**
     * ListView Adapter for tricks
     */
    class ListViewAdapter extends BaseAdapter {

        LayoutInflater inflater;

        public ListViewAdapter() {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return Global.sharedInstance().tagArr.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.row_filter_tag, null);
            final Tag tag = Global.sharedInstance().tagArr.get(position);
            final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            checkBox.setText(tag.tag_name);
            checkBox.setChecked(isSelectedTag(tag));
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Tag tag = Global.sharedInstance().tagArr.get(position);
                    if (isChecked) {
                        mFilterTagArray.add(tag);
                    } else {
                        mFilterTagArray.remove(tag);
                    }
                }
            });
            return convertView;
        }
    }

    private boolean isSelectedTag(Tag tag) {
        for (Tag tempTag: mFilterTagArray) {
            if (tempTag.tag_id == tag.tag_id) {
                return true;
            }
        }

        return false;
    }
}

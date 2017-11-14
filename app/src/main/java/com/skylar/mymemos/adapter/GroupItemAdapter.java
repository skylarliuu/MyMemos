package com.skylar.mymemos.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.TextViewCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skylar.mymemos.R;
import com.skylar.mymemos.application.MyApplication;
import com.skylar.mymemos.db.MemoDBManager;

import java.util.List;

/**
 * Created by Skylar on 2017/4/6.
 */

public class GroupItemAdapter extends BaseAdapter {

    private final int mIconSize;
    private Context mContext;
    private LayoutInflater mInflater;
    private List<String> groupNameList;
    private MemoDBManager dbManager;

    public GroupItemAdapter(Context context,List<String> groupNameList){
        mInflater = LayoutInflater.from(context);
        mContext = context;
        this.groupNameList = groupNameList;
        mIconSize = context.getResources().getDimensionPixelSize(R.dimen.drawer_icon_size);//24dp
        dbManager = MyApplication.getDBManager();
    }

    @Override
    public int getCount() {
        return groupNameList.size();
    }

    @Override
    public Object getItem(int position) {
        return groupNameList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {

        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.group_list_item, parent,
                    false);
        }

        final TextView groupNameText = (TextView) convertView.findViewById(R.id.groupNameText);
        ImageView deleteView = (ImageView) convertView.findViewById(R.id.deleteView);

        groupNameText.setText(groupNameList.get(position));
        Drawable icon;
        //不可删除默认分组
        if(groupNameText.getText().equals(mContext.getString(R.string.all_group))){
            deleteView.setVisibility(View.INVISIBLE);
            icon = mContext.getResources().getDrawable(R.drawable.icon_all_group_black);
        }else{
            icon = mContext.getResources().getDrawable(R.drawable.icon_a_group_);
        }
        setIconColor(icon);
        if (icon != null)
        {
            icon.setBounds(0, 0, mIconSize, mIconSize);
            TextViewCompat.setCompoundDrawablesRelative(groupNameText, icon, null, null, null);
        }

        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //删除分组
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        mContext);
                builder.setTitle(mContext.getString(R.string.menu_delete));
//                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setMessage(mContext.getString(R.string.group_item_delete));
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dbManager.deleteGroup(groupNameList.get(position));
                                groupNameList.remove(position);
                                notifyDataSetChanged();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.show();
            }
        });

        return convertView;
    }

    public void setIconColor(Drawable icon)
    {
        int textColorSecondary = android.R.attr.textColorSecondary;
        TypedValue value = new TypedValue();
        if (!mContext.getTheme().resolveAttribute(textColorSecondary, value, true))
        {
            return;
        }
        int baseColor = mContext.getResources().getColor(value.resourceId);
        icon.setColorFilter(baseColor, PorterDuff.Mode.MULTIPLY);
    }
}

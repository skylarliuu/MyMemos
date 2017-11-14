package com.skylar.mymemos.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.skylar.mymemos.application.MyApplication;
import com.skylar.mymemos.bean.Memo;
import com.skylar.mymemos.bean.MemoItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Skylar on 2017/3/21.
 */

public class MemoAdapter extends CursorAdapter {


    private HashMap<Integer, Boolean> mSelectedIndex;
    private boolean mChoiceMode;

    public MemoAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mSelectedIndex = new HashMap<Integer, Boolean>();
    }



    /* (1)newView：并不是每次都被调用的，它只在实例化的时候调用,数据增加的时候也会调用,但是在重绘(比如修改条目里的TextView的内容)的时候不会被调用
       (2)bindView：从代码中可以看出在绘制Item之前一定会调用bindView方法它在重绘的时候也同样被调用
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        if (view instanceof MemoItem) {
            long id = cursor.getLong(0);
            Memo data = MyApplication.getDBManager().getMemo(id);
            ((MemoItem) view).bind(context, data, mChoiceMode,
                    isSelectedItem(cursor.getPosition()));
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new MemoItem(context);
    }

    public void setChoiceMode(boolean mode) {
        mSelectedIndex.clear();
        mChoiceMode = mode;
        notifyDataSetChanged();
    }

    public void setCheckedItem(final int position, final boolean checked) {
        mSelectedIndex.put(position, checked);
        notifyDataSetChanged();
    }

    public boolean isInChoiceMode() {
        return mChoiceMode;
    }

    public HashSet<Long> getSelectedItemIds() {
        HashSet<Long> itemSet = new HashSet<Long>();
        for (Integer position : mSelectedIndex.keySet()) {
            if (mSelectedIndex.get(position) == true) {
                Long id = getItemId(position);
                itemSet.add(id);
            }
        }
        return itemSet;
    }

    public int getSelectedCount() {
        Collection<Boolean> values = mSelectedIndex.values();
        if (null == values) {
            return 0;
        }
        Iterator<Boolean> iter = values.iterator();
        int count = 0;
        while (iter.hasNext()) {
            if (true == iter.next()) {
                count++;
            }
        }
        return count;
    }

    public boolean isSelectedItem(final int position) {
        if (null == mSelectedIndex.get(position)) {
            return false;
        }
        return mSelectedIndex.get(position);
    }

}

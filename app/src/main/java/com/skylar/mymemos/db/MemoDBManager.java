package com.skylar.mymemos.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.skylar.mymemos.application.GlobalValues;
import com.skylar.mymemos.bean.Memo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Skylar on 2017/3/21.
 */

public class MemoDBManager {

    private SQLiteDatabase db;
    private String ORDER_BY = "stick desc,modifiedDate desc";//asc

    public MemoDBManager(SQLiteDatabase db) {
        this.db = db;
    }

    //添加便签
    public void addMemo(Memo memo) {
        ContentValues values = new ContentValues();
        values.put("content", memo.getContent());
        values.put("groupName", memo.getGroupName());
        values.put("widgetId", memo.getWidgetId());
        values.put("widgetType", memo.getWidgetType());
        values.put("modifiedDate", memo.getModifiedDate());
        values.put("alarmDate", memo.getAlarmDate());
        values.put("createDate",memo.getCreateDate());
        values.put("stick",memo.getStick());
        values.put("fontSize",memo.getFontSize());
        values.put("alarmEnable",memo.getAlarmEnable());
        db.insert(GlobalValues.TABLE_NAME, null, values);
    }

    //查询所有便签
    public Cursor queryAll(){
        Cursor cursor = db.query(GlobalValues.TABLE_NAME, null, null, null, null, null, ORDER_BY);
        return cursor;
    }

    //查询某一个便签
    public Memo getMemo(Long memoId) {
        Memo memo = new Memo();
        Cursor cursor = db.query(GlobalValues.TABLE_NAME,
                new String[]{"content,groupName,widgetId,widgetType,modifiedDate,alarmDate,createDate,stick,fontSize,alarmEnable"},
                "_id = ?", new String[]{memoId+""}, null, null, ORDER_BY, null);
        if (cursor.moveToFirst()) {
            do {
                memo.setId(memoId);
                memo.setContent(cursor.getString(cursor.getColumnIndex("content")));
                memo.setGroupName(cursor.getString(cursor.getColumnIndex("groupName")));
                memo.setWidgetId(cursor.getInt(cursor.getColumnIndex("widgetId")));
                memo.setWidgetType(cursor.getInt(cursor.getColumnIndex("widgetType")));
                memo.setModifiedDate(cursor.getLong(cursor.getColumnIndex("modifiedDate")));
                memo.setAlarmDate(cursor.getLong(cursor.getColumnIndex("alarmDate")));
                memo.setCreateDate(cursor.getLong(cursor.getColumnIndex("createDate")));
                memo.setStick(cursor.getInt(cursor.getColumnIndex("stick")));
                memo.setFontSize(cursor.getInt(cursor.getColumnIndex("fontSize")));
                memo.setAlarmEnable(cursor.getString(cursor.getColumnIndex("alarmEnable")));
            } while (cursor.moveToNext());
        }
        if(cursor!= null){
            cursor.close();
        }

        return memo;
    }

    //删除某一个便签
    public void deleteMemos(Long memoId){
            db.delete(GlobalValues.TABLE_NAME,"_id = ?",new String[]{memoId+""});
    }

    //查询符合widgetId值得便签
    public List<Memo>  queryByWidget(int widgetId){
        List<Memo> memoList = new ArrayList<Memo>() ;
        Cursor cursor = db.query(GlobalValues.TABLE_NAME,
                new String[]{"_id,content,groupName,widgetId,widgetType,modifiedDate,alarmDate,createDate,stick,fontSize,alarmEnable"},
                "widgetId = ?", new String[]{widgetId+""}, null, null, ORDER_BY, null);
        if (cursor.moveToFirst()) {
            do {
                Memo memo = new Memo();
                memo.setId(cursor.getLong(cursor.getColumnIndex("_id")));
                memo.setContent(cursor.getString(cursor.getColumnIndex("content")));
                memo.setGroupName(cursor.getString(cursor.getColumnIndex("groupName")));
                memo.setWidgetId(cursor.getInt(cursor.getColumnIndex("widgetId")));
                memo.setWidgetType(cursor.getInt(cursor.getColumnIndex("widgetType")));
                memo.setModifiedDate(cursor.getLong(cursor.getColumnIndex("modifiedDate")));
                memo.setAlarmDate(cursor.getLong(cursor.getColumnIndex("alarmDate")));
                memo.setCreateDate(cursor.getLong(cursor.getColumnIndex("createDate")));
                memo.setStick(cursor.getInt(cursor.getColumnIndex("stick")));
                memo.setFontSize(cursor.getInt(cursor.getColumnIndex("fontSize")));
                memo.setAlarmEnable(cursor.getString(cursor.getColumnIndex("alarmEnable")));
                memoList.add(memo);
            } while (cursor.moveToNext());
        }
        if(cursor!= null){
            cursor.close();
        }
        return memoList;
    }

    //更新某一个便签
    public void updateMemo(Memo memo){
        ContentValues values = new ContentValues();
        values.put("content", memo.getContent());
        values.put("groupName", memo.getGroupName());
        values.put("widgetId", memo.getWidgetId());
        values.put("widgetType", memo.getWidgetType());
        values.put("modifiedDate", memo.getModifiedDate());
        values.put("alarmDate", memo.getAlarmDate());
        values.put("stick",memo.getStick());
        values.put("fontSize",memo.getFontSize());
        values.put("alarmEnable",memo.getAlarmEnable());
        db.update(GlobalValues.TABLE_NAME,values,"_id = ?",new String[]{memo.getId()+""});
    }

    //添加一个分组
    public void addGroup(String groupName){
        ContentValues values = new ContentValues();
        values.put("groupName",groupName);
//        values.put("groupId",groupId);
        db.insert(GlobalValues.TABLE_GROUP_NAME,null,values);
    }

    //查询某一个分组的便签
    public Cursor queryGroup(String groupName){
        Cursor  cursor = db.query(GlobalValues.TABLE_NAME,null,"groupName = ?",new String[]{groupName},null,null,ORDER_BY);

        return cursor;
    }

    //查询所有的分组
    public List<String> queryAllGroup(){
        Cursor cursor = db.query(GlobalValues.TABLE_GROUP_NAME,null,null,null,null,null,null);
        List<String> groupNameList = new ArrayList<String>();
        if(cursor.moveToFirst()){
            do {
                String groupName = cursor.getString(cursor.getColumnIndex("groupName"));
                groupNameList.add(groupName);
            } while (cursor.moveToNext());
        }
        return groupNameList;
    }

    public void deleteGroup(String groupName){
       db.delete(GlobalValues.TABLE_GROUP_NAME,"groupName = ?",new String[]{groupName});
    }
}

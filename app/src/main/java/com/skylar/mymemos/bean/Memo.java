package com.skylar.mymemos.bean;

/**
 * Created by Skylar on 2017/3/15.
 */

public class Memo {
    private long _id;
    private String content;
    private String groupName;  //分组名称
    private int widgetId;      //桌面小工具id
    private int widgetType;    //桌面小工具类型
    private long modifiedDate; //便签修改时间
    private long alarmDate;    //便签提醒时间
    private long createDate;   //创建时间

    public String getAlarmEnable() {
        return alarmEnable;
    }

    public void setAlarmEnable(String alarmEnable) {
        this.alarmEnable = alarmEnable;
    }

    private String alarmEnable = "false";//提醒的标志

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    private int stick;         //置顶标志
    private int fontSize;      //字体大小

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public long getAlarmDate() {
        return alarmDate;
    }

    public void setAlarmDate(long alarmDate) {
        this.alarmDate = alarmDate;
    }

    public int getStick() {
        return stick;
    }

    public void setStick(int stick) {
        this.stick = stick;
    }

    public long getId() {
        return _id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setId(long _id) {
        this._id = _id;
    }

    public int getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(int widgetId) {
        this.widgetId = widgetId;
    }

    public int getWidgetType() {
        return widgetType;
    }

    public void setWidgetType(int widgetType) {
        this.widgetType = widgetType;
    }

    public long getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(long modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

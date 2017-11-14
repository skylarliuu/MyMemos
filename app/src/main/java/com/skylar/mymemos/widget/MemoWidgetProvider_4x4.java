package com.skylar.mymemos.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;

import com.skylar.mymemos.R;
import com.skylar.mymemos.application.GlobalValues;


public class MemoWidgetProvider_4x4 extends MemoWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.update(context, appWidgetManager, appWidgetIds);
    }

    protected int getLayoutId() {
        return R.layout.widget_4x4;
    }

//    @Override
//    protected int getBgResourceId(int bgId) {
//        return ResourceParser.WidgetBgResources.getWidget4xBgResource(bgId);
//    }

    @Override
    protected int getWidgetType() {
        return GlobalValues.WIDGET_4X;
    }
}

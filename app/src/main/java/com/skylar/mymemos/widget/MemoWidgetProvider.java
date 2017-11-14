package com.skylar.mymemos.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.skylar.mymemos.R;
import com.skylar.mymemos.activity.MemoEditActivity;
import com.skylar.mymemos.application.GlobalValues;
import com.skylar.mymemos.application.MyApplication;
import com.skylar.mymemos.bean.Memo;
import com.skylar.mymemos.db.MemoDBManager;

import java.util.List;

public abstract class MemoWidgetProvider extends AppWidgetProvider {

	private MemoDBManager dbManager;

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {

	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		update(context, appWidgetManager, appWidgetIds);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	      protected void update(Context context, AppWidgetManager appWidgetManager,
			   int[] appWidgetIds) {
		    dbManager = MyApplication.getDBManager();
		    for (int i = 0; i < appWidgetIds.length; i++) {
			  if (appWidgetIds[i] != AppWidgetManager.INVALID_APPWIDGET_ID) {
				Intent intent = new Intent(context, MemoEditActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.setAction(Intent.ACTION_VIEW);
				intent.putExtra(GlobalValues.INTENT_WIDGET_TYPE, getWidgetType());
				List<Memo> list = dbManager.queryByWidget(appWidgetIds[i]);
				RemoteViews rv = new RemoteViews(context.getPackageName(),
						getLayoutId());
				if (list.size() == 0) {
				  rv.setTextViewText(R.id.widget_text, "点击创建新便签");
				  rv.setTextViewTextSize(R.id.widget_text, TypedValue.COMPLEX_UNIT_SP,18);
				} else {
				  Memo memo = list.get(0);
				  rv.setTextViewText(R.id.widget_text, memo.getContent());
				  rv.setTextViewTextSize(R.id.widget_text, TypedValue.COMPLEX_UNIT_SP,
							memo.getFontSize());
				  intent.putExtra(Intent.EXTRA_UID, memo.getId());
				}
				intent.putExtra(GlobalValues.INTENT_WIDGET_ID, appWidgetIds[i]);
				PendingIntent pendingIntent = null;
				pendingIntent = PendingIntent.getActivity(context, appWidgetIds[i],
						intent, PendingIntent.FLAG_UPDATE_CURRENT);
				rv.setOnClickPendingIntent(R.id.widget_text, pendingIntent);
				appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
			  }
		    }
	      }

//	protected abstract int getBgResourceId(int bgId);

	protected abstract int getLayoutId();

	protected abstract int getWidgetType();
}

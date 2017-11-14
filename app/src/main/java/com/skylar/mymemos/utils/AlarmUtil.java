package com.skylar.mymemos.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.skylar.mymemos.application.GlobalValues;
import com.skylar.mymemos.bean.Memo;

/**
 * Created by Skylar on 2017/5/10.
 */

public class AlarmUtil {

    /**
     * 设置便签提醒
     */
    public static void setAlarmClock(Context context, Memo memo) {
      AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
      Intent intent1 = new Intent(context, AlarmReceiver.class);
      intent1.putExtra(GlobalValues.INTENT_MEMO_ID, memo.getId());
      PendingIntent pi1 = PendingIntent.getBroadcast(context,
              memo.getWidgetId(), intent1, 0);
      long alarmTime = memo.getAlarmDate();
      if((alarmTime - System.currentTimeMillis() > 0)) {
         am.cancel(pi1);
         am.set(AlarmManager.RTC_WAKEUP, alarmTime, pi1);
      }
    }
    /**
     * 取消便签提醒
     */
    public static void cancleAlarmClock(Context context, Memo memo) {
      AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
      Intent intent = new Intent(context, AlarmReceiver.class);
      intent.putExtra(GlobalValues.INTENT_MEMO_ID, memo.getId());
      PendingIntent pi = PendingIntent.getBroadcast(context,
              memo.getWidgetId(), intent, 0);
      am.cancel(pi);
    }

}

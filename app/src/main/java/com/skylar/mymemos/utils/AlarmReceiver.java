package com.skylar.mymemos.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.skylar.mymemos.R;
import com.skylar.mymemos.activity.MemoEditActivity;
import com.skylar.mymemos.application.GlobalValues;
import com.skylar.mymemos.application.MyApplication;
import com.skylar.mymemos.bean.Memo;
import com.skylar.mymemos.db.MemoDBManager;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Skylar on 2017/5/10.
 */

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取系统的NotificationManager服务
        long memoId = intent.getLongExtra(GlobalValues.INTENT_MEMO_ID,0L);
        String content = "提醒";
        if(memoId!= 0L){
            MemoDBManager dbManager = MyApplication.getDBManager();
            Memo memo = dbManager.getMemo(memoId);
            content = memo.getContent();
        }
        Intent intent1 = new Intent(context
                , MemoEditActivity.class);
        intent1.putExtra(Intent.EXTRA_UID, memoId);
        PendingIntent pi = PendingIntent.getActivity(
                context, 0, intent, 0);
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification notify = new Notification.Builder(context)
                // 设置打开该通知，该通知自动消失
                .setAutoCancel(true)
                // 设置显示在状态栏的通知提示信息
                .setTicker(context.getString(R.string.alarm_notice_ticker))
                // 设置通知的图标
                .setSmallIcon(R.mipmap.ic_launcher)
                // 设置通知内容的标题
                .setContentTitle(context.getString(R.string.alarm_notice_ticker))
                // 设置通知内容
                .setContentText(content)
                // 设置使用系统默认的声音、默认LED灯
                 .setDefaults(Notification.DEFAULT_SOUND
                 |Notification.DEFAULT_LIGHTS)
                // 设置通知的自定义声音
//                .setSound(Uri.parse("android.resource://org.crazyit.ui/"
//                        + R.raw.msg))
                .setWhen(System.currentTimeMillis())
                // 设改通知将要启动程序的Intent
                .setContentIntent(pi)  // ①
                .build();
        // 发送通知
        manager.notify(Integer.parseInt(memoId+""), notify);
    }


}

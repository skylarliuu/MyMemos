package com.skylar.mymemos.bean;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skylar.mymemos.R;

/**
 * Created by Skylar on 2017/3/21.
 */

public class MemoItem extends LinearLayout{
    private CardView mCardView;
    private TextView mTitle;
    private TextView mTime;
    private ImageView mAlarmIcon;
    private CheckBox mCheckBox;
    private Memo data = null;

    public MemoItem(Context context) {
        super(context);
        inflate(context, R.layout.memo_item, this);
        mTitle = (TextView) findViewById(R.id.tv_title);
        mTime = (TextView) findViewById(R.id.tv_time);
        mCheckBox = (CheckBox) findViewById(android.R.id.checkbox);
        mCardView = (CardView) findViewById(R.id.cardView);
        mAlarmIcon = (ImageView) findViewById(R.id.alarm);
    }

    public void bind(Context context, Memo data, boolean choiceMode,
                     boolean checked) {
        this.data = data;
        if (choiceMode) {
            mCheckBox.setVisibility(View.VISIBLE);
            mCheckBox.setChecked(checked);
        } else {
            mCheckBox.setVisibility(View.GONE);
        }

        mTitle.setText(data.getContent());

       /*    DateUtils.getRelativeTimeSpanString(System.currentTimeMillis()+60*4000));
             //返回相对于当前时间的最大区间表示的字符串：几(分钟,小时,天,周,月,年)前/后
              DateUtils.getRelativeTimeSpanString(context, long timeMillis);
              //返回相对于当前时间的，参数时间字符串：在同一天显示时分；在不同一天，显示月日；在不同一年，显示年月日
        * */
        mTime.setText(DateUtils.getRelativeTimeSpanString(context,data.getModifiedDate()));

        if(data.getStick() != 0){//置顶状态
//            mCardView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            mCardView.setCardBackgroundColor(getResources().getColor(R.color.stick_item_bg));
        }else{
            mCardView.setCardBackgroundColor(Color.WHITE);
        }

        if(data.getAlarmEnable().equals("true")){
            mAlarmIcon.setVisibility(View.VISIBLE);
        }else{
            mAlarmIcon.setVisibility(View.INVISIBLE);
        }

    }

    public Memo getItemData() {
        return data;
    }
}

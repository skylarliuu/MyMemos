package com.skylar.mymemos.activity;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.skylar.mymemos.R;
import com.skylar.mymemos.application.GlobalValues;
import com.skylar.mymemos.application.MyApplication;
import com.skylar.mymemos.bean.Memo;
import com.skylar.mymemos.db.MemoDBManager;
import com.skylar.mymemos.utils.AlarmUtil;
import com.skylar.mymemos.widget.MemoWidgetProvider_4x1;
import com.skylar.mymemos.widget.MemoWidgetProvider_4x2;
import com.skylar.mymemos.widget.MemoWidgetProvider_4x3;
import com.skylar.mymemos.widget.MemoWidgetProvider_4x4;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Skylar on 2017/3/15.
 */

public class MemoEditActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener
,TimePickerDialog.OnTimeSetListener{

    private TextView tv_alarm_time;
    private ScrollView mScrollView;
    private EditText mEditText;
    private TextView createTime;
    private MemoDBManager dbManager;
    private Memo mNewMemo;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private int mAppWidgetType;
    private boolean createNew = true;
    private int fontSize = 18;//默认字体大小
    private int mFontSizeItem =2;//当前选中的字体选项
    private int mGroupItem = 0;//当前选中的分组选项
    private int mStick = 0;//置顶标志
    private int selectShareItem = 0;
    private long memoId;
    private String groupName;
    private String content;
    private String alarmEnable;
    private long alarmDate;

    //保存开始的状态，在退出时比较有没有变化
    private int preFontSize;
    private int preStick;
    private String preGroupName;
    private String preContent;

    private static int FONT_SIZE_SUPER_SMALL = 14;
    private static int FONT_SIZE_SMALL = 16;
    private static int FONT_SIZE_NORMAL = 18;
    private static int FONT_SIZE_HUGE = 20;
    private static int FONT_SIZE_SUPER_HUGE = 22;

    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";
    private Calendar calendar;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private int mYear,mMonth,mDay,mHour,mMinute;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        groupName = getString(R.string.all_group);//默认分组是全部分组
        dbManager = MyApplication.getDBManager();
        Intent intent = getIntent();
        mAppWidgetId = intent.getIntExtra(GlobalValues.INTENT_WIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mAppWidgetType = intent.getIntExtra(GlobalValues.INTENT_WIDGET_TYPE,GlobalValues.WIDGET_INVALIDE);
        if (savedInstanceState == null && !initActivityState(getIntent())) {
            finish();
            return;
        }

        initUI();

        //初始化日期控件
        calendar = Calendar.getInstance();

        datePickerDialog = DatePickerDialog.newInstance(this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                false);

        timePickerDialog = TimePickerDialog.newInstance(this,
                calendar.get(Calendar.HOUR_OF_DAY) ,
                calendar.get(Calendar.MINUTE),
                false, false);//最后两个参数，是否是24小时制，是否抖动

        //保存日期选择器状态
        if (savedInstanceState != null) {
            DatePickerDialog dpd = (DatePickerDialog) getSupportFragmentManager().findFragmentByTag(DATEPICKER_TAG);
            if (dpd != null) {
                dpd.setOnDateSetListener(this);
            }

            TimePickerDialog tpd = (TimePickerDialog) getSupportFragmentManager().findFragmentByTag(TIMEPICKER_TAG);
            if (tpd != null) {
                tpd.setOnTimeSetListener(this);
            }
        }

    }

    private boolean initActivityState(Intent intent) {
        memoId = intent.getLongExtra(Intent.EXTRA_UID, 0L);

        if (memoId == 0) { //新建一个便签
            mNewMemo = new Memo();
            mNewMemo.setStick(0);
            mNewMemo.setCreateDate(System.currentTimeMillis());
            createNew = true;
            return true;
        } else { //读取便签
            createNew = false;
            mNewMemo = dbManager.getMemo(memoId);
            mAppWidgetId = mNewMemo.getWidgetId();
            mAppWidgetType = mNewMemo.getWidgetType();
            if (mNewMemo == null) {
                finish();
                return false;
            }
            return true;
        }
    }

    private void initUI(){
        tv_alarm_time = (TextView) findViewById(R.id.tv_alarm_time);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mEditText = (EditText) findViewById(R.id.editText);
        createTime = (TextView) findViewById(R.id.createTime);
        if(mNewMemo != null){
            content = mNewMemo.getContent();
            if(content!=null){
                mEditText.setText(content);
                mEditText.setSelection(content.length());//光标指向最后
                fontSize = mNewMemo.getFontSize();
                groupName = mNewMemo.getGroupName();
                mStick = mNewMemo.getStick();
                alarmEnable = mNewMemo.getAlarmEnable();
                alarmDate = mNewMemo.getAlarmDate();
                invalidateOptionsMenu();
                mEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP,fontSize);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(mNewMemo.getCreateDate());
                String str = format.format(date);
                createTime.setText(getString(R.string.create_time)+str);
                if(alarmEnable.equals("true")){
                    tv_alarm_time.setVisibility(View.VISIBLE);
//                    SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd hh:mm");

                    tv_alarm_time.setText(DateUtils.getRelativeTimeSpanString(this,alarmDate));
                }

                //保存最初的值，在退出时比较有没有变化
                preContent = content;
                preGroupName = groupName;
                preFontSize = fontSize;
                preStick = mStick;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_stick) {
            if(mStick == 0){
                mStick = 1;//置顶
            }else{
                mStick = 0;//取消置顶
            }
            invalidateOptionsMenu();
            return true;
        }else if(id == R.id.menu_fontsize){
            showFontSizeDialog();
            return true;
        }else if(id == R.id.menu_delete){
            batchDelete();
            return true;
        }else if(id == R.id.menu_group){
            showGroupNameDialog();
            return true;
        }else if(id == android.R.id.home){
            saveMemo();
            updateWidget();
            finish();
            return true;
        }else if(id == R.id.menu_alarm){
            showAlarmDialog();
            return true;
        }else if(id == R.id.menu_share){
            showShareDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mStick == 1){
            menu.findItem(R.id.menu_stick).setIcon(R.drawable.icon_stick);
        }else{
            menu.findItem(R.id.menu_stick).setIcon(R.drawable.icon_stick_cancel);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void showShareDialog(){
        final String content = mEditText.getText().toString().trim();
        if(TextUtils.isEmpty(content)){
            Toast.makeText(this,getString(R.string.no_share_content),Toast.LENGTH_SHORT).show();
            return;
        }else{
            AlertDialog.Builder dialog = new AlertDialog.Builder(
                    this);
            dialog.setTitle(getString(R.string.menu_share));

            String[] items = new String[]{getString(R.string.share_with_text),getString(R.string.share_with_pic)};

            dialog.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    selectShareItem = which;
                }
            });

            dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (selectShareItem){
                        case 0:
                            //分享文字
                            Intent textIntent = new Intent(Intent.ACTION_SEND);
                            textIntent.setType("text/plain");
                            textIntent.putExtra(Intent.EXTRA_TEXT, content+"\n"+"\n"+getString(R.string.share_text));
                            startActivity(Intent.createChooser(textIntent, getString(R.string.menu_share)));
                            break;
                        case 1:
                            //分享图片
                            String imagePath = saveBitmap(mScrollView,mNewMemo.getId()+".jpg");
                            Log.e("memo","生成后的path"+imagePath);
                            //由文件得到uri
                            Uri imageUri = Uri.fromFile(new File(imagePath));
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                            shareIntent.setType("image/*");
                            startActivity(Intent.createChooser(shareIntent, getString(R.string.menu_share_to)));
                            break;
                    }
                    dialog.dismiss();
                }
            });
            dialog.setNegativeButton(android.R.string.cancel, null);

            dialog.show();
        }
    }

    /**
     * 截取scrollview的屏幕
     **/
    public Bitmap getScrollViewBitmap(ScrollView scrollView) {
        int h = 0;
        //获取子布局的高
        h += scrollView.getChildAt(0).getHeight();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //创建对应大小的bitmap
        Bitmap bitmap = Bitmap.createBitmap(dm.widthPixels, h,
                Bitmap.Config.ARGB_4444);
        //创建一个画布
        final Canvas canvas = new Canvas(bitmap);
        //设置画布的背景色
        canvas.drawColor(Color.parseColor("#f2f7fa"));
        //将scrollview的内容画到画布上
        scrollView.draw(canvas);
        return bitmap;
    }

    private String saveBitmap(ScrollView scrollView,String bitName)
    {
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "MyMemos" +File.separator+ "sharePicture";
        File file1 = new File(path);
        if(!file1.exists()){
            file1.mkdirs();
        }
        String picturePath = path+File.separator+bitName;
        File file = new File(picturePath);
        if(file.exists()){
            file.delete();
        }
        FileOutputStream out;
        try{
            out = new FileOutputStream(file);
            Bitmap bitmap = getScrollViewBitmap(scrollView);
            if(bitmap.compress(Bitmap.CompressFormat.PNG, 90, out))
            {
                out.flush();
                out.close();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return picturePath;
    }

    //删除便签
    private void batchDelete() {
        new AsyncTask<Integer, Integer, Boolean>() {
            protected Boolean doInBackground(Integer... params) {
                    if(createNew){
                        finish();
                    }else{
                        if(memoId!=0){
                            updateWidget();
                            dbManager.deleteMemos(memoId);
                            return true;
                        }

                    }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                 if(result){
                     finish();
                 }
            }
        }.execute();
    }


    //便签提示 时间选择器
    private void showAlarmDialog(){
        //先显示日期选择，再显示时间选择
        datePickerDialog.setVibrate(false);//是否抖动
        datePickerDialog.setYearRange(1985, 2028);//设置年份区间
        datePickerDialog.setCloseOnSingleTapDay(false);//选择后是否消失
        datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);//展示dialog，传一个tag参数

    }

    //日期设置的监听器，得到年月日
    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        mYear = year;
        mMonth = month;
        mDay = day;
        timePickerDialog.setVibrate(false);//是否抖动
        timePickerDialog.setCloseOnSingleTapMinute(false);//选择后是否消失
        timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);//展示dialog，传一个tag参数
    }

    //时间设置的监听器，得到时分
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        /**
         * 这里返回的hourOfDay是24小时制的小时，就是说无论你设置的timePicker是否是24小时制，这里都返回24小时制的小时。
         * 很方便我们来做判断。比如如果hourOfDay>12就说明是下午了。
         */
        mHour = hourOfDay;
        mMinute = minute;

        setAlarm();
    }

    private boolean setAlarm() {

        Calendar c = Calendar.getInstance();
        c.set(mYear, mMonth, mDay, mHour, mMinute);

        if (!checkAlarmTime(c)) {
            return false;
        }

        mNewMemo.setAlarmDate(c.getTimeInMillis());
        mNewMemo.setAlarmEnable("true");
        dbManager.updateMemo(mNewMemo);
        AlarmUtil.setAlarmClock(this,mNewMemo);
        return true;
    }

    private boolean checkAlarmTime(Calendar alarmCalendar) {
        if (alarmCalendar.getTime().getTime() - System.currentTimeMillis() >0) {// 判断时间设置是否合理
            return true;
        }else{
            Toast.makeText(MemoEditActivity.this, R.string.alarm_time_err,
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void showFontSizeDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(
                this);
        dialog.setTitle(getString(R.string.dialog_fontsize_title));

        String[] items = new String[]{getString(R.string.super_small_size),getString(R.string.small_size),
                getString(R.string.normal_size),getString(R.string.huge_size),getString(R.string.super_huge_size)};
        //判断对话框应选择哪一项
        if(fontSize == FONT_SIZE_SUPER_SMALL){
            mFontSizeItem = 0;
        }else if(fontSize == FONT_SIZE_SMALL){
            mFontSizeItem = 1;
        }else if(fontSize == FONT_SIZE_NORMAL){
            mFontSizeItem = 2;
        }else if(fontSize == FONT_SIZE_HUGE){
            mFontSizeItem = 3;
        }else {
            mFontSizeItem = 4;
        }

        dialog.setSingleChoiceItems(items, mFontSizeItem, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 4:
                        fontSize = FONT_SIZE_SUPER_HUGE;
                        break;
                    case 3:
                        fontSize = FONT_SIZE_HUGE;
                        break;
                    case 2:
                        fontSize = FONT_SIZE_NORMAL;
                        break;
                    case 1:
                        fontSize = FONT_SIZE_SMALL;
                        break;
                    case 0:
                        fontSize = FONT_SIZE_SUPER_SMALL;
                        break;

                }
                mFontSizeItem = which;
                mEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP,fontSize);
                dialog.dismiss();
            }
        });


        dialog.setNegativeButton(android.R.string.cancel, null);

        dialog.show();
    }


    private void showGroupNameDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(
                this);
        dialog.setTitle(getString(R.string.dialog_groupname_title));

        //判断对话框应选择哪一项
        final List<String> groupList = dbManager.queryAllGroup();
        for(int i =0;i<groupList.size();i++){
            String name = groupList.get(i);
            if(groupName.equals(name)){
                mGroupItem = i;
                break;
            }
        }

        dialog.setSingleChoiceItems(groupList.toArray(new String[groupList.size()]), mGroupItem, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //保存分组
                groupName = groupList.get(which);
                mGroupItem = which;
                dialog.dismiss();
            }
        });


        dialog.setNegativeButton(android.R.string.cancel, null);

        dialog.show();
    }

    private void saveMemo() {
        String newContent = mEditText.getText().toString();
        mNewMemo.setContent(newContent);
        mNewMemo.setWidgetId(mAppWidgetId);
        mNewMemo.setWidgetType(mAppWidgetType);
        mNewMemo.setGroupName(groupName);
        mNewMemo.setFontSize(fontSize);
        mNewMemo.setModifiedDate(System.currentTimeMillis());
        mNewMemo.setStick(mStick);
        if (!createNew) {//便签变化时才更新
            if(compareMemo()){
                //内容变化时才更新
                dbManager.updateMemo(mNewMemo);
            }
        } else {
            if (TextUtils.isEmpty(mNewMemo.getContent())) {
                return ;
            }
            dbManager.addMemo(mNewMemo);
        }
    }


    private boolean compareMemo(){
        if(preFontSize != mNewMemo.getFontSize())
            return true;
        if(!preGroupName.equals(mNewMemo.getGroupName()))
            return true;
        if(!preContent.equals(mNewMemo.getContent()))
            return true;
        if(preStick != mStick)
            return true;
        return false;
    }

    private void updateWidget() {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        if (mNewMemo.getWidgetType() == GlobalValues.WIDGET_1X) {
            intent.setClass(this, MemoWidgetProvider_4x1.class);
        } else if (mNewMemo.getWidgetType() == GlobalValues.WIDGET_2X) {
            intent.setClass(this, MemoWidgetProvider_4x2.class);
        } else if (mNewMemo.getWidgetType() == GlobalValues.WIDGET_3X) {
            intent.setClass(this, MemoWidgetProvider_4x3.class);
        } else if (mNewMemo.getWidgetType() == GlobalValues.WIDGET_4X) {
            intent.setClass(this, MemoWidgetProvider_4x4.class);
        } else {
            Log.e("updateWidget", "Unspported widget type");
            return;
        }

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {
                mAppWidgetId
        });

        sendBroadcast(intent);
    }

    @Override
    public void onBackPressed() {
        saveMemo();
        updateWidget();
        super.onBackPressed();
    }
}

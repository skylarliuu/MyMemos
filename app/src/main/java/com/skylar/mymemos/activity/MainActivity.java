package com.skylar.mymemos.activity;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.skylar.mymemos.R;
import com.skylar.mymemos.adapter.MemoAdapter;
import com.skylar.mymemos.adapter.MenuItemAdapter;
import com.skylar.mymemos.application.GlobalValues;
import com.skylar.mymemos.application.MyApplication;
import com.skylar.mymemos.bean.LvMenuItem;
import com.skylar.mymemos.bean.Memo;
import com.skylar.mymemos.bean.MemoItem;
import com.skylar.mymemos.db.MemoDBManager;
import com.skylar.mymemos.widget.MemoWidgetProvider_4x1;
import com.skylar.mymemos.widget.MemoWidgetProvider_4x2;
import com.skylar.mymemos.widget.MemoWidgetProvider_4x3;
import com.skylar.mymemos.widget.MemoWidgetProvider_4x4;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView mListView;
    private Cursor cursor = null;
    private MemoAdapter adapter;
    private SQLiteDatabase db;
    private MemoDBManager dbManager;
    private ModeCallback mModeCallBack;
    private ActionBarDrawerToggle toggle;
    private NavigationView mNavigationView;
    private DrawerLayout drawer;
    private ListView mLvLeftMenu;
    private List<LvMenuItem> mItems = new ArrayList<LvMenuItem>();
    private List<String> groupNameList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.all_group));
        toolbar.setTitleTextColor(Color.parseColor("#ffffff")); //设置标题颜色
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mLvLeftMenu = (ListView) findViewById(R.id.id_lv_left_menu);
        mListView = (ListView) findViewById(R.id.listView);
        db = MyApplication.getDB();
        dbManager = MyApplication.getDBManager();
        cursor = dbManager.queryAll();
        adapter = new MemoAdapter(this,cursor,false);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new OnMyItemClickListener());
        mListView.setOnItemLongClickListener(new OnMyItemLongClickListener());
        mModeCallBack = new ModeCallback();

        SharedPreferences sp = getSharedPreferences("Memo",MODE_PRIVATE);
        Boolean isFirstStart = sp.getBoolean("isFirstStart",false);
        if(!isFirstStart){
            //第一次运行APP，默认添加分组-全部
            dbManager.addGroup(getString(R.string.all_group));
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isFirstStart",true);
            editor.commit();
        }

        //新增便签
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show()
                Intent intent = new Intent(MainActivity.this,
                        MemoEditActivity.class);
                intent.putExtra(Intent.EXTRA_UID, 0L);
                startActivity(intent);
            }
        });

        //设置侧边栏的内容
        setUpDrawer();

//        LayoutInflater inflater = LayoutInflater.from(this);
//        mLvLeftMenu.addHeaderView(inflater.inflate(R.layout.nav_header_main, mLvLeftMenu, false));

        mLvLeftMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
           int itemPosition = position - mLvLeftMenu.getHeaderViewsCount();
           LvMenuItem item = mItems.get(itemPosition);
           String itemString;
           if(view instanceof TextView){
              itemString = ((TextView) view).getText().toString();
           }else{
              return;
           }
            //关闭侧边栏
           drawer.closeDrawer(GravityCompat.START);
           switch (item.type)
           {
           case LvMenuItem.TYPE_NORMAL: //左边图标，右边文字
           if(itemString.equals(getString(R.string.all_group))){//查询全部
              toolbar.setTitle(itemString);
              cursor = dbManager.queryAll();
              adapter.changeCursor(cursor);
           }else if(itemString.equals(getString(R.string.backup_restore))) {//备份与恢复
             Intent intent = new Intent(MainActivity.this, BackupRestoreActivity.class);
             MainActivity.this.startActivity(intent);
           }else{//查询指定分组
                toolbar.setTitle(itemString);
                cursor = dbManager.queryGroup(itemString);
                adapter.changeCursor(cursor);
           }
           break;
           case LvMenuItem.TYPE_NO_ICON: //只有文字
           if(itemString.equals(getString(R.string.group_manager))){//分组管理
                Intent intent = new Intent(MainActivity.this,EditGroupActivity.class);
                intent.putExtra("groupNameList",(Serializable) groupNameList);
                MainActivity.this.startActivityForResult(intent,1);
           }
           break;
           }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cursor.requery();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                //分组管理界面
                setUpDrawer();
                break;
        }
    }

     private void setUpDrawer()
     {
        if(mItems != null){
            mItems.clear();
        }
        if(groupNameList != null){
            groupNameList.clear();
        }
        //侧边栏的数据集合
        mItems = new ArrayList<LvMenuItem>();
        //添加分割线
        mItems.add(new LvMenuItem());
        mItems.add(new LvMenuItem(getString(R.string.group_manager)));
        //获取数据库里的分组
        groupNameList = dbManager.queryAllGroup();
        if(groupNameList != null && groupNameList.size()>0){
          for (String groupName : groupNameList){
            if(groupName.equals(getString(R.string.all_group))){
              mItems.add(new LvMenuItem(R.drawable.icon_all_group_black,groupName));
            }else{
              mItems.add(new LvMenuItem(R.drawable.icon_a_group_,groupName));
            }
          }
        }
        //添加分割线
        mItems.add(new LvMenuItem());
        //备份与恢复
        mItems.add(new LvMenuItem(R.drawable.icon_backup,"备份与恢复"));
        mLvLeftMenu.setAdapter(new MenuItemAdapter(this,mItems));
     }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//
////        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//
//        return super.onOptionsItemSelected(item);
//    }


//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
////        int id = item.getItemId();
////
////        if (id == R.id.group_all) {
////            cursor = dbManager.queryGroup("全部");
////        }
//        adapter.changeCursor(cursor);
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        Log.e("onContextMenuClosed","onContextMenuClosed");
        if (mListView != null) {
            mListView.setOnCreateContextMenuListener(null);
        }
        super.onContextMenuClosed(menu);
    }

    class ModeCallback implements ListView.MultiChoiceModeListener,
            MenuItem.OnMenuItemClickListener {
        private ActionMode mActionMode;

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.memo_list_options, menu);
            menu.findItem(R.id.delete).setOnMenuItemClickListener(this);
            menu.findItem(R.id.stick).setOnMenuItemClickListener(this);

            mActionMode = mode;
            adapter.setChoiceMode(true);
            mListView.setLongClickable(false);
            return true;
        }

        public void finishActionMode() {
            mActionMode.finish();
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            //退出长按编辑模式
            adapter.setChoiceMode(false);
            mListView.setLongClickable(true);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (adapter.getSelectedCount() == 0) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.menu_select_none),
                        Toast.LENGTH_SHORT).show();
                return true;
            }

            switch (item.getItemId()){
                case R.id.delete:
                    //删除便签
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            MainActivity.this);
                    builder.setTitle(getString(R.string.alert_title_delete));
//            builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setMessage(getString(R.string.alert_message_delete_notes,
                            adapter.getSelectedCount()));
                    builder.setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    batchDelete();
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.show();
                    break;
                case R.id.stick:
                    //置顶便签
                    setStick();
                    break;
            }
            return true;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            adapter.setCheckedItem(position, checked);
        }

    }

    private void batchDelete() {
        new AsyncTask<Integer, Integer, Cursor>() {
            protected Cursor doInBackground(Integer... params) {
                HashSet<Long> ids = adapter.getSelectedItemIds();
                for (Long id:ids){
                    Memo memo = dbManager.getMemo(id);
                    int widgetId = memo.getWidgetId();
                    int widgetType = memo.getWidgetType();
                    if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                            && widgetType != GlobalValues.WIDGET_INVALIDE) {
                        updateWidget(widgetId,widgetType);
                    }
                    dbManager.deleteMemos(id);
                }
                cursor = dbManager.queryAll();
                return cursor;
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                adapter.changeCursor(cursor);
                mModeCallBack.finishActionMode();
            }
        }.execute();
    }

    private void setStick(){
        new AsyncTask<Integer, Integer, Cursor>() {
            protected Cursor doInBackground(Integer... params) {
                HashSet<Long> ids = adapter.getSelectedItemIds();
                for (Long id:ids){
                    Log.e("test","set stick id+"+id);
                    Memo memo = dbManager.getMemo(id);
                    int stick = memo.getStick();
                    if(stick == 0){
                        memo.setStick(1);
                    }else{
                        memo.setStick(0);
                    }
                    dbManager.updateMemo(memo);
                }
                cursor = dbManager.queryAll();
                return cursor;
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                adapter.changeCursor(cursor);
                mModeCallBack.finishActionMode();
            }
        }.execute();
    }

    private void updateWidget(int appWidgetId, int appWidgetType) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        if (appWidgetType == GlobalValues.WIDGET_1X) {
            intent.setClass(this, MemoWidgetProvider_4x1.class);
        } else if (appWidgetType == GlobalValues.WIDGET_2X) {
            intent.setClass(this, MemoWidgetProvider_4x2.class);
        } else if (appWidgetType == GlobalValues.WIDGET_3X) {
            intent.setClass(this, MemoWidgetProvider_4x3.class);
        } else if (appWidgetType == GlobalValues.WIDGET_4X) {
            intent.setClass(this, MemoWidgetProvider_4x4.class);
        } else {
            Log.e("updateWidget", "Unspported widget type");
            return;
        }

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {
                appWidgetId
        });

        sendBroadcast(intent);
    }


    class OnMyItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if (adapter.isInChoiceMode()) {
                position = position - mListView.getHeaderViewsCount();
                mModeCallBack.onItemCheckedStateChanged(null, position, id,
                        !adapter.isSelectedItem(position));
            } else {
                Memo item = ((MemoItem) view).getItemData();
                Intent intent = new Intent(MainActivity.this,
                        MemoEditActivity.class);
                intent.putExtra(Intent.EXTRA_UID, item.getId());
                startActivity(intent);
            }
            return;
        }

    }

    class OnMyItemLongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            if (view instanceof MemoItem) {
                position = position - mListView.getHeaderViewsCount();
                if (mListView.startActionMode(mModeCallBack) != null) {
                    mModeCallBack.onItemCheckedStateChanged(null, position, id,
                            !adapter.isSelectedItem(position));
                }

                return true;
            }
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(adapter.isInChoiceMode()){
                //如果是选择状态就要还原
                adapter.setChoiceMode(false);
            }
            super.onBackPressed();
        }
    }
}
